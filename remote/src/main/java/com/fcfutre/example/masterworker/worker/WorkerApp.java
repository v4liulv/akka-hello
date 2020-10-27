package com.fcfutre.example.masterworker.worker;

import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Map;
import java.util.TreeMap;

public class WorkerApp {

    private static ActorSystem workerSystem;
    private static Props props;
    private static Integer workerCount = 3;
    private static String workerSystemName = "worker-system";
    private static String askStr = "hello";

    public static void main(String[] args) {
        Map<String, Object> overrideConfig = new TreeMap<>();
        overrideConfig.put("akka.actor.provider", "akka.remote.RemoteActorRefProvider");
        overrideConfig.put("akka.remote.artery.canonical.hostname", "127.0.0.1");
        overrideConfig.put("akka.remote.artery.canonical.port", 8787);
        Config conf = ConfigFactory.parseMap(overrideConfig);

        workerSystem = ActorSystem.create(workerSystemName, conf);
        props = Props.create(WorkerActor.class);
        genManyWorker(workerCount);
        sendTest(askStr);
    }

    // 建多个actor，成一个组
    private static void genManyWorker(int count) {
        for (int i = 0; i < count; i++) {
            workerSystem.actorOf(props, "worker_" + i);
        }
    }

    // 向genManyWorker()生成的actor组发消息，测试
    private static void sendTest(String msg) {
        ActorSelection sel = workerSystem.actorSelection("akka://worker-system/user/worker_*");
        sel.tell(msg, ActorRef.noSender());
    }
}
