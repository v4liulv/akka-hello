package com.tcfuture.akka.cluster.statsclusterdy;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author liulv
 *
 * 用于中转请求、存储workers信息、进行中间处理（单词切分）等
 */
public class StatsService extends AbstractBehavior<Message.CommandService> {

    /**
     * 缓存ActorRef<Message.Process> workers
     */
    private final List<ActorRef<Message.Process>> workers =  new ArrayList<>();

    private int jobCounter = 0;

    /**
     * Actor构造器
     *
     * @param context ActorContext
     */
    public StatsService(ActorContext<Message.CommandService> context) {
        super(context);

        //向集群订阅worker消息，使用消息触发器将订阅的消息存储打破WorkersUpdated参数，然后自己会获取到此WorkersUpdated消息
        ActorRef<Receptionist.Listing> subscriptionAdapter =
                context.messageAdapter(Receptionist.Listing.class, listing ->
                        new Message.WorkersUpdated(listing.getServiceInstances(StatsWorker.WORKER_SERVICE_KEY)));
        context.getSystem().receptionist().tell(Receptionist.subscribe(StatsWorker.WORKER_SERVICE_KEY,
                subscriptionAdapter));
    }

    /**
     * 创建StatsService actor
     *
     * @return Behavior<Message.CommandService>
     */
    public static Behavior<Message.CommandService> create(){
        return Behaviors.setup(StatsService::new);
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
                .onMessage(Message.WorkersUpdated.class, this::workersUpdated)
                .onMessageEquals(Message.Stop.INSTANCE, () -> Behaviors.stopped())
                .build();
    }

    private Behavior<Message.CommandService> process(Message.ProcessText command) {
        getContext().getLog().info("委托请求");
        //文本中获取单词
        List<String> words = Arrays.asList(command.text.split(" "));

        //选举worker
        ActorRef<Message.Process> selectedWorker = workers.get(jobCounter % workers.size());
        workers.stream().forEach(worker -> getContext().getLog().info(worker.toString()));

        //创建一个子的actor
        getContext().spawnAnonymous(StatsAggregator.create(words, selectedWorker, command.replyTo));

        jobCounter ++;
        return this;
    }

    /**
     * 新增
     *
     * @param event Message.WorkersUpdated
     * @return
     */
    private Behavior<Message.CommandService> workersUpdated(Message.WorkersUpdated event) {
        //workers.clear();
        workers.addAll(event.newWorkers);
        getContext().getLog().info("向接待员注册的服务列表已更改: {}", event.newWorkers);
        return this;
    }
}
