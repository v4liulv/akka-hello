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
 *
 * Actor Ask请求响应示例代码
 *
 * 有时您需要与actor系统外部的actor进行交互，这可以通过如上所述的“一劳永逸”操作或通过ask返回a的另一个版本来完成，
 * 该响应要么成功完成，要么失败，如果成功，在指定的超时时间内没有响应。CompletionStage<Response>TimeoutException
 *
 * 为此，我们用于akka.actor.typed.javadsl.AskPattern.ask向Actor发送消息并获得CompletionState[Response]回复。
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
                //比较全的处理响应
                //app.askAndMapInvalid(context.getSystem(), cookieFabric);
                //直接打印处理
                app.askAndPrint(context.getSystem(), cookieFabric);

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

        //ask 响应结果中间处理，正常响应、模拟存储异常、直接抛出异常
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

        ///ask 响应结果处理，成功值打印，或模拟失败打印、或请求超时无响应处理等
        cookies.whenComplete((cookiesReply, failure) -> {
            if (cookies != null){
                if(cookiesReply != null)
                System.out.println("Yay, " + cookiesReply.count + " cookies!");
                else {
                    log.warn(failure.getMessage());
                }
            }
            else log.warn("Boo! didn't get cookies in time. " + failure);
        });
        // #standalone-ask-fail-future
    }

    public void askAndPrint(ActorSystem<Void> system,
                            ActorRef<CookieFabric.Command> cookieFabric) {
        CompletionStage<CookieFabric.Reply> result =
                AskPattern.ask(
                        cookieFabric,
                        replyTo -> new CookieFabric.GiveMeCookies(5, replyTo),
                        // asking someone requires a timeout and a scheduler, if the timeout hits without
                        // response the ask is failed with a TimeoutException
                        Duration.ofSeconds(3),
                        system.scheduler());
        //也可以使用, 但是第二个参数是Object，不是Function
        //CompletionStage<Object> askCS = Patterns.ask(
        //        cookieFabric,
        //        new CookieFabric.GiveMeCookies(3, cookieFabric),
        //        Duration.ofSeconds(3));

        result.whenComplete(
                (reply, failure) -> {
                    if (reply instanceof CookieFabric.Cookies)
                        System.out.println("Yay, " + ((CookieFabric.Cookies) reply).count + " cookies!");
                    else if (reply instanceof CookieFabric.InvalidRequest)
                        System.out.println(
                                "No cookies for me. " + ((CookieFabric.InvalidRequest) reply).reason);
                    else System.out.println("Boo! didn't get cookies in time. " + failure);
                });
    }
}

