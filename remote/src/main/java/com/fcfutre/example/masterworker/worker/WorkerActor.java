package com.fcfutre.example.masterworker.worker;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;import com.fcfutre.example.masterworker.utils.PhisicUtils;

public class WorkerActor extends AbstractActor {

    private LoggingAdapter logger = Logging.getLogger(getContext().system(), this);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    logger.info("get msg [" + msg + "] from " + getSender() + " by " + getContext().getSelf());
                    // 反馈结果给printerActor，即这里的getSender()
                    getSender().tell(getWorkerInfo().toString(), getSelf());
                }).build();
    }

    private WorkerInfo getWorkerInfo() {
        return new WorkerInfo(
                PhisicUtils.getLocalIP(),
                PhisicUtils.getCpuUsage(),
                PhisicUtils.getMemoryUsage());
    }
}
