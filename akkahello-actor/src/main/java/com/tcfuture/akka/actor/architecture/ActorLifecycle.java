package com.tcfuture.akka.actor.architecture;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author liulv
 *
 * actor生命周期：
 * actor在创建时出现，然后在用户请求时停止。当一个actor被停止时，它的所有子actor也会被递归地停止。此行为极大地简化了资源清理工作，
 * 并有助于避免资源泄漏，比如由于打开套接字和文件而导致的泄漏。事实上，在处理低级多线程代码时，一个经常被忽视的困难是各种并发资源的
 * 生命周期管理。
 *
 * 为了停止一个actor，推荐的模式是在actor内部返回Behaviors.stopped()来停止它自己，通常作为对一些用户定义的停止消息的响应，或
 * 者当actor完成它的工作时。从父节点调用context.stop(childRef)来停止子节点从技术上讲是可行的，但是不可能通过这种方式停止任意
 * 的(非子节点)节点。
 */
class StartStopActor1 extends AbstractBehavior<String>{
    static Behavior<String> create() {
        return Behaviors.setup(StartStopActor1::new);
    }

    private StartStopActor1(ActorContext<String> context) {
        super(context);
        System.out.println("first started");

        context.spawn(StartStopActor2.create(), "second");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("stop", Behaviors::stopped)
                .onSignal(PostStop.class, signal -> onPostStop())
                .build();
    }

    private Behavior<String> onPostStop() {
        System.out.println("first stopped");
        return this;
    }
}

class StartStopActor2 extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(StartStopActor2::new);
    }

    private StartStopActor2(ActorContext<String> context) {
        super(context);
        System.out.println("second started");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private Behavior<String> onPostStop() {
        System.out.println("second stopped");
        return this;
    }
}

class Main extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(Main::new);
    }

    private Main(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("start", this::start).build();
    }

    private Behavior<String> start() {
        ActorRef<String> firstRef = getContext().spawn(StartStopActor1.create(), "first");

        firstRef.tell("stop");
        return this;
    }
}

public class ActorLifecycle{
    public static void main(String[] args) {
        ActorRef<String> testSystem = ActorSystem.create(Main.create(), "testSystem");
        testSystem.tell("start");
    }

}


