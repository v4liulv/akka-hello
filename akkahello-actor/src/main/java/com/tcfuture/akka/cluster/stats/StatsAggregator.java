package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liulv
 *
 * statsService 子actor:统计汇总器
 */
public class StatsAggregator extends AbstractBehavior<Message.Event> {

    private final int expectedResponses;
    private final ActorRef<Message.Response> replyTo;
    private final List<Integer> results = new ArrayList<>();

    public StatsAggregator(ActorContext<Message.Event> context, List<String> words,
                           ActorRef<Message.Process> workers,
                           ActorRef<Message.Response> replyTo) {
        super(context);
        this.expectedResponses = words.size();
        this.replyTo = replyTo;

        getContext().setReceiveTimeout(Duration.ofSeconds(3), Message.Timeout.INSTANCE);

        /**
         * 响应适配器- 接收到Processed消息，将并转换为响应CalculationComplete消息
         */
        ActorRef<Message.Processed> responseAdapter =
                getContext().messageAdapter(Message.Processed.class, processed ->
                        new Message.CalculationComplete(processed.length));

        /**
         * 循环每个单词发送消息给workers处理
         */
        words.stream().forEach(word -> workers.tell(new Message.Process(word, responseAdapter)));
    }

    public static Behavior<Message.Event> create(List<String> words,
                                                 ActorRef<Message.Process> workers,
                                                 ActorRef<Message.Response> replyTo) {
        return Behaviors.setup(context ->
                new StatsAggregator(context, words, workers, replyTo)
        );
    }

    @Override
    public Receive<Message.Event> createReceive() {
        return newReceiveBuilder()
                .onMessage(Message.CalculationComplete.class, this::onCalculationComplete)
                .onMessageEquals(Message.Timeout.INSTANCE, this::onTimeout)
                .build();
    }

    /**
     * 这么聚和计算, 通过获取每个单词完成计算，每个单词长度添加到缓存list results中.
     * 当缓存list results的size等于文本单词数时，进行聚和计算，并且发送到client actor
     *
     * @param event CalculationComplete
     * @return 当前actor或者停止当前子actor
     */
    private Behavior<Message.Event> onCalculationComplete(Message.CalculationComplete event) {
        results.add(event.length);
        if (results.size() == expectedResponses) {
            int sum = results.stream().mapToInt(i -> i).sum();
            double meanWordLength = ((double) sum) / results.size();
            replyTo.tell(new Message.JobResult(meanWordLength));
            return Behaviors.stopped();
        } else {
            return this;
        }
    }

    private Behavior<Message.Event> onTimeout() {
        replyTo.tell(new Message.JobFailed("Service unavailable, try again later"));
        return Behaviors.stopped();
    }

}
