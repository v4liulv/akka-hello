package com.tcfuture.akka.actor.example.hello;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

/**
 * 监控的actor
 */
public class GreeterMain extends AbstractBehavior<GreeterMain.SayHello> {

    public static class SayHello {
        public final String name;

        public SayHello(String name) {
            this.name = name;
        }
    }

    private final ActorRef<Greeter.Greet> greeter;

    public static Behavior<SayHello> create() {
        return Behaviors.setup(GreeterMain::new);
    }

    private GreeterMain(ActorContext<SayHello> context) {
        super(context);
        //#create-actors
        //创建子Actor greeter
        greeter = context.spawn(Greeter.create(), "greeter");
        System.out.println(greeter);
        //#create-actors
    }

    @Override
    public Receive<SayHello> createReceive() {
        System.out.println("监护greeter接收到SayHello消息， 并调用onSayHello方法处理");
        return newReceiveBuilder()
                .onMessage(SayHello.class, this::onSayHello)
                .build();
    }

    private Behavior<SayHello> onSayHello(SayHello command) {
        System.out.println("onSayHello方法处理");
        //#create-actors
        //通过SayHello消息的名称Charles创建回复Actor
        ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.name);
        //监护greeter发送Greeter.Greet实例消息
        greeter.tell(new Greeter.Greet(command.name, replyTo));
        //#create-actors
        return this;
    }
}
