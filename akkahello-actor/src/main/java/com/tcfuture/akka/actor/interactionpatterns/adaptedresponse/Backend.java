package com.tcfuture.akka.actor.interactionpatterns.adaptedresponse;

import akka.actor.typed.ActorRef;

import java.net.URI;

/**
 * @author liulv
 */
public class Backend {
    public interface Request {}

    /**
     * 运行转换job
     */
    public static class StartTranslationJob implements Request {
        public final int taskId;
        public final URI site;
        public final ActorRef<Response> replyTo;

        public StartTranslationJob(int taskId, URI site, ActorRef<Response> replyTo) {
            this.taskId = taskId;
            this.site = site;
            this.replyTo = replyTo;
        }
    }

    public interface Response {}

    /**
     * job启动
     */
    public static class JobStarted implements Response {
        public final int taskId;

        public JobStarted(int taskId) {
            this.taskId = taskId;
        }
    }

    /**
     * job运行中
     */
    public static class JobProgress implements Response {
        public final int taskId;
        public final double progress;

        public JobProgress(int taskId, double progress) {
            this.taskId = taskId;
            this.progress = progress;
        }
    }

    /**
     * job完成
     */
    public static class JobCompleted implements Response {
        public final int taskId;
        public final URI result;

        public JobCompleted(int taskId, URI result) {
            this.taskId = taskId;
            this.result = result;
        }
    }
}

