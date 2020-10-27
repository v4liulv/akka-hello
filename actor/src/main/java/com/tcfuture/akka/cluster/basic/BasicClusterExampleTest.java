package com.tcfuture.akka.cluster.basic;

// #join-seed-nodes

import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import akka.cluster.MemberStatus;
import akka.cluster.typed.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import scala.collection.immutable.SortedSet;

import java.io.IOException;
import java.util.*;

// #join-seed-nodes
// #cluster-imports
// #cluster-imports

/**
 * @author liulv
 *
 * 基础集群示例
 */
public class BasicClusterExampleTest {

    private Config clusterConfig =
            ConfigFactory.parseString(
                    "akka { \n"
                            + "  actor.provider = cluster \n"
                            + "  remote.artery { \n"
                            + "    canonical { \n"
                            + "      hostname = \"127.0.0.1\" \n"
                            + "      port = 2551 \n"
                            + "    } \n"
                            + "  } \n"
                            + "}  \n");
    private Config noPort =
            ConfigFactory.parseString(
                    "      akka.remote.classic.netty.tcp.port = 0 \n"
                            + "      akka.remote.artery.canonical.port = 0 \n"
                            //如果需要配置同一个JVM进程允许启动多个集群，配置为on
                            + "      akka.cluster.jmx.multi-mbeans-in-same-jvm = on \n"
            );

    @Test
    public void clusterApiExample() {
        ActorSystem<Object> system =
                ActorSystem.create(Behaviors.empty(), "ClusterSystem", noPort.withFallback(clusterConfig));
        ActorSystem<Object> system2 =
                ActorSystem.create(Behaviors.empty(), "ClusterSystem2",
                        noPort.withFallback(clusterConfig));

        try {
            // #cluster-create
            Cluster cluster = Cluster.get(system);
            Cluster cluster2 = Cluster.get(system2);

            // #cluster-join
            cluster.manager().tell(Join.create(cluster.selfMember().address()));
            cluster2.manager().tell(Join.create(cluster2.selfMember().address()));

            SortedSet<Member> members = cluster.state().members();
            System.out.println(members.size());
            scala.collection.immutable.List<Member> members1 =
                    members.filter(e -> (e.status() == MemberStatus.up())).toList();
            System.out.println(members1.size());
            List<String> address = new ArrayList<>();
            members1.foreach(e -> {
                String atrs = e.address().toString();
                address.add(atrs);
                return atrs;
            });
            for (String s : address) {
                System.out.println(s);
            }

            // TODO wait for/verify cluster to form

            // #cluster-leave
            cluster.manager().tell(Leave.create(cluster.selfMember().address()));
            cluster2.manager().tell(Leave.create(cluster2.selfMember().address()));

            // TODO wait for/verify node 2 leaving

        } finally {
            // #actorSystem-terminate终止
            system.terminate();
            system2.terminate();
        }
    }

    /**
     * 集群订阅：可用于在集群状态更改时接收消息
     *
     * @throws Exception
     */
    @Test
    public void clusterSubscribe() throws Exception {
        ActorSystem<Object> system =
                ActorSystem.create(Behaviors.empty(), "ClusterSystem", noPort.withFallback(clusterConfig));

        try {
            Cluster cluster = Cluster.get(system);

            TestProbe<ClusterEvent.MemberEvent> testProbe = TestProbe.create(system);
            ActorRef<ClusterEvent.MemberEvent> subscriber = testProbe.getRef();
            // #cluster-subscribe
            //订阅了一个ActorRef<MemberEvent>订阅服务器
            cluster.subscriptions().tell(Subscribe.create(subscriber, ClusterEvent.MemberEvent.class));
            // #cluster-subscribe

            Address anotherMemberAddress = cluster.selfMember().address();
            // #cluster-leave-example
            cluster.manager().tell(Join.create(anotherMemberAddress));
            cluster.manager().tell(Leave.create(anotherMemberAddress));
            // subscriber will receive events MemberUp、MemberLeft, MemberExited and MemberRemoved
            // #cluster-leave-example
            testProbe.expectMessageClass(ClusterEvent.MemberUp.class).member().address().toString();
            testProbe.expectMessageClass(ClusterEvent.MemberLeft.class);
            testProbe.expectMessageClass(ClusterEvent.MemberExited.class);
            testProbe.expectMessageClass(ClusterEvent.MemberRemoved.class);

        } finally {
            system.terminate();
        }
    }

    /**
     * 加入种子节点，加入种子节点有三种方式：
     * 1. conf配置文件指定
     * akka{
     *     cluster{
     *          seed-nodes = [ “akka://ClusterSystem@host1:2552”,
     *        “akka://ClusterSystem@host2:2552”]
     *     }
     * }
     * 2. 可在JVM启动是指定
     * -Dakka.cluster.seed-nodes.0=akka://ClusterSystem@host1:2552
     * -Dakka.cluster.seed-nodes.1=akka://ClusterSystem@host2:2552
     *
     * 3. 通过本示例程序中加入种子节点
     *  Cluster.get(system).manager().tell(new JoinSeedNodes(seedNodes));
     */
    @Test
    public void illustrateJoinSeedNodes() throws IOException {
        ActorSystem<Object> system =
                ActorSystem.create(Behaviors.empty(), "ClusterSystem", noPort.withFallback(clusterConfig));

        Cluster cluster = Cluster.get(system);

        // #join-seed-nodes
        List<Address> seedNodes = new ArrayList<>();
        seedNodes.add(AddressFromURIString.parse("akka://ClusterSystem@127.0.0.1:2551"));
        seedNodes.add(AddressFromURIString.parse("akka://ClusterSystem@127.0.0.1:2552"));

        cluster.manager().tell(new JoinSeedNodes(seedNodes));
        // #join-seed-nodes

        //System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        system.terminate();
    }

    /**
     * 后端角色Behavior
     */
    static class Backend {
        static Behavior<Void> create() {
            return Behaviors.empty();
        }
    }

    /**
     * 前端角色Behavior
     */
    static class Frontend {
        static Behavior<Void> create() {
            return Behaviors.empty();
        }
    }

    /**
     * 集群角色示例
     *
     * 通过指定配置akka.cluster.roles
     * overrides.put("akka.cluster.roles", Collections.singletonList(role));
     */
    void illustrateRoles() {
        ActorContext<Void> context = null;

        // #hasRole
        Member selfMember = Cluster.get(context.getSystem()).selfMember();
        if (selfMember.hasRole("backend")) {
            context.spawn(Backend.create(), "back");
        } else if (selfMember.hasRole("front")) {
            context.spawn(Frontend.create(), "front");
        }
        // #hasRole
    }

    void illustrateDcAccess() {
        ActorSystem<Void> system = null;

        // #dcAccess
        final Cluster cluster = Cluster.get(system);
        // this node's data center
        String dc = cluster.selfMember().dataCenter();
        // all known data centers
        Set<String> allDc = cluster.state().getAllDataCenters();
        // a specific member's data center
        Member aMember = cluster.state().getMembers().iterator().next();
        String aDc = aMember.dataCenter();
        // #dcAccess
    }
}
