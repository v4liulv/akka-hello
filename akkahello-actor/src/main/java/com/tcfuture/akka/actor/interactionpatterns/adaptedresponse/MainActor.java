package com.tcfuture.akka.actor.interactionpatterns.adaptedresponse;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.net.URI;

/**
 * @author liulv
 *
 *
 */
public class MainActor {

    public static class URIActor extends AbstractBehavior<URI> {

        public URIActor(ActorContext<URI> context) {
            super(context);
        }

        public static Behavior<URI> create(){
            return Behaviors.setup(URIActor::new);
        }

        @Override
        public Receive<URI> createReceive() {
            return null;
        }
    }

    public static class RequestActor extends AbstractBehavior<Backend.Request> {

        public final ActorRef<Frontend.Command> translatorActorRef;
        public final ActorRef<URI> uriActorRef;

        public RequestActor(ActorContext<Backend.Request> context) {
            super(context);
            translatorActorRef = context.spawn(Frontend.Translator.create(context.getSelf()),
                    "translator");
            uriActorRef = context.spawn(URIActor.create(), "123");
        }

        public static Behavior<Backend.Request> create(){
            return Behaviors.setup(RequestActor::new);
        }

        @Override
        public Receive<Backend.Request> createReceive() {
            return newReceiveBuilder().onMessage(Backend.StartTranslationJob.class, this::onRequest).build();
        }

        private Behavior<Backend.Request> onRequest(Backend.Request cmd) {
            Frontend.Translate translate = new Frontend.Translate(URI.create("123"), uriActorRef);
            translatorActorRef.tell(translate);
            return this;
        }
    }

    public static void main(String[] args) {
        ActorSystem<Backend.Request> actorSystem =
                ActorSystem.create(RequestActor.create(), "request");
    }
}
