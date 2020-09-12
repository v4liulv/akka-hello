package com.tcfuture.akka.actor.interactionpatterns.wrapper;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.pattern.StatusReply;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 */
public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

    interface Command {
    }

    public static class GiveMeCookies implements CookieFabric.Command {
        public final int count;
        public final ActorRef<StatusReply<CookieFabric.Cookies>> replyTo;

        public GiveMeCookies(int count, ActorRef<StatusReply<CookieFabric.Cookies>> replyTo) {
            this.count = count;
            this.replyTo = replyTo;
        }
    }

    public static class Cookies {
        public final int count;

        public Cookies(int count) {
            this.count = count;
        }
    }

    public static Behavior<CookieFabric.Command> create() {
        return Behaviors.setup(CookieFabric::new);
    }

    private CookieFabric(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(CookieFabric.GiveMeCookies.class, this::onGiveMeCookies).build();
    }

    private Behavior<Command> onGiveMeCookies(CookieFabric.GiveMeCookies request) {
        if (request.count >= 5) request.replyTo.tell(StatusReply.error("Too many cookies."));
        else request.replyTo.tell(StatusReply.success(new CookieFabric.Cookies(request.count)));

        return this;
    }

    public void askAndPrint(ActorSystem<Void> system, ActorRef<Command> cookieFabric) {
        CompletionStage<Cookies> result = AskPattern.askWithStatus(cookieFabric, replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
                // asking someone requires a timeout and a scheduler, if the timeout hits without
                // response the ask is failed with a TimeoutException
                Duration.ofSeconds(3), system.scheduler());

        result.whenComplete((reply, failure) -> {
            if (reply != null) System.out.println("Yay, " + reply.count + " cookies!");
            else if (failure instanceof StatusReply.ErrorMessage) System.out.println("No cookies for me. " + failure.getMessage());
            else System.out.println("Boo! didn't get cookies in time. " + failure);
        });

        //注意，验证错误在消息协议中也是显式的，但被编码为包装类型，使用StatusReply.error(text)构造
       /* result.whenComplete(
                (cookiesReply, failure) -> {
                    if (result != null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
                    else System.out.println("Boo! didn't get cookies in time. " + failure);
                });*/
    }
}
