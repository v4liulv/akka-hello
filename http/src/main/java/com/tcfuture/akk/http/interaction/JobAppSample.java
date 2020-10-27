package com.tcfuture.akk.http.interaction;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.BehaviorBuilder;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.Route;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * 依赖JobRoutes、JobRepository
 *
 * 最后，我们创建一个引导Web服务器并使用它作为actor系统的根行为：Behavior
 *
 * @author liulv
 */
public class JobAppSample {

    interface Message {}

    /**
     * 启动失败消息
     */
    private static final class StartFailed implements Message {
        final Throwable ex;

        public StartFailed(Throwable ex) {
            this.ex = ex;
        }
    }

    /**
     * 已启动
     */
    private static final class Started implements Message {
        final ServerBinding binding;

        public Started(ServerBinding binding) {
            this.binding = binding;
        }
    }

    private static final class Stop implements Message {}

    /**
     * 创建root Actor - ActorSystem
     * 并创建子JobRepository、JobRoutes构建Route
     *
     * @param host http的ip地址
     * @param port http的端口
     * @return Behavior<Message>
     */
    public static Behavior<Message> create(String host, Integer port) {
        return Behaviors.setup(context -> {
            ActorSystem<Void> system = context.getSystem();
            ActorRef<JobRepository.Command> buildJobRepository = context.spawn(JobRepository.create(), "JobRepository");
            Route routes = new JobRoutes(buildJobRepository, context.getSystem()).jobRoutes();

            CompletionStage<ServerBinding> serverBinding =
                    Http.get(system).newServerAt(host, port).bind(routes);

            //修改后，将给定的第一个参数CompletionStage的结果发送给该Actor（“self”）给定的功能。
            context.pipeToSelf(serverBinding, (binding, failure) -> {
                if (binding != null) return new Started(binding);
                else return new StartFailed(failure);
            });

            return starting(false);
        });
    }

    /**
     * 启动Actor方法，接收各种消息并最终调用自己或running方法创建Behavior，其最终都是调用running方法创建
     * Behavior
     *
     * @param wasStopped 是否停止Actor，为true是停止actor暂时无意义，启动就马上停止有什么意义
     * @return Behavior<Message>
     */
    private static Behavior<Message> starting(boolean wasStopped) {
        return Behaviors.setup(context ->
                BehaviorBuilder.<Message>create()
                        .onMessage(StartFailed.class, failed -> {
                            throw new RuntimeException("服务器启动失败", failed.ex);
                        })
                        .onMessage(Started.class, msg -> {
                            context.getLog().info(
                                    "Server online at http://{}:{}",
                                    msg.binding.localAddress().getAddress(),
                                    msg.binding.localAddress().getPort());

                            if (wasStopped) context.getSelf().tell(new Stop());

                            return running(msg.binding);
                        })
                        .onMessage(Stop.class, s -> {
                            //我们收到了停止消息，但尚未完成开始，
                            //我们无法停止，直到开始完成
                            return starting(true);
                        })
                        .build());
    }

    /**
     * 最终创建Behavior Actor
     *
     * @param binding ServerBinding Http服务绑定信息
     * @return Behavior<Message>
     */
    private static Behavior<Message> running(ServerBinding binding) {
        return Behaviors.setup(context ->
                    BehaviorBuilder.<Message>create().onMessage(Stop.class, msg -> {
                    context.getLog().info("Behaviors 停止...");
                    return Behaviors.stopped();
                }).onSignal(PostStop.class, msg -> {
                    context.getLog().info("ServerBinding 解绑...");
                    binding.unbind();
                    return Behaviors.same();
                }).build()
        );
    }

    public static void main(String[] args) {
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", 27703);
        //overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("http_test"));


        ActorSystem<Message> system = ActorSystem.create(
                JobAppSample.create("localhost", 8080), "BuildJobsServer", config);
    }
}
