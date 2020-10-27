package com.fcfutre.example.masterworker.master;

import akka.actor.AbstractActor;
import akka.actor.ActorSelection;


public class MasterActor extends AbstractActor {

    private String remoteAddr;

    public MasterActor(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, msg -> {
                    ActorSelection sel = getContext().getSystem().actorSelection(remoteAddr);
                    // 把printerActor传递给worker，tell
                    sel.tell("master call '" + msg + "'", getSender());
                }).build();
    }
}
