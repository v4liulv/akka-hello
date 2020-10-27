package com.tcfuture.akka.cluster.transformation;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author liulv
 */
public class Frontend extends AbstractBehavior<Massage.Event> {

    private final List<ActorRef<Massage.TransformText>> workers = new ArrayList<>();
    private int jobCounter = 0;

    public static Behavior<Massage.Event> create() {
        return Behaviors.setup(context ->
                Behaviors.withTimers(timers ->
                        new Frontend(context, timers)
                )
        );
    }

    public Frontend(ActorContext<Massage.Event> context,
                    TimerScheduler<Massage.Event> timers) {
        super(context);

        //向集群订阅worker消息，使用消息触发器将订阅的消息存储打破WorkersUpdated参数，然后自己会获取到此WorkersUpdated消息
        ActorRef<Receptionist.Listing> subscriptionAdapter =
                context.messageAdapter(Receptionist.Listing.class, listing ->
                    new Massage.WorkersUpdated(listing.getServiceInstances(Worker.WORKER_SERVICE_KEY)));
        context.getSystem().receptionist().tell(Receptionist.subscribe(Worker.WORKER_SERVICE_KEY,
                subscriptionAdapter));

        //每隔2秒延时发送一个Tick.INSTANCE消息给自己
        timers.startTimerWithFixedDelay(Massage.Tick.INSTANCE,
                Massage.Tick.INSTANCE, Duration.ofSeconds(2));
    }

    @Override
    public Receive<Massage.Event> createReceive() {
        return newReceiveBuilder()
                //接收receptionist订阅的消息WorkersUpdated
                .onMessage(Massage.WorkersUpdated.class, this::onWorkersUpdated)
                //接收定时触发钩子消息
                .onMessageEquals(Massage.Tick.INSTANCE, this::onTick)
                //成功接送到work消息后处理
                .onMessage(Massage.TransformCompleted.class, this::onTransformCompleted)
                //没接收到work信息失败处理
                .onMessage(Massage.JobFailed.class, this::onJobFailed)
                .build();
    }

    /**
     * 处理Tick.INSTANCE定时任务请求
     *
     * 判断workers ，无worker的话， 提示 ：有tick请求，但没有可用的worker，不发送任何工作
     *
     * @return Behavior<FrontedMassage.Event>
     */
    private Behavior<Massage.Event> onTick() {
        if (workers.isEmpty()) {
            getContext().getLog().warn("有tick请求，但没有可用的worker，不发送任何工作");
        } else {
            //ask请求超时时间，设置5秒
            Duration timeout = Duration.ofSeconds(5);
            //获取worker actor
            ActorRef<Massage.TransformText> selectedWorker = workers.get(jobCounter % workers.size());
            getContext().getLog().info("将work发送到 {}", selectedWorker);

            //文本信息
            String text = "hello-" + jobCounter;

            getContext().ask(
                    Massage.TextTransformed.class,
                    selectedWorker,
                    timeout,
                    responseRef -> new Massage.TransformText(text, responseRef),
                    (response, failure) -> {
                        //如果ask响应不为空，则成功发送TransformCompleted消息，如果无响应则认为失败JobFailed消息
                        if (response != null) {
                            return new Massage.TransformCompleted(text, response.text);
                        } else {
                            return new Massage.JobFailed("Processing timed out", text);
                        }
                    }
            );

            jobCounter++;
        }
        return this;
    }

    /**
     * 处理向receptionist定义的WorkersUpdated消息
     *
     * @param event
     * @return
     */
    private Behavior<Massage.Event> onWorkersUpdated(Massage.WorkersUpdated event) {
        workers.clear();
        workers.addAll(event.newWorkers);
        getContext().getLog().info("向接待员注册的服务列表已更改: {}", event.newWorkers);
        return this;
    }

    private Behavior<Massage.Event> onTransformCompleted (Massage.TransformCompleted event) {
        getContext().getLog().info("转换任务完成了 {}: {}", event.originalText, event.transformedText);
        return this;
    }

    private Behavior<Massage.Event> onJobFailed(Massage.JobFailed event) {
        getContext().getLog().info("转换任务失败了 {}, 失败原因：{}", event.text, event.why);
        return this;
    }


}
