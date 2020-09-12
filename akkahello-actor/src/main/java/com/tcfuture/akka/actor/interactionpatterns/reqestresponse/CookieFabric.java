package com.tcfuture.akka.actor.interactionpatterns.reqestresponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

/**
 * @author liulv
 */
public class CookieFabric {
    private static ActorContext<Request> actorContextcontext;

    public static class Request {
        final String query;
        public final ActorRef<Response> replyTo;

        Request(String query, ActorRef<Response> replyTo) {
            this.query = query;
            this.replyTo = replyTo;
        }
    }

    static class Response {
        final String result;


        Response(String result) {
            this.result = result;
        }
    }

    // actor behavior
    public static Behavior<Request> create() {
        return Behaviors.setup(
                context -> {
                    actorContextcontext = context;
                    return Behaviors.receive(Request.class).onMessage(Request.class,
                            CookieFabric::onRequest).build();
                }

        );
    }

    private static Behavior<Request> onRequest(Request request) {
        actorContextcontext.getLog().info("收到请求了， 我在回复...");
        request.replyTo.tell(new Response("Here are the cookies for " + request.query));
        return Behaviors.same();
    }
}