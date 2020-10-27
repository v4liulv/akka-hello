package com.tcfuture.akka.actor.example.iot;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * @author liulv
 */
public class DeviceGroupTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    /**
     * 设置监控并注册请求
     */
    @Test
    public void testReplyToRegistrationRequests() {
        //设备注册probe
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        //创建设备组Actor
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group01"));

        //设备组Actor(group01)发送消息监控注册（group01，device01，设备组Actor）
        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device01", probe.getRef()));
        DeviceManager.DeviceRegistered registered1 = probe.receiveMessage();

        //设备组Actor(group01)发送消息监控注册（group01，device02，设备组Actor）
        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device02", probe.getRef()));
        DeviceManager.DeviceRegistered registered2 = probe.receiveMessage();
        assertNotEquals(registered1.device, registered2.device);

        // 检查设备Actor是否在工作
        TestProbe<Device.TemperatureRecorded> recordProbe =
                testKit.createTestProbe(Device.TemperatureRecorded.class);
        registered1.device.tell(new Device.RecordTemperature(0L, 1.0, recordProbe.getRef()));
        assertEquals(0L, recordProbe.receiveMessage().requestId);
        registered2.device.tell(new Device.RecordTemperature(1L, 2.0, recordProbe.getRef()));
        assertEquals(1L, recordProbe.receiveMessage().requestId);
    }

    /**
     * 测试忽略错误的注册请求：
     * 其中创建了名为group的分组Actor,但是请求的注册的分组ID名为wrongGroup
     */
    @Test
    public void testIgnoreWrongRegistrationRequests() {
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));
        groupActor.tell(new DeviceManager.RequestTrackDevice("wrongGroup", "device1", probe.getRef()));
        probe.expectNoMessage();
    }

    /**
     * 测试用例：测试已经存在设备Actor进行注册
     *
     * 如果已经存在用于注册请求的设备Actor，我们想使用现有的Actor而不是新的Actor。我们尚未对此进行测试，因此我们需要解决此问题
     */
    @Test
    public void testReturnSameActorForSameDeviceId() {
        //为Testkit Actor System创建新的测试探针的快捷方式，DeviceManager.DeviceRegistered为探测应该接受的消息类型
        TestProbe<DeviceManager.DeviceRegistered> probe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        //创建名称为"group01"的分组actor
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group01"));
        //向group01 Actor发送消息,消息类型为RequestTrackDevice（"group01", "device01"）
        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device01", probe.getRef()));
        DeviceManager.DeviceRegistered registered1 = probe.receiveMessage();

        // 再次注册一样的设备Actor应该是相等的
        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device01", probe.getRef()));
        DeviceManager.DeviceRegistered registered2 = probe.receiveMessage();
        assertEquals(registered1.device, registered2.device);
    }

    /**
     * 测试用例：测试活着的设备列表
     *
     * 在添加了一些设备之后，是否能返回正确的 ID 列表
     */
    @Test
    public void testListActiveDevices() {
        //创建 test probe 快捷方式用于testkit actor system, 应该接受的消息类型为DeviceRegistered
        TestProbe<DeviceManager.DeviceRegistered> registeredProbe = testKit.createTestProbe(
                DeviceManager.DeviceRegistered.class);
        //创建设备组Actor
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group01"));

        //给group01发送RequestTrackDevice消息
        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device01", registeredProbe.getRef()));
        //在默认超时期限内接收一个类型为“M” DeviceRegistered 的消息。
        registeredProbe.receiveMessage();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device02", registeredProbe.getRef()));
        registeredProbe.receiveMessage();

        //查询活跃的Actor
        TestProbe<DeviceManager.ReplyDeviceList> deviceListProbe = testKit.createTestProbe(DeviceManager.ReplyDeviceList.class);
        groupActor.tell(new DeviceManager.RequestDeviceList(0L, "group01", deviceListProbe.getRef()));
        DeviceManager.ReplyDeviceList reply = deviceListProbe.receiveMessage();
        //打印查询ID
        System.out.println(reply.requestId);
        //打印有心跳活着的设备ID
        System.out.println(reply.ids);
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device01", "device02").collect(Collectors.toSet()), reply.ids);
    }

    /**
     * 测试用例：确保在设备 Actor 停止后正确删除设备 ID
     *
     * 添加设备01和设备02，发送消息给设备01停止设备，查看设备列表是否只有了设备02。
     */
    @Test
    public void testListActiveDevicesAfterOneShutsDown() {
        //添加了一些设备
        TestProbe<DeviceManager.DeviceRegistered> registeredProbe = testKit.createTestProbe(DeviceManager.DeviceRegistered.class);
        ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group01"));

        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device01",
                registeredProbe.getRef()));
        DeviceManager.DeviceRegistered registered1 = registeredProbe.receiveMessage();

        groupActor.tell(new DeviceManager.RequestTrackDevice("group01", "device02",
                registeredProbe.getRef()));
        DeviceManager.DeviceRegistered registered2 = registeredProbe.receiveMessage();

        //请求设备列表
        TestProbe<DeviceManager.ReplyDeviceList> deviceListProbe = testKit.createTestProbe(DeviceManager.ReplyDeviceList.class);
        groupActor.tell(new DeviceManager.RequestDeviceList(0L, "group01", deviceListProbe.getRef()));
        DeviceManager.ReplyDeviceList reply = deviceListProbe.receiveMessage();
        assertEquals(0L, reply.requestId);
        assertEquals(Stream.of("device01", "device02").collect(Collectors.toSet()), reply.ids);

        //停止registered1
        ActorRef<Device.Command> toShutDown = registered1.device;
        toShutDown.tell(Device.Passivate.INSTANCE);
        registeredProbe.expectTerminated(toShutDown, registeredProbe.getRemainingOrDefault());

        // using awaitAssert to retry because it might take longer for the groupActor
        //        // to see the Terminated, that order is undefined
        registeredProbe.awaitAssert(
                () -> {
                    groupActor.tell(new DeviceManager.RequestDeviceList(1L, "group01",
                            deviceListProbe.getRef()));
                    DeviceManager.ReplyDeviceList r = deviceListProbe.receiveMessage();
                    assertEquals(1L, r.requestId);
                    System.out.println("查看actor列表:" + reply.ids);
                    assertEquals(Stream.of("device02").collect(Collectors.toSet()), r.ids);
                    return null;
                });
    }
}
