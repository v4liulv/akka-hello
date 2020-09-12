package com.tcfuture.akka.actor.interactionpatterns.reqestresponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 */
public class Main {

    private static ActorContext<CookieFabric.Response> actorContext;

    // actor behavior
    public static Behavior<CookieFabric.Response> create() {
        return Behaviors.setup(
                context -> {
                    actorContext = context;
                    return Behaviors.receive(CookieFabric.Response.class).onMessage(CookieFabric.Response.class,
                            Main::onResponse).build();
                }

                );
    }

    private static Behavior<CookieFabric.Response> onResponse(CookieFabric.Response response) {
        // ... process request ...
        actorContext.getLog().info("收到回复的消息了： {}", response.result);
        return Behaviors.stopped();
    }

    public static void main(String[] args) {
        final ActorSystem<CookieFabric.Request> actorSystem =
                ActorSystem.create(CookieFabric.create(), "requestresponse");

        // note that system is also the ActorRef to the guardian actor
        ActorRef<CookieFabric.Response> response = actorSystem.systemActorOf(Main.create(),
                "mainres",
                Props.empty());

        actorSystem.tell(new CookieFabric.Request("消息1", response));
    }
}
