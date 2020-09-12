package com.tcfuture.akka.cluster.transformation;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 */
public class MainApp {

    public static void main(String[] args) {
        if (args.length == 0) {
            startup("backend", 25251);
            startup("backend", 25252);
            startup("frontend", 0);
        } else {
            if (args.length != 2) throw new IllegalArgumentException("Usage: role port");
            startup(args[0], Integer.parseInt(args[1]));
        }
    }

    /**
     *
     * @param role 角色名
     * @param port) 端口
     */
    private static void startup(String role, int port) {
        //覆盖端口配置
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("transformation"));

        ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }

    /**
     * 跟行为，管理创建集群和集群之间的角色actor
     */
    private static class RootBehavior {

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                //创建集群
                Cluster cluster = Cluster.get(context.getSystem());

                // 如果集群成员的角色为前端frontend
                if(cluster.selfMember().hasRole("frontend")){
                    context.spawn(Frontend.create(), "Frontend");
                }

                //如果集群成员的角色为后端backend
                if(cluster.selfMember().hasRole("backend")){
                    //每个节点的worker数
                    int workersPerNode = context.getSystem().settings().config().getInt("transformation.workers-per-node");
                    //循环创建worker actor
                    for (int i = 0; i < workersPerNode; i++) {
                        context.spawn(Worker.create(), "Worker" + i);
                    }
                }

                return Behaviors.empty();
            });
        }
    }

}
