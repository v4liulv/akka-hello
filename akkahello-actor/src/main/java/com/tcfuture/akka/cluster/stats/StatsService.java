package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Arrays;
import java.util.List;

/**
 * @author liulv
 */
public class StatsService extends AbstractBehavior<Message.CommandService> {

    /**
     * 缓存ActorRef<Message.Process> workers
     */
    private final ActorRef<Message.Process> workers;

    /**
     * Actor构造器
     *
     * @param context ActorContext
     * @param workers ActorRef<Message.Process>
     */
    public StatsService(ActorContext<Message.CommandService> context, ActorRef<Message.Process> workers) {
        super(context);

        this.workers = workers;
    }

    /**
     * 创建StatsService actor
     *
     * @param workers ActorRef<Message.Process>
     * @return Behavior<Message.CommandService>
     */
    public static Behavior<Message.CommandService> create(ActorRef<Message.Process> workers){
        return Behaviors.setup(context ->
            new StatsService(context, workers)
        );
    }

    /**
     * 接收ProcessText消息
     *
     * @return
     */
    @Override
    public Receive<Message.CommandService> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.ProcessText.class, this::process)
                .onMessageEquals(Message.Stop.INSTANCE, () -> Behaviors.stopped())
                .build();
    }

    private Behavior<Message.CommandService> process(Message.ProcessText command) {
        getContext().getLog().info("Delegating request");
        //文本中获取单词
        List<String> words = Arrays.asList(command.text.split(" "));
        //创建一个子的actor
        getContext().spawnAnonymous(StatsAggregator.create(words, workers, command.replyTo));

        return this;
    }
}
