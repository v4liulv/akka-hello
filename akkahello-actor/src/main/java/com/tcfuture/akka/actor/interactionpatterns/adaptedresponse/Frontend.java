package com.tcfuture.akka.actor.interactionpatterns.adaptedresponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 *
 * 前端
 */
public class Frontend {

    public interface Command {}

    /**
     * 转换
     */
    public static class Translate implements Command {
        public final URI site;
        public final ActorRef<URI> replyTo;

        public Translate(URI site, ActorRef<URI> replyTo) {
            this.site = site;
            this.replyTo = replyTo;
        }
    }

    /**
     * 包装后端响应
     */
    private static class WrappedBackendResponse implements Command {
        final Backend.Response response;

        public WrappedBackendResponse(Backend.Response response) {
            this.response = response;
        }
    }

    /**
     * 编译Actor
     */
    @SuppressWarnings("DanglingJavadoc")
    public static class Translator extends AbstractBehavior<Command> {
        //请求后端
        private final ActorRef<Backend.Request> backend;
        //后端响应适配
        private final ActorRef<Backend.Response> backendResponseAdapter;

        private int taskIdCounter = 0;
        private Map<Integer, ActorRef<URI>> inProgress = new HashMap<>();

        public Translator(ActorContext<Command> context, ActorRef<Backend.Request> backend) {
            super(context);
            this.backend = backend;
            /**
             *如果消息类与给定的类匹配，则将使用消息适配器或其子类。以相反的顺序尝试已注册的适配器
             * 其注册顺序，即最后先注册的。
             */
            this.backendResponseAdapter =
                    context.messageAdapter(Backend.Response.class, WrappedBackendResponse::new);
        }

        public static Behavior<Command> create(ActorRef<Backend.Request> backend) {
            return Behaviors.setup(context -> new Translator(context, backend));
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Translate.class, this::onTranslate)
                    .onMessage(WrappedBackendResponse.class, this::onWrappedBackendResponse)
                    .build();
        }

        private Behavior<Command> onTranslate(Translate cmd) {
            taskIdCounter += 1;
            inProgress.put(taskIdCounter, cmd.replyTo);
            backend.tell(
                    new Backend.StartTranslationJob(taskIdCounter, cmd.site, backendResponseAdapter));
            return this;
        }

        private Behavior<Command> onWrappedBackendResponse(WrappedBackendResponse wrapped) {
            Backend.Response response = wrapped.response;
            if (response instanceof Backend.JobStarted) {
                Backend.JobStarted rsp = (Backend.JobStarted) response;
                getContext().getLog().info("Started {}", rsp.taskId);
            } else if (response instanceof Backend.JobProgress) {
                Backend.JobProgress rsp = (Backend.JobProgress) response;
                getContext().getLog().info("Progress {}", rsp.taskId);
            } else if (response instanceof Backend.JobCompleted) {
                Backend.JobCompleted rsp = (Backend.JobCompleted) response;
                getContext().getLog().info("Completed {}", rsp.taskId);
                inProgress.get(rsp.taskId).tell(rsp.result);
                inProgress.remove(rsp.taskId);
            } else {
                return Behaviors.unhandled();
            }

            return this;
        }
    }
}
