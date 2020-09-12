package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

/**
 * @author liulv
 *
 * 设备参与者的任务很简单:
 * 1. 收集温度测量
 * 2. 当被问及时，报告最后测量的温度
 *
 * 然而，设备可能在没有立即测量温度的情况下启动。因此，我们需要考虑温度不存在的情况。这还允许我们在没有写入部分的情况下测试actor的查询部分，因为
 * 设备actor可以报告一个空结果。
 *
 * 从设备actor获得当前温度的协议很简单。
 * 1. 等待对当前温度的请求。
 * 2. 用一个回复响应请求，该回复要么包含当前温度，要么表示温度还不可用。
 *
 * 我们需要两条消息，一条用于请求，一条用于应答。我们的第一次尝试可能是这样的:
 */
@SuppressWarnings("ALL")
public class Device extends AbstractBehavior<Device.Command> {

    /**
     * 命令接口
     */
    public interface Command {}

    /**
     * 写协议：
     *
     * actor收到来自传感器的信息时，它需要一种方法来改变温度的状态
     * 写协议的目的是在actor接收到包含温度的消息时更新currentTemperature字段。同样，我们很容易将write协议定义为一个非常简单的消息.
     *
     * 这种方法没有考虑到记录温度消息的发送者永远不能确定该消息是否已被处理。我们已经看到，Akka并不保证这些消息的传递，而是让应用程序
     * 提供成功通知。在我们的情况下，一旦我们更新了上次的温度记录，我们就会向发送者发送确认信息，例如，回复一条温度恢复记录信息。
     * 与温度查询和响应一样，包含ID字段以提供最大的灵活性也是一个好主意。
     */
    public static final class RecordTemperature implements Device.Command {
        final long requestId;
        final double value;
        final ActorRef<TemperatureRecorded> replyTo;

        public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
            this.requestId = requestId;
            this.value = value;
            this.replyTo = replyTo;
        }
    }

    /**
     * 温度记录类
     */
    public static final class TemperatureRecorded {
        final long requestId;

        public TemperatureRecorded(long requestId) {
            this.requestId = requestId;
        }
    }

    /**
     * 回复一条温度记录信息。与温度查询和响应一样，包含ID字段以提供最大的灵活性。
     *
     * @param r RecordTemperature 记录温度
     * @return
     */
    private Behavior<Command> onRecordTemperature(RecordTemperature r) {
        getContext().getLog().info("Recorded temperature reading {} with {}", r.value, r.requestId);
        lastTemperatureReading = Optional.of(r.value);
        r.replyTo.tell(new TemperatureRecorded(r.requestId));
        return this;
    }

    /**
     * 读取温度类
     */
    public static final class ReadTemperature implements Command {
        /**
         * 如果我们想在查询设备参与者(因为超时请求)的参与者中实现resends，或者如果我们想查询多个参与者，我们需要能够关联请求和响应。
         * 因此，我们在我们的消息中增加了一个字段，以便请求者提供一个ID(我们将在后面的步骤中添加这个代码到我们的应用中):
         */
        final long requestId;
         /**
         * 回复消息
         */
        final ActorRef<RespondTemperature> replyTo;

        public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo ) {
            this.replyTo = replyTo;
            this.requestId = requestId;
        }
    }

    /**
     * 报告当前温度类
     */
    public static final class RespondTemperature {
        /**
         * 如果我们想在查询设备参与者(因为超时请求)的参与者中实现resends，或者如果我们想查询多个参与者，我们需要能够关联请求和响应。
         * 因此，我们在我们的消息中增加了一个字段，以便请求者提供一个ID(我们将在后面的步骤中添加这个代码到我们的应用中):
         */
        final long requestId;
        final String deviceId;
        final Optional<Double> value;

        public RespondTemperature(long requestId, String deviceId, Optional<Double> value){
            this.value = value;
            this.deviceId = deviceId;
            this.requestId = requestId;
        }
    }

    /**
     * 要从我们的测试用例外部停止设备actor，我们必须向它发送一条消息。我们添加了一个Passivate钝化消息，指示参与者停止。
     * 一旦设备参与者停止，就会收到通知。我们也可以用死亡看护来做这个
     */
    static enum Passivate implements Command {
        INSTANCE
    }

    /**
     *
     * @param groupId
     * @param deviceId
     * @return
     */
    public static Behavior<Command> create(String groupId, String deviceId) {
        return Behaviors.setup(context -> new Device(context, groupId, deviceId));
    }
    /**
     * 实现设备actor及其读取协议:
     *   每个actor都定义了它将接受的消息类型。我们的设备参与者有责任为给定查询的响应使用相同的ID参数。
     *
     *  groupId: 分组ID
     *  deviceId：设备ID
     */
    private final String groupId;
    private final String deviceId;

    /**
     * 最后读取到的温度值
     */
    private Optional<Double> lastTemperatureReading = Optional.empty();

    private Device(ActorContext<Command> context, String groupId, String deviceId) {
        super(context);
        this.groupId = groupId;
        this.deviceId = deviceId;

        context.getLog().info("Device actor {}-{} started", groupId, deviceId);
    }

    /**
     * 如果接受到消息类型为ReadTemperature，则调用应用处理程序操作onReadTemperature方法.
     * 消息发生后调用停止掉该actor(onSignal(PostStop.class, signal -> onPostStop())
     *
     * @return 使用消息匹配构建器实现的专门的“接收”行为，
     */
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(ReadTemperature.class, this::onReadTemperature)
                .onMessage(RecordTemperature.class, this::onRecordTemperature) //
                .onMessage(Passivate.class, m -> Behaviors.stopped()) //发现了消息Passivate停止Actor
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    /**
     * 把lastTemperatureReading最后读取到的温度信息回复给对应请求者（requestId）
     *
     * @param r 读取到的温度实例
     * @return 返回
     */
    private Behavior<Command> onReadTemperature(ReadTemperature r) {
        getContext().getLog().info("Reading temperature read {} with {}", lastTemperatureReading, r.requestId);
        r.replyTo.tell(new RespondTemperature(r.requestId, deviceId, lastTemperatureReading));
        return this;
    }

    private Device onPostStop() {
        getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
        return this;
    }

}



