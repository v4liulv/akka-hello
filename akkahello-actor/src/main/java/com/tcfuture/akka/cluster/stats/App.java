package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 *
 * 逻辑：
 * 1. 创建集群
 * 2. 如果集群角色为compute
 *  a. 通过Routers.pool创建多个worker pool 行为，通过worker pool 行为创建 worker actor: WorkerRouter
 *    worker 创建定时器每隔30秒给自己发送钩子消息Message.EvictCache.INSTANCE，用于清楚单词缓存
 *  b. 通过a步骤中创建的workers作为参数传递给Service创建 StatsService actor, workers会缓存到Jvm中
 *  c. 将 StatsService actor 注册到 receptionist
 * 3. 如果集群角色为client
 *   a. 获取receptionist中注册的可用路由, 创建路由actor: ServiceRouter
 *   b. 将路由actor-ServiceRouter作为参数创建client actor: Client
 * 4.
 *
 */
public class App {
    //创建服务密钥。给定的ID应该使用给定的协议唯一地定义服务。
    static final ServiceKey<Message.ProcessText> STATS_SERVICE_KEY =
            ServiceKey.create(Message.ProcessText.class, "StatsService");

    private static class RootBehavior {

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                //创建集群
                Cluster cluster = Cluster.get(context.getSystem());

                //如果角色为compute
                if (cluster.selfMember().hasRole("compute")) {
                    //在每个计算节点上，都有一个服务实例委派给N个本地工人
                    final int numberOfWorkers = context.getSystem().settings().config().getInt("stats-service.workers-per-node");
                    Behavior<Message.Process> workerPoolBehavior =
                            Routers.pool(numberOfWorkers,
                                    StatsWorker.create().<Message.Process>narrow())
                                    .withConsistentHashingRouting(1, process -> process.word);
                    //创建workers actor
                    ActorRef<Message.Process> workers =
                            context.spawn(workerPoolBehavior, "WorkerRouter");
                    //创建Service actor 把当前创建workers actor作为参数
                    ActorRef<Message.CommandService> service =
                            context.spawn(StatsService.create(workers.narrow()), "StatsService");

                    // published through the receptionist to the other nodes in the cluster
                    //通过前台发布到集群中的其他节点
                    context.getSystem().receptionist().tell(Receptionist.register(STATS_SERVICE_KEY, service.narrow()));
                }

                //如果角色为client
                if (cluster.selfMember().hasRole("client")) {
                    //一个路由器，它将跟踪注册到[[akka.actor.typed.receptionist.Receptionist]]的可用路由, 并通过随机选择遍历。
                    ActorRef<Message.ProcessText> serviceRouter =
                            context.spawn(Routers.group(STATS_SERVICE_KEY), "ServiceRouter");
                    context.spawn(StatsClient.create(serviceRouter), "Client");
                }

                return Behaviors.empty();
            });
        }
    }

    private static void startup(String role, int port) {
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("stats"));

       ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            startup("compute", 25251);
            startup("compute", 25252);
            startup("compute", 0);
            startup("client", 0);
        } else {
            if (args.length != 2)
                throw new IllegalArgumentException("Usage: role port");
            startup(args[0], Integer.parseInt(args[1]));
        }
    }
}
