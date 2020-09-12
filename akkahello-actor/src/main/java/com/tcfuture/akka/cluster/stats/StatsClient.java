package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

import java.time.Duration;

/**
 * @author liulv
 */
public class StatsClient {

    /**
     *  定时每2秒给自己发送INSTANCE消息
     *  接收定时INSTANCE消息，发送Message.ProcessText消息给service
     *  接收ServiceResponse消息，打印输出
     *
     * @param service ActorRef<Message.ProcessText>
     * @return Behavior<Event>
     */
    public static Behavior<Message.Event> create(ActorRef<Message.ProcessText> service) {
        return Behaviors.setup(context ->
                Behaviors.withTimers(timers -> {
                    //定时没2秒发送Tick.INSTANCE消息
                    timers.startTimerWithFixedDelay(Message.Tick.INSTANCE, Message.Tick.INSTANCE, Duration.ofSeconds(2));
                    ActorRef<Message.Response> responseAdapter =
                            context.messageAdapter(Message.Response.class, Message.ServiceResponse::new);

                    return Behaviors.receive(Message.Event.class)
                            .onMessageEquals(Message.Tick.INSTANCE, () -> {
                                context.getLog().info("Sending process request");
                                service.tell(new Message.ProcessText("this is the text that will be analyzed", responseAdapter));
                                return Behaviors.same();
                            })
                            .onMessage(Message.ServiceResponse.class, response -> {
                                context.getLog().info("Service result: {}", response.result);
                                return Behaviors.same();
                            }).build();
                })
        );
    }
}
