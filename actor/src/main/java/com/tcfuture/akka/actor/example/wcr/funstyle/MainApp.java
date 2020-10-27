package com.tcfuture.akka.actor.example.wcr.funstyle;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 *
 * 考虑一个运行聊天室的 Actor:客户端 Actor可以通过发送一条包含他们的屏幕名的消息来连接，然后他们可以发布消息。聊天室 Actor将把所有发布的消息发送给所有当前连接的客户端 Actor。
 *
 * 本示例演示以下模式：
 * 1. 使用接口和实现该接口的类来表示 Acotor 可以接收的多个消息
 * 2. 通过使用子 actors 处理会话
 * 3. 通过改变行为来处理状态
 * 4. 使用多个 actors 以类型安全的方式表示协议的不同部分
 */
public class MainApp {
    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
                    ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
                    context.watch(gabbler);
                    chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(MainApp.create(), "ChatRoomDemo");
    }
}
