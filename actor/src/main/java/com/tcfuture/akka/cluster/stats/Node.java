package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 * @since 1.0.0
 */
public class Node {
    public static void main(String[] args) {
        if (args.length == 0) {
            startup("client", 25254);
        } else {
            if (args.length != 2)
                throw new IllegalArgumentException("Usage: role port");
            startup(args[0], Integer.parseInt(args[1]));
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

    //创建服务密钥。给定的ID应该使用给定的协议唯一地定义服务。
    static final ServiceKey<Message.ProcessText> STATS_SERVICE_KEY =
            ServiceKey.create(Message.ProcessText.class, "StatsService");

    private static class RootBehavior {

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                //创建集群
                Cluster cluster = Cluster.get(context.getSystem());

                //角色为client 创建 client actor
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
}
