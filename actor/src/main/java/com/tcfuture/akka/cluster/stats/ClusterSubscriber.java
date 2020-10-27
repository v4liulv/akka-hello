package com.tcfuture.akka.cluster.stats;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ClusterEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author liulv
 * @since 1.0.0
 *
 * 集群订阅Actor
 */
public class ClusterSubscriber {

    public static final Set<String> clients =  new HashSet<>();

    static Behavior<ClusterEvent.MemberEvent> create() {

        return Behaviors.setup(context -> Behaviors.receive(ClusterEvent.MemberEvent.class)
                 .onMessage(ClusterEvent.MemberUp.class, memberUp -> {
                     String hostPort = memberUp.member().address().hostPort();
                     context.getLog().info("订阅了节点Up: {}", hostPort);
                     clients.add(hostPort);
                     return Behaviors.same();
                 })
                .onMessage(ClusterEvent.MemberLeft.class, memberUp -> {
                    context.getLog().info("订阅了节点left: {}",
                            memberUp.member().address().hostPort());
                    return Behaviors.same();
                })
                .onMessage(ClusterEvent.MemberExited.class, memberUp -> {
                    String hostPort = memberUp.member().address().hostPort();
                    context.getLog().info("订阅了节点exited: {}", hostPort);
                    clients.remove(hostPort);
                    return Behaviors.same();
                })
                .onMessage(ClusterEvent.MemberRemoved.class, memberUp -> {
                    context.getLog().info("订阅了节点removed: {}",
                            memberUp.member().address().hostPort());
                    return Behaviors.same();
                })
                .build());
    }
}
