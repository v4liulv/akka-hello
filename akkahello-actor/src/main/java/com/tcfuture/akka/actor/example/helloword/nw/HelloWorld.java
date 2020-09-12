package com.tcfuture.akka.actor.example.helloword.nw;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author liulv
 *
 * 定义第一个Actor，它会说hello
 *
 * 1. 用于命令Actor向某人打招呼
 * 2. 用于Actor确认它已经这样做了
 *
 * Greet类型不仅包含要打招呼的对象的信息，它还包含消息发送方提供的ActorRef，以便HelloWorld
 * Actor可以发送回确认消息。
 *
 *
 */
public class HelloWorld extends AbstractBehavior<HelloWorld.Greet> {

    public static final class Greet {
        public final String whom;
        public final ActorRef<Greeted> replyTo;

        public Greet(String whom, ActorRef<Greeted> replyTo) {
            this.whom = whom;
            this.replyTo = replyTo;
        }
    }

    public static final class Greeted {
        public final String whom;
        public final ActorRef<Greet> from;

        public Greeted(String whom, ActorRef<Greet> from) {
            this.whom = whom;
            this.from = from;
        }
    }

    public static Behavior<Greet> create() {
        return Behaviors.setup(HelloWorld::new);
    }

    private HelloWorld(ActorContext<Greet> context) {
        super(context);
    }

    @Override
    public Receive<Greet> createReceive() {
        return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
    }

    private Behavior<Greet> onGreet(Greet command) {
        getContext().getLog().info("Hello {}!", command.whom);
        command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
        return this;
    }
}
