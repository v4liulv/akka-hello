package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liulv
 *
 *
 */
public class DeviceGroupQuery extends AbstractBehavior<DeviceGroupQuery.Command> {

    public interface Command {}

    /**
     * 创建一个表示查询超时的消息：任何参数的简单消息
     */
    private static enum CollectionTimeout implements Command {
        INSTANCE
    }

    /**
     * 封装响应温度
     */
    static class WrappedRespondTemperature implements Command {
        final Device.RespondTemperature response;

        WrappedRespondTemperature(Device.RespondTemperature response) {
            this.response = response;
        }
    }

    /**
     * 设备停止
     */
    private static class DeviceTerminated implements Command {
        final String deviceId;

        private DeviceTerminated(String deviceId) {
            this.deviceId = deviceId;
        }
    }

    /**
     * 创建组查询Actor
     *
     * @param deviceIdToActor 要查询的活动设备 Actor 的快照和 ID
     * @param requestId 启动查询的请求的 ID（以便我们可以在响应中包含它）
     * @param requester 发送查询的 Actor 的引用。我们会直接给这个 Actor 响应
     * @param timeout 指示查询等待响应的期限。将其作为参数将简化测试
     * @return DeviceGroupQuery 设备组查询Actor
     */
    public static Behavior<Command> create(
            Map<String, ActorRef<Device.Command>> deviceIdToActor,
            long requestId,
            ActorRef<DeviceManager.RespondAllTemperatures> requester,
            Duration timeout) {
        return Behaviors.setup(
                context -> Behaviors.withTimers( timers -> new DeviceGroupQuery(deviceIdToActor, requestId, requester, timeout, context, timers)));
    }

    //请求ID
    private final long requestId;
    //发送查询的 Actor 的引用。我们会直接给这个 Actor 响应
    private final ActorRef<DeviceManager.RespondAllTemperatures> requester;
    //收集已经回复的Acton, 更新温度信息
    private Map<String, DeviceManager.TemperatureReading> repliesSoFar = new HashMap<>();
    //在超时的情况下，我们需要获取所有尚未响应的actor
    private final Set<String> stillWaiting;

    /**
     * 构造方法
     */
    private DeviceGroupQuery(
            Map<String, ActorRef<Device.Command>> deviceIdToActor,
            long requestId,
            ActorRef<DeviceManager.RespondAllTemperatures> requester,
            Duration timeout,
            ActorContext<Command> context,
            TimerScheduler<Command> timers) {
        super(context);
        this.requestId = requestId;
        this.requester = requester;

        timers.startSingleTimer(CollectionTimeout.INSTANCE, timeout);

        ActorRef<Device.RespondTemperature> respondTemperatureAdapter =
                context.messageAdapter(Device.RespondTemperature.class, WrappedRespondTemperature::new);

        for (Map.Entry<String, ActorRef<Device.Command>> entry : deviceIdToActor.entrySet()) {
            context.watchWith(entry.getValue(), new DeviceTerminated(entry.getKey()));
            entry.getValue().tell(new Device.ReadTemperature(0L, respondTemperatureAdapter));
        }
        stillWaiting = new HashSet<>(deviceIdToActor.keySet());
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(WrappedRespondTemperature.class, this::onRespondTemperature)
                .onMessage(DeviceTerminated.class, this::onDeviceTerminated)
                .onMessage(CollectionTimeout.class, this::onCollectionTimeout)
                .build();
    }

    private Behavior<Command> onRespondTemperature(WrappedRespondTemperature r) {
        DeviceManager.TemperatureReading reading =
                r.response.value.map(v -> (DeviceManager.TemperatureReading) new DeviceManager.Temperature(v)).orElse(DeviceManager.TemperatureNotAvailable.INSTANCE);

        String deviceId = r.response.deviceId;
        repliesSoFar.put(deviceId, reading);
        stillWaiting.remove(deviceId);

        return respondWhenAllCollected();
    }

    /**
     * 响应行为：actor停止
     *
     * @param terminated DeviceTerminated
     * @return Behavior
     */
    private Behavior<Command> onDeviceTerminated(DeviceTerminated terminated) {
        if (stillWaiting.contains(terminated.deviceId)) {
            repliesSoFar.put(terminated.deviceId, DeviceManager.DeviceNotAvailable.INSTANCE);
            stillWaiting.remove(terminated.deviceId);
        }
        return respondWhenAllCollected();
    }

    /**
     * 响应行为：actor超时
     *
     * @param timeout 超时消息
     * @return Behavior
     */
    private Behavior<Command> onCollectionTimeout(CollectionTimeout timeout) {
        for (String deviceId : stillWaiting) {
            repliesSoFar.put(deviceId, DeviceManager.DeviceTimedOut.INSTANCE);
        }
        stillWaiting.clear();
        return respondWhenAllCollected();
    }

    /**
     * repliesSoFar的map中记录新结果，并将actor从静止等待中删除。
     *
     * 下一步是检查是否还有我们正在等待的Actor :  stillWaiting.isEmpty()
     * 如果没有，我们将查询结果发送给原始请求者，并停止查询actor。
     * 否则，我们需要更新repliesSoFar和stillWaiting结构并等待更多消息
     *
     * @return Behavior
     */
    private Behavior<Command> respondWhenAllCollected() {
        if (stillWaiting.isEmpty()) {
            requester.tell(new DeviceManager.RespondAllTemperatures(requestId, repliesSoFar));
            return Behaviors.stopped();
        } else {
            return this;
        }
    }

}