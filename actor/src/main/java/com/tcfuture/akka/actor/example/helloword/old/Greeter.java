package com.tcfuture.akka.actor.example.helloword.old;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * @author liulv
 *
 * 包含问候语的消息
 *
 * Greeter类扩展了akka.actor.AbstractActor类并实现了createReceive方法。
 */
public class Greeter extends AbstractActor {
    /**
     * 静态props方法创建并返回Props实例。Props是一个配置类，用于指定创建 Actor 的选项，将其视为不可变的，因此可以自由共享用于创建
     * 包含相关部署信息的 Actor 的方法。这个例子简单地传递了 Actor 在构造时需要的参数。我们将在本教程的后面部分看到props方法的实际应用。
     *
     * @param message 消息
     * @param printerActor 打印Actor
     * @return Props
     */
    static public Props props(String message, ActorRef printerActor) {
        return Props.create(Greeter.class, () -> new Greeter(message, printerActor));
    }

    //问候消息的接受者
    static public class WhoToGreet {
        public final String who;

        public WhoToGreet(String who) {
            this.who = who;
        }
    }

    //执行问候的指令
    static public class Greet {
        public Greet() {
        }
    }

    /**
     * Greeter构造函数接受两个参数：String message，它将在构建问候语时使用，
     * ActorRef printerActor是处理问候语输出的 Actor 的引用。
     */
    private final String message;
    private final ActorRef printerActor;
    //greeting变量包含 Actor 的状态，默认设置为""
    private String greeting = "";

    public Greeter(String message, ActorRef printerActor) {
        this.message = message;
        this.printerActor = printerActor;
    }

    @Override
    public Receive createReceive() {
        /**
         * receiveBuilder定义了行为；Actor 应该如何响应它接收到的不同消息。Actor 可以有状态。访问或改变 Actor 的内部状态
         * 是线程安全的，因为它受 Actor 模型的保护。createReceive方法应处理 Actor 期望的消息。对于Greeter，它需要两种类
         * 型的消息：WhoToGreet和Greet，前者将更新 Actor 的问候语状态，后者将触发向Printer Actor发送问候语。
         */
        return receiveBuilder()
                .match(WhoToGreet.class, wtg -> {
                    this.greeting = message + ", " + wtg.who;
                })
                .match(Greet.class, x -> {
                    printerActor.tell(new Printer.Greeting(greeting), getSelf());
                })
                .build();
    }
}
