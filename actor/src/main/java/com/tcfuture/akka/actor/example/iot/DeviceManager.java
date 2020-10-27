package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author liulv
 *
 * 作为第一步，我们需要设计用于注册设备和创建负责该设备的组和设备参与者的协议。这个协议将由DeviceManager组件本身提供，因为它是预先已知和可用的
 * 唯一参与者:设备组和设备Actor是按需创建的。
 *
 * 1. 当DeviceManager收到一个带有组groupId和设备id的请求时:
 *   - 如果管理器已经有了设备组的actor，它就会将请求转发给该actor。
 *   - 否则，它将创建一个新的设备组actor，然后转发请求。
 * 2. DeviceGroup actor 接收到为给定设备注册actor的请求:
 *    - 如果组已经为该设备提供了一个actor，那么它将使用现有设备actor的ActorRef进行响应。
 *    - 否则，DeviceGroup actor首先创建一个设备actor并使用新创建的设备actor的ActorRef进行响应。
 * 3. 传感器现在将有ActorRef
 */
public class DeviceManager extends AbstractBehavior<DeviceManager.Command> {

    public interface Command {}

    /**
     * 请求跟踪监控设备
     */
    public static final class RequestTrackDevice
            implements DeviceManager.Command, DeviceGroup.Command {
        //设备组ID
        public final String groupId;
        //设备ID
        public final String deviceId;
        //注册ActorRef
        public final ActorRef<DeviceRegistered> replyTo;

        public RequestTrackDevice(String groupId, String deviceId, ActorRef<DeviceRegistered> replyTo) {
            this.groupId = groupId;
            this.deviceId = deviceId;
            this.replyTo = replyTo;
        }
    }

    /**
     * 已经注册的设备
     */
    public static final class DeviceRegistered {
        //设备ActorRef
        public final ActorRef<Device.Command> device;

        public DeviceRegistered(ActorRef<Device.Command> device) {
            this.device = device;
        }
    }

    /**
     * 目前为止，我们还没有办法知道组设备参与者跟踪哪些设备，因此，我们还不能测试我们的新功能。为了便于测试，我们添加了一个新的查询
     * 功能(messageRequestDeviceList)，它列出了当前活动的设备id:
     */
    public static final class RequestDeviceList
            implements DeviceManager.Command, DeviceGroup.Command {
        //请求ID
        final long requestId;
        //设置分组ID
        final String groupId;
        //请求设备列表ActorRef
        final ActorRef<ReplyDeviceList> replyTo;

        public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
            this.requestId = requestId;
            this.groupId = groupId;
            this.replyTo = replyTo;
        }
    }

    public static final class ReplyDeviceList {
        final long requestId;
        final Set<String> ids;

        public ReplyDeviceList(long requestId, Set<String> ids) {
            this.requestId = requestId;
            this.ids = ids;
        }
    }

    private static class DeviceGroupTerminated implements DeviceManager.Command {
        public final String groupId;

        DeviceGroupTerminated(String groupId) {
            this.groupId = groupId;
        }
    }

    public static final class RequestAllTemperatures
            implements DeviceGroupQuery.Command, DeviceGroup.Command, Command {

        final long requestId;
        final String groupId;
        final ActorRef<RespondAllTemperatures> replyTo;

        public RequestAllTemperatures(
                long requestId, String groupId, ActorRef<RespondAllTemperatures> replyTo) {
            this.requestId = requestId;
            this.groupId = groupId;
            this.replyTo = replyTo;
        }
    }

    public static final class RespondAllTemperatures {
        final long requestId;
        final Map<String, TemperatureReading> temperatures;

        public RespondAllTemperatures(long requestId, Map<String, TemperatureReading> temperatures) {
            this.requestId = requestId;
            this.temperatures = temperatures;
        }
    }

    //查询相关 start
    public interface TemperatureReading {}

    public static final class Temperature implements TemperatureReading {
        public final double value;

        public Temperature(double value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Temperature that = (Temperature) o;

            return Double.compare(that.value, value) == 0;
        }

        @Override
        public int hashCode() {
            long temp = Double.doubleToLongBits(value);
            return (int) (temp ^ (temp >>> 32));
        }

        @Override
        public String toString() {
            return "Temperature{" + "value=" + value + '}';
        }
    }

    //它已经做出了回应，但还没有给出温度:
    public enum TemperatureNotAvailable implements TemperatureReading {
        INSTANCE
    }

    //它在响应之前已停止
    public enum DeviceNotAvailable implements TemperatureReading {
        INSTANCE
    }

    //它在最后期限之前没有响应
    public enum DeviceTimedOut implements TemperatureReading {
        INSTANCE
    }
    //查询相关 end

    public static Behavior<Command> create() {
        return Behaviors.setup(DeviceManager::new);
    }

    private final Map<String, ActorRef<DeviceGroup.Command>> groupIdToActor = new HashMap<>();

    public DeviceManager(ActorContext<Command> context) {
        super(context);
        context.getLog().info("DeviceManager started");
    }

    private DeviceManager onTrackDevice(RequestTrackDevice trackMsg) {
        String groupId = trackMsg.groupId;
        ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(groupId);
        if (ref != null) {
            ref.tell(trackMsg);
        } else {
            getContext().getLog().info("Creating device group actor for {}", groupId);
            ActorRef<DeviceGroup.Command> groupActor =
                    getContext().spawn(DeviceGroup.create(groupId), "group-" + groupId);
            getContext().watchWith(groupActor, new DeviceGroupTerminated(groupId));
            groupActor.tell(trackMsg);
            groupIdToActor.put(groupId, groupActor);
        }
        return this;
    }

    private DeviceManager onRequestDeviceList(RequestDeviceList request) {
        ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(request.groupId);
        if (ref != null) {
            ref.tell(request);
        } else {
            request.replyTo.tell(new ReplyDeviceList(request.requestId, Collections.emptySet()));
        }
        return this;
    }

    private DeviceManager onTerminated(DeviceGroupTerminated t) {
        getContext().getLog().info("Device group actor for {} has been terminated", t.groupId);
        groupIdToActor.remove(t.groupId);
        return this;
    }

    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(RequestDeviceList.class, this::onRequestDeviceList)
                .onMessage(DeviceGroupTerminated.class, this::onTerminated)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private DeviceManager onPostStop() {
        getContext().getLog().info("DeviceManager stopped");
        return this;
    }

}
