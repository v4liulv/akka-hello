package com.tcfuture.akka.actor.interactionpatterns.wrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

/**
 * @author liulv
 *
 * 响应包装：
 *
 * 在许多情况下，响应可以是一个成功的结果，也可以是一个错误(例如，命令无效的验证错误)。必须为每个请求类型定义两个响应类
 * 和一个共享超类型，这可能是重复的，特别是在集群上下文中，您还必须确保消息可以序列化以通过网络发送。
 *
 * 为了解决这个问题使用，使用通用的响应状态类型-StatusReply，并使用context.askWithStatus假设响应是StatusReply，
 * 它将打开成功的响应并帮助处理验证错误。
 *
 */
public class Hal extends AbstractBehavior<Hal.Command> {
    public interface Command {}

    public static Behavior<Command> create() {
        return Behaviors.setup(Hal::new);
    }

    private Hal(ActorContext<Command> context) {
        super(context);
    }

    public static final class OpenThePodBayDoorsPlease implements Hal.Command {
        public final ActorRef<StatusReply<String>> respondTo;

        public OpenThePodBayDoorsPlease(ActorRef<StatusReply<String>> respondTo) {
            this.respondTo = respondTo;
        }
    }
    @Override
    public Receive<Hal.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(Hal.OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
                .build();
    }

    private Behavior<Hal.Command> onOpenThePodBayDoorsPlease(
            Hal.OpenThePodBayDoorsPlease message) {
        message.respondTo.tell(StatusReply.error("I'm sorry, Dave. I'm afraid I can't do that."));
        return this;
    }
}


