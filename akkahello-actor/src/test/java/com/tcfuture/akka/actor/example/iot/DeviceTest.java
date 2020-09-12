package com.tcfuture.akka.actor.example.iot;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * @author liulv
 *
 * Iot Device 测试
 */
public class DeviceTest {
    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    /**
     * 测试用例：如果没有已知的温度回复空读取
     */
    @Test
    public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
        //空回复
        TestProbe<Device.RespondTemperature> probe =
                testKit.createTestProbe(Device.RespondTemperature.class);
        ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));
        deviceActor.tell(new Device.ReadTemperature(42L, probe.getRef()));
        Device.RespondTemperature response = probe.receiveMessage();
        assertEquals(42L, response.requestId);
        assertEquals(Optional.empty(), response.value);
    }

    /**
     * 测试用例：执行读/查询和写/记录功能
     *
     * 测试回复 - 读取到的最新温度
     */
    @Test
    public void testReplyWithLatestTemperatureReading() {
        //写 - 记录Probe
        TestProbe<Device.TemperatureRecorded> recordProbe =
                testKit.createTestProbe(Device.TemperatureRecorded.class);
        //读 - 读取Probe
        TestProbe<Device.RespondTemperature> readProbe =
                testKit.createTestProbe(Device.RespondTemperature.class);

        //创建Device Actor
        ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));

        //Device Actor 发送写消息，记录请求ID为1，设备温度为24
        deviceActor.tell(new Device.RecordTemperature(1L, 24.0, recordProbe.getRef()));
        assertEquals(1L, recordProbe.receiveMessage().requestId);

        //Device Actor 发送读消息，记录请求ID为2
        deviceActor.tell(new Device.ReadTemperature(2L, readProbe.getRef()));
        Device.RespondTemperature response1 = readProbe.receiveMessage();
        assertEquals(2L, response1.requestId);
        assertEquals(Optional.of(24.0), response1.value);

        //Device Actor 发送写记录温度消息，记录请求ID为3，设备温度为55
        deviceActor.tell(new Device.RecordTemperature(3L, 55.0, recordProbe.getRef()));
        assertEquals(3L, recordProbe.receiveMessage().requestId);

        //Device Actor 发送读取温度消息，请求者ID为4
        deviceActor.tell(new Device.ReadTemperature(4L, readProbe.getRef()));
        Device.RespondTemperature response2 = readProbe.receiveMessage();
        assertEquals(4L, response2.requestId);
        assertEquals(Optional.of(55.0), response2.value);
    }

}
