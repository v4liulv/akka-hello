package com.fcfutre.example.masterworker.master;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.fcfutre.example.masterworker.printer.PrinterActor;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Map;
import java.util.TreeMap;

public class MasterApp {

    private static String master = "master-system";
    private static String remoteAddr = "akka://worker-system@127.0.0.1:8787/user/worker_*";

    public static void main(String[] args) {
        Map<String, Object> overrideConfig = new TreeMap<>();
        overrideConfig.put("akka.actor.provider", "akka.remote.RemoteActorRefProvider");
        overrideConfig.put("akka.remote.artery.canonical.hostname", "localhost");
        overrideConfig.put("akka.remote.artery.canonical.port", 8786);
        Config conf = ConfigFactory.parseMap(overrideConfig);

        ActorSystem system = ActorSystem.create(master, conf);
        Props masterProps = Props.create(MasterActor.class, remoteAddr);
        // masterActor是发出消息的actor
        ActorRef masterActor = system.actorOf(masterProps, "master-actor");
        // printerActor是用于接收消息的actor
        ActorRef printerActor = system.actorOf(Props.create(PrinterActor.class), "printer-actor");
        // 把printerActor和消息一起发出去
        masterActor.tell("I AM MASTER", printerActor);
    }
}
