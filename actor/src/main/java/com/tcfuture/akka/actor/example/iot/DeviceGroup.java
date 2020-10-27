package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 *
 * 一个Group actor有一些工作要做时，涉及到注册、停止、删除，包括:
 * 1. 处理现有设备参与者的注册请求或创建新的参与者。
 * 2. 跟踪组中存在哪些设备参与者，并在停止时将其从组中删除。
 *
 * 处理注册请求:
 *  设备组actor必须使用现有子元素的ActorRef响应请求，或者创建一个。为了通过子actor的设备id查找子actor，我们将使用一个映射。
 */
public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {
    //设备组ID
    private final String groupId;
    //集合（设备id：设备actor）
    private final Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();

    private DeviceGroup(ActorContext<Command> context, String groupId) {
        super(context);
        this.groupId = groupId;
        context.getLog().info("DeviceGroup {} started", groupId);
    }

    //抽象接口
    public interface Command {}

    /**
     * 创建设备组Actor
     *
     * @param groupId 组ID
     * @return DeviceGroup Actor
     */
    public static Behavior<Command> create(String groupId) {
        return Behaviors.setup(context -> new DeviceGroup(context, groupId));
    }

    /**
     * 列出了当前活动的设备id： 发送消息给设备管理者
     *
     * @param r DeviceManager.RequestDeviceList
     * @return DeviceGroup
     */
    private DeviceGroup onDeviceList(DeviceManager.RequestDeviceList r) {
        r.replyTo.tell(new DeviceManager.ReplyDeviceList(r.requestId, deviceIdToActor.keySet()));
        return this;
    }

    /**
     * 设备需要终止的实体类
     */
    private class DeviceTerminated implements Command {
        public final ActorRef<Device.Command> device;
        public final String groupId;
        public final String deviceId;

        DeviceTerminated(ActorRef<Device.Command> device, String groupId, String deviceId) {
            this.device = device;
            this.groupId = groupId;
            this.deviceId = deviceId;
        }
    }

    /**
     * 跟踪设备
     *   1. 如果注册的组ID跟本组ID相等，进行注册
     *      - 如设备actor存在发送消息告诉manager注册该actor
     *      - 否则根据组ID和设备ID创建设备actor，创建完后当前组添加该设备信息，然后在发送注册该actor消息
     *   2. 否则忽略请求跟踪不进行处理，只warn级别打印请求中的组ID 和 当前的组ID
     *
     * @param trackMsg manager请求跟踪设备
     * @return 设备组
     */
    private DeviceGroup onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
        //如果在该组内（分组ID相同）
        if (this.groupId.equals(trackMsg.groupId)) {
            ActorRef<Device.Command> deviceActor = deviceIdToActor.get(trackMsg.deviceId);
            if (deviceActor != null) {
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            } else {
                getContext().getLog().info("Creating device actor for {}", trackMsg.deviceId);
                deviceActor =
                        getContext()
                                .spawn(Device.create(groupId, trackMsg.deviceId), "device-" +
                                        trackMsg.deviceId);
                //对象识别出actor后，使用自定义消息注册终止通知
                getContext()
                        .watchWith(deviceActor, new DeviceTerminated(deviceActor, groupId,
                                trackMsg.deviceId));
                deviceIdToActor.put(trackMsg.deviceId, deviceActor);
                trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
            }
        } else {
            getContext()
                    .getLog()
                    .warn(
                            "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
                            groupId,
                            this.groupId);
        }
        return this;
    }

    /**
     * 从组中删除设备，也就是根据DeviceTerminated需要终止设备信息读取到设备ID, 然后在deviceIdToActor删除该设备ID
     *
     * @param t DeviceTerminated需要终止设备信息
     * @return 当前组实例
     */
    private DeviceGroup onTerminated(DeviceTerminated t) {
        getContext().getLog().info("Device actor for {} has been terminated", t.deviceId);
        deviceIdToActor.remove(t.deviceId);
        return this;
    }

    /**
     * 接收到监控消息后响应进行监控设备（调用onTrackDevice方法）
     *
     * @return Receive<Command>
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
                .onMessage(
                        DeviceManager.RequestDeviceList.class,
                        r -> r.groupId.equals(groupId),
                        this::onDeviceList)
                .onMessage(DeviceTerminated.class, this::onTerminated)
                .onMessage(DeviceManager.RequestAllTemperatures.class,
                        r -> r.groupId.equals(groupId),
                        this::onAllTemperatures)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * 停止组actor
     *
     * @return 设备组
     */
    private DeviceGroup onPostStop() {
        getContext().getLog().info("DeviceGroup {} stopped", groupId);
        return this;
    }

    /**
     * 现在在组参与者中包含查询特性相当简单。我们在查询actor本身中完成了所有繁重的工作，组actor只需要使用正确的初始参数创建它，而不需要其他东西。
     * @param r RequestAllTemperatures
     * @return
     */
    private DeviceGroup onAllTemperatures(DeviceManager.RequestAllTemperatures r) {
        // since Java collections are mutable, we want to avoid sharing them between actors (since
        // multiple Actors (threads)
        // modifying the same mutable data-structure is not safe), and perform a defensive copy of the
        // mutable map:
        //
        // Feel free to use your favourite immutable data-structures library with Akka in Java
        // applications!
        Map<String, ActorRef<Device.Command>> deviceIdToActorCopy = new HashMap<>(this.deviceIdToActor);

        getContext().spawnAnonymous(DeviceGroupQuery.create(deviceIdToActorCopy, r.requestId, r.replyTo, Duration.ofSeconds(3)));

        return this;
    }

}
