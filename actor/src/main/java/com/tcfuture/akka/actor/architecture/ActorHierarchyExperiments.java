package com.tcfuture.akka.actor.architecture;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author liulv
 *
 * 查看actor层次结构的最简单方法是打印ActorRef实例。
 * 在这个小实验中，我们创建一个actor，打印它的引用，创建这个actor的子元素，然后打印子元素的引用
 */
class PrintMyActorRefActor extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(PrintMyActorRefActor::new);
    }

    private PrintMyActorRefActor(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("printit", this::printIt).build();
    }

    private Behavior<String> printIt() {
        ActorRef<String> secondRef = getContext().spawn(Behaviors.empty(), "second-actor");
        System.out.println("Second: " + secondRef);
        return this;
    }
}

class Main1 extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(Main1::new);
    }

    private Main1(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("start", this::start).build();
    }

    private Behavior<String> start() {
        ActorRef<String> firstRef = getContext().spawn(PrintMyActorRefActor.create(), "first-actor");

        System.out.println("First: " + firstRef);
        firstRef.tell("printit");
        return Behaviors.same();
    }
}

public class ActorHierarchyExperiments {
    public static void main(String[] args) {
        ActorRef<String> testSystem = ActorSystem.create(Main1.create(), "testSystem");
        testSystem.tell("start");
    }
}
