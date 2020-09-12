package com.tcfuture.akka.actor.example.wcr.objstyle;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

/**
 * @author liulv
 *
 * 最初，客户端Actor只能访问允许他们进行第一步的ActorRef<GetSession>。一旦建立了客户端的会话，它就会得到一
 * 个包含句柄的SessionGranted消息，该句柄用于解锁下一个协议步骤，即发布消息。需要将PostMessage命令发送到这
 * 个表示已添加到聊天室的会话的特定地址。会话的另一个方面是客户端通过replyTo参数显示了它自己的地址，以便后续的
 * messagepost事件可以发送给它。
 *
 *
 */
public class Gabbler extends AbstractBehavior<ChatRoom.SessionEvent> {
    public static Behavior<ChatRoom.SessionEvent> create() {
        return Behaviors.setup(Gabbler::new);
    }

    private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
        super(context);
    }

    @Override
    public Receive<ChatRoom.SessionEvent> createReceive() {
        ReceiveBuilder<ChatRoom.SessionEvent> builder = newReceiveBuilder();
        return builder
                .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
                .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
                .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
                .build();
    }

    private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
        getContext().getLog().info("cannot start chat room session: {}", message.reason);
        return Behaviors.stopped();
    }

    /**
     * 第三步：收到SessionGranted消息后进行回复
     * 1. 给SessionGranted中handle，也就是SessionBehavior 发送PostMessage消息，消息内容为"Hello World!"
     *
     * @param message SessionGranted
     * @return BehaviorImpl.same
     */
    private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
        message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
        return Behaviors.same();
    }

    /**
     * 第七步：收到的Session行为Actor发送的MessagePosted消息后，回复
     * 1. 打印收到的消息屏名称和信息字符串
     * 2. 终止回话
     *
     * @param message MessagePosted
     * @return Behavior从消息处理返回此行为，以表示此actor自愿终止。如果该actor已经创建了子actor，则这些将被停
     * 止作为关机过程的一部分。
     */
    private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
        getContext()
                .getLog()
                .info("message has been posted by '{}': {}", message.screenName, message.message);
        return Behaviors.stopped();
    }
}
