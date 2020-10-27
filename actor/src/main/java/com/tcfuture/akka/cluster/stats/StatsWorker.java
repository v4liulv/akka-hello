package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 */
public class StatsWorker extends AbstractBehavior<Message.CommandWorker> {

    private final Map<String, Integer> cache = new HashMap<String, Integer>();

    public StatsWorker(ActorContext<Message.CommandWorker> context) {
        super(context);
    }

    /**
     * 创建Behavior Actor, 并且定时每30秒发送Message.EvictCache.INSTANCE消息
     * @return
     */
    public static Behavior<Message.CommandWorker> create() {
        return Behaviors.setup(context ->
                Behaviors.withTimers(timers -> {
                    context.getLog().info("Worker starting up");
                    timers.startTimerWithFixedDelay(Message.EvictCache.INSTANCE, Message.EvictCache.INSTANCE, Duration.ofSeconds(30));

                    return new StatsWorker(context);
                })
        );
    }

    /**
     * 处理消息：
     * 1. Message.EvictCache.INSTANCE 清理缓存
     * 2. Message.Process, 把单词和长度存入缓存中，并回复当前缓存中单词的Message.Processed消息
     *
     * @return 构建新的Receive<Message.CommandWorker> actor
     */
    @Override
    public Receive<Message.CommandWorker> createReceive() {
        return newReceiveBuilder()
                .onMessageEquals(Message.EvictCache.INSTANCE, this::evictCache)
                .onMessage(Message.Process.class, this::process)
                .build();
    }

    /**
     * 处理Message.Process消息
     * 判断word是否已经存在缓存中，如果不存在，存入当前单词的长度和单词到缓存中
     *
     * 回复消息当前单词和缓存中的长度
     *
     * @param command Process 接收的消息
     * @return 当前actor
     */
    private Behavior<Message.CommandWorker> process(Message.Process command) {
        getContext().getLog().info("Worker 处理请求 [{}]", command.word);
        if (!cache.containsKey(command.word)) {
            int length = command.word.length();
            cache.put(command.word, length);
        }
        command.replyTo.tell(new Message.Processed(command.word, cache.get(command.word)));
        return this;
    }

    /**
     * 清楚缓存cache
     *
     * @return 返回当前actor
     */
    private Behavior<Message.CommandWorker> evictCache() {
        cache.clear();
        return this;
    }

}
