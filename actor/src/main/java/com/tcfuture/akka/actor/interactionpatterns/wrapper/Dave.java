package com.tcfuture.akka.actor.interactionpatterns.wrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.pattern.StatusReply;

import java.time.Duration;

/**
* @author liulv
*
* 响应包装StatusReply：
*
* 在许多情况下，响应可以是一个成功的结果，也可以是一个错误(例如，命令无效的验证错误)。必须为每个请求类型定义两个响应类
* 和一个共享超类型，这可能是重复的，特别是在集群上下文中，您还必须确保消息可以序列化以通过网络发送。
*
* 为了解决这个问题使用，使用通用的响应状态类型-StatusReply，并使用context.askWithStatus假设响应是StatusReply，
* 它将打开成功的响应并帮助处理验证错误。
 *
 * 核心代码：context.askWithStatus
*
*/
public class Dave extends AbstractBehavior<Dave.Command> {

    public interface Command {}

    // this is a part of the protocol that is internal to the actor itself
    private static final class AdaptedResponse implements Dave.Command {
        public final String message;

        public AdaptedResponse(String message) {
            this.message = message;
        }
    }

    public static Behavior<Dave.Command> create(ActorRef<Hal.Command> hal) {
        return Behaviors.setup(context -> new Dave(context, hal));
    }

    private Dave(ActorContext<Command> context, ActorRef<Hal.Command> hal) {
        super(context);

        // asking someone requires a timeout, if the timeout hits without response
        // the ask is failed with a TimeoutException
        final Duration timeout = Duration.ofSeconds(3);

        //核心代码
        context.askWithStatus(
                String.class,
                hal,
                timeout,
                // construct the outgoing message
                (ActorRef<StatusReply<String>> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
                // adapt the response (or failure to respond)
                (response, throwable) -> {
                    if (response != null) {
                        // a ReponseWithStatus.success(m) is unwrapped and passed as response
                        return new Dave.AdaptedResponse(response);
                    } else {
                        // a ResponseWithStatus.error will end up as a StatusReply.ErrorMessage()
                        // exception here
                        return new Dave.AdaptedResponse("Request failed: " + throwable.getMessage());
                    }
                });
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                // the adapted message ends up being processed like any other
                // message sent to the actor
                .onMessage(Dave.AdaptedResponse.class, this::onAdaptedResponse)
                .build();
    }

    private Behavior<Command> onAdaptedResponse(Dave.AdaptedResponse response) {
        getContext().getLog().info("Got response from HAL: {}", response.message);
        return this;
    }
}
