package com.tcfuture.akka.actor.interactionpatterns.ask;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AskPattern;
import akka.actor.typed.javadsl.Behaviors;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 * @since 1.0.0
 */
@Slf4j
public class App {
    //main方法创建RootBehavior Actor
    public static void main(String[] args) {
        ActorSystem actorSystem = ActorSystem.create(RootBehavior.create(), "RequestResponseAsk");
    }

    /**
     * 而RootBehavior 创建CookieFabric actor， 并且调用ask请求封装的方法
     */
    private static class RootBehavior {
        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                //创建CookieFabric Actor
                ActorRef<CookieFabric.Command> cookieFabric =
                        context.spawn(CookieFabric.create(), "CookieFabric");

                //ask 请求方法
                App app = new App();
                app.askAndMapInvalid(context.getSystem(), cookieFabric);

                return Behaviors.empty();
            });
        }
    }

    /**
     * 执行ask请求，并且处理
     *
     * @param system ActorSystem
     * @param cookieFabric CookieFabric Actor
     */
    public void askAndMapInvalid(ActorSystem<Void> system, ActorRef<CookieFabric.Command> cookieFabric) {
        // #standalone-ask-fail-future
        //设置 参数count >=5 则模拟返回CookieFabric.InvalidRequest
        CompletionStage<CookieFabric.Reply> result = AskPattern.ask(cookieFabric,
                replyTo -> new CookieFabric.GiveMeCookies(5, replyTo),
                Duration.ofSeconds(3), system.scheduler());

        CompletionStage<CookieFabric.Cookies> cookies = result.thenCompose((CookieFabric.Reply reply) -> {
            if (reply instanceof CookieFabric.Cookies) {
                return CompletableFuture.completedFuture((CookieFabric.Cookies) reply);
            } else if (reply instanceof CookieFabric.InvalidRequest) {
                log.warn("无效的请求，原因：{}", ((CookieFabric.InvalidRequest) reply).reason);
                CompletableFuture<CookieFabric.Cookies> failed = new CompletableFuture<>();
                failed.completeExceptionally(new IllegalArgumentException(((CookieFabric.InvalidRequest) reply).reason));
                return failed;
            } else {
                throw new IllegalStateException("Unexpected reply: " + reply.getClass());
            }
        });

        cookies.whenComplete((cookiesReply, failure) -> {
            if (cookies != null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
            else System.out.println("Boo! didn't get cookies in time. " + failure);
        });
        // #standalone-ask-fail-future
    }
}

