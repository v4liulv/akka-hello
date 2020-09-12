package com.tcfuture.akka.actor.example.helloword.nw;

import akka.actor.typed.ActorSystem;

/**
 * @author liulv
 */
public class HelloWordApp {
    public static void main(String[] args) {
        final ActorSystem<HelloWorldMain.SayHello> system =
                ActorSystem.create(HelloWorldMain.create(), "hello");

        system.tell(new HelloWorldMain.SayHello("World"));
        system.tell(new HelloWorldMain.SayHello("Akka"));
    }
}
