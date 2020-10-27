package com.tcfuture.akka.actor.fireandforget;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 *
 * fire and forget Example
 *
 * Tell是异步的，这意味着该方法立即返回。语句执行后，不能保证接收方已经处理了消息。它还意味着无法知道消
 * 息是否被接收、处理是否成功。
 *
 * 有用的时候：
 * 确保消息已被处理并不重要
 * 没有办法对不成功的交付或处理采取行动
 * 我们希望最小化创建的消息数量，以获得更高的吞吐量(发送响应将需要创建两倍于此的消息数量)
 *
 * 存在问题：
 * 如果消息的流入超过了actor的处理能力，那么收件箱将被填满，在最坏的情况下会导致JVM崩溃，并产生OutOfMemoryError错误
 * 如果消息丢失，发送者将不知道
 *
 * 给定协议和actor行为
 */
public class Printer {

    /**
     * 打印信息
     */
    public static class PrintMe {
        public final String message;

        public PrintMe(String message) {
            this.message = message;
        }
    }

    /**
     * 从构建器的当前状态 构建行为
     * @return
     */
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
