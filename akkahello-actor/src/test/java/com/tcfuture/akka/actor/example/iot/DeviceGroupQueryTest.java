package com.tcfuture.akka.actor.example.iot;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import com.tcfuture.akka.actor.example.iot.DeviceManager.RespondAllTemperatures;
import com.tcfuture.akka.actor.example.iot.DeviceManager.Temperature;
import com.tcfuture.akka.actor.example.iot.DeviceManager.TemperatureNotAvailable;
import com.tcfuture.akka.actor.example.iot.DeviceManager.TemperatureReading;
import org.junit.ClassRule;
import org.junit.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author liulv
 *
 * 现在，让我们验证查询 Actor 实现的正确性。我们需要单独测试各种场景，以确保一切都按预期工作。为了能够做到这一点，我们需要以某种方式模拟设备 Actor 来运行各种正常或故障场景。幸运的是，我们将合作者（collaborators）列表（实际上是一个Map）作为查询 Actor 的参数，这样我们就可以传入TestKit引用。
 */
public class DeviceGroupQueryTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    /**
     * 测试用例：添加了两个设备的情况下进行测试，两个设备都报告了温度
     */
    @Test
    public void testReturnTemperatureValueForWorkingDevices() {
        TestProbe<RespondAllTemperatures> requester =
                testKit.createTestProbe(DeviceManager.RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        //创建查询Actor
        ActorRef<DeviceGroupQuery.Command> queryActor = testKit.spawn(
                        DeviceGroupQuery.create(deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

        device1.expectMessageClass(Device.ReadTemperature.class);
        device2.expectMessageClass(Device.ReadTemperature.class);

        //发送设备1的温度消息
        queryActor.tell(new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

        //发送设备2的温度消息
        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

        RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new Temperature(1.0));
        expectedTemperatures.put("device2", new Temperature(2.0));

        assertEquals(expectedTemperatures, response.temperatures);
    }

    /**
     * 测试用例：有时设备不能提供温度测量。这个场景与前一个稍有不同
     */
    @Test
    public void testReturnTemperatureNotAvailableForDevicesWithNoReadings() {
        TestProbe<RespondAllTemperatures> requester =
                testKit.createTestProbe(RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        ActorRef<DeviceGroupQuery.Command> queryActor =
                testKit.spawn(DeviceGroupQuery.create(deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

        assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.empty())));

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

        RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", TemperatureNotAvailable.INSTANCE);
        expectedTemperatures.put("device2", new Temperature(2.0));

        assertEquals(expectedTemperatures, response.temperatures);
    }

    /**
     * 测试用例：时候设备操作者会在回答之前停下来
     */
    @Test
    public void testReturnDeviceNotAvailableIfDeviceStopsBeforeAnswering() {
        TestProbe<RespondAllTemperatures> requester =
                testKit.createTestProbe(RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        ActorRef<DeviceGroupQuery.Command> queryActor =
                testKit.spawn(
                        DeviceGroupQuery.create(
                                deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

        assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

        device2.stop();

        RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new Temperature(1.0));
        expectedTemperatures.put("device2", DeviceManager.DeviceNotAvailable.INSTANCE);

        assertEquals(expectedTemperatures, response.temperatures);
    }

    /**
     * 如果你还记得，还有另一种情况与设备Actor停止有关。有可能我们从一个设备Actor那里得到一个正常的回复，但是之
     * 后会收到一个终止的相同Actor的回复。在这种情况下，我们希望保留第一个回复，不将该设备标记为
     * DeviceNotAvailable。我们也应该测试一下:
     */
    @Test
    public void testReturnTemperatureReadingEvenIfDeviceStopsAfterAnswering() {
        TestProbe<RespondAllTemperatures> requester =
                testKit.createTestProbe(RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        ActorRef<DeviceGroupQuery.Command> queryActor =
                testKit.spawn(
                        DeviceGroupQuery.create(
                                deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

        assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

        device2.stop();

        RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new Temperature(1.0));
        expectedTemperatures.put("device2", new Temperature(2.0));

        assertEquals(expectedTemperatures, response.temperatures);
    }


    /**
     * 最后一种情况是，不是所有设备都及时响应。为了保持我们的测试相对快速，我们将用更小的超时构造
     * DeviceGroupQuery actor:
     */
    @Test
    public void testReturnDeviceTimedOutIfDeviceDoesNotAnswerInTime() {
        TestProbe<RespondAllTemperatures> requester =
                testKit.createTestProbe(RespondAllTemperatures.class);
        TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
        TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

        Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
        deviceIdToActor.put("device1", device1.getRef());
        deviceIdToActor.put("device2", device2.getRef());

        ActorRef<DeviceGroupQuery.Command> queryActor =
                testKit.spawn(
                        DeviceGroupQuery.create(
                                deviceIdToActor, 1L, requester.getRef(), Duration.ofMillis(200)));

        assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
        assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

        queryActor.tell(
                new DeviceGroupQuery.WrappedRespondTemperature(
                        new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

        // no reply from device2

        RespondAllTemperatures response = requester.receiveMessage();
        assertEquals(1L, response.requestId);

        Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
        expectedTemperatures.put("device1", new Temperature(1.0));
        expectedTemperatures.put("device2", DeviceManager.DeviceTimedOut.INSTANCE);

        assertEquals(expectedTemperatures, response.temperatures);
    }
}
