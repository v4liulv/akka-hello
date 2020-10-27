package com.tcfuture.akka.cluster.statsclusterdy;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import com.tcfuture.akka.cluster.util.WordUtils;

import java.time.Duration;

/**
 * @author liulv
 *
 * 客户端 actor: 用于定时发送请求和接收最终处理结果，打印最新处理结果
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

                    //消息适配器，接收到的是Message.Response类型消息，其实最终接收的消息是Message.ServiceResponse消息
                    ActorRef<Message.Response> responseAdapter =
                            context.messageAdapter(Message.Response.class, Message.ServiceResponse::new);

                    return Behaviors.receive(Message.Event.class)
                            .onMessageEquals(Message.Tick.INSTANCE, () -> {
                                context.getLog().info("发送 process 请求");
                                service.tell(new Message.ProcessText(WordUtils.createText(8), responseAdapter));
                                return Behaviors.same();
                            })
                            .onMessage(Message.ServiceResponse.class, response -> {
                                context.getLog().info("处理后结果: {}", response.result);
                                return Behaviors.same();
                            }).build();
                })
        );
    }
}
