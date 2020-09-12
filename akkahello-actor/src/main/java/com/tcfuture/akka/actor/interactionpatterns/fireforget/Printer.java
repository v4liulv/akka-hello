package com.tcfuture.akka.actor.interactionpatterns.fireforget;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 *
 * 与actor交互的基本方法是通过actorRef.tell(message)。使用tell发送消息可以从任何线程安全地完成。
 *
 * Tell是异步的，这意味着该方法立即返回。
 * 语句执行后，不能保证接收方已经处理了消息。
 * 它还意味着无法知道消息是否被接收、处理是否成功。
 */
public class Printer {
    public static class PrintMe {
        public final String message;

        public PrintMe(String message) {
            this.message = message;
        }
    }

    public static Behavior<PrintMe> create() {
        return Behaviors.setup(
                context ->
                        Behaviors.receive(PrintMe.class)
                                .onMessage(
                                        PrintMe.class,
                                        printMe -> {
                                            context.getLog().info(printMe.message);
                                            return Behaviors.same();
                                        })
                                .build());
    }
}
