package com.tcfuture.akka.actor.architecture;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author liulv
 *
 * 父母和孩子在整个生命周期中都是相互联系的。每当一个参与者失败时(抛出一个异常或一个未处理的异常从接收中冒出)，失败信息就会传播到监督策略，然后由
 * 其决定如何处理由参与者引起的异常。
 * 监控策略通常由父actor在生成子actor时定义。这样，父母就成了孩子的监督者。默认的管理策略是阻止孩子。如果你不定义策略，所有的失败都会导致停止。
 *
 * 我们看到，在失败后，被监督的参与者被停止并立即重新启动。我们还会看到一个日志条目报告所处理的异常，在本例中是我们的测试异常。在本例中，我们还使
 * 用了PreRestart信号，该信号在重启之前被处理。
 */
public class FailureHandling extends AbstractBehavior<String>{

    static Behavior<String> create() {
        return Behaviors.setup(FailureHandling::new);
    }

    private FailureHandling(ActorContext<String> context) {
        super(context);
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("start", this::start).build();
    }

    private Behavior<String> start() {
        ActorRef<String> firstRef = getContext().spawn(SupervisingActor.create(), "supervising-actor");

        System.out.println("First: " + firstRef);
        firstRef.tell("failChild");
        return Behaviors.same();
    }

    public static void main(String[] args) {
        ActorRef<String> testSystem = ActorSystem.create(FailureHandling.create(), "ActorSystem");
        testSystem.tell("start");
    }
}

/**
 * 父actor 监督者
 */
class SupervisingActor extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(SupervisingActor::new);
    }

    private final ActorRef<String> child;

    private SupervisingActor(ActorContext<String> context) {
        super(context);
        child =
                context.spawn(
                        Behaviors.supervise(SupervisedActor.create()).onFailure(SupervisorStrategy.restart()),
                        "supervised-actor");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder().onMessageEquals("failChild", this::onFailChild).build();
    }

    private Behavior<String> onFailChild() {
        child.tell("fail");
        return this;
    }
}

/**
 * 子actor， 有监督的actor
 */
class SupervisedActor extends AbstractBehavior<String> {

    static Behavior<String> create() {
        return Behaviors.setup(SupervisedActor::new);
    }

    private SupervisedActor(ActorContext<String> context) {
        super(context);
        System.out.println("supervised actor started");
    }

    @Override
    public Receive<String> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals("fail", this::fail)
                .onSignal(PreRestart.class, signal -> preRestart())
                .onSignal(PostStop.class, signal -> postStop())
                .build();
    }

    private Behavior<String> fail() {
        System.out.println("supervised actor fails now");
        throw new RuntimeException("I failed!");
    }

    private Behavior<String> preRestart() {
        System.out.println("second will be restarted");
        return this;
    }

    private Behavior<String> postStop() {
        System.out.println("second stopped");
        return this;
    }
}
