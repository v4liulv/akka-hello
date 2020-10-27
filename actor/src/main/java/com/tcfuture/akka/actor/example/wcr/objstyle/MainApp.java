package com.tcfuture.akka.actor.example.wcr.objstyle;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 */
public class MainApp {
    public static Behavior<Void> create() {
        return Behaviors.setup(
                context -> {
                    //创建聊天室Actor行为
                    ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
                    //创建客户端Gabbler Actor
                    ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
                    ActorRef<ChatRoom.SessionEvent> gabbler01 = context.spawn(Gabbler.create(),
                            "gabbler01");
                    //观察一个特定的ActorRef，并在actor终止后发出下游失败的信号。有信号的失败将是一个WatchedActorTerminatedException。
                    context.watch(gabbler);
                    context.watch(gabbler01);
                    //第一步：给聊天室发送获取回话命令，窗口名为ol’ Gabbler, 回话事件对象为 Gabbler
                    chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

                    chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler01", gabbler01));

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                });
    }

    public static void main(String[] args) {
        ActorSystem.create(MainApp.create(), "ChatRoomDemo");
    }
}