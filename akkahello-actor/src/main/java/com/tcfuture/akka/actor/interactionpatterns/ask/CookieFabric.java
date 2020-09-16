package com.tcfuture.akka.actor.interactionpatterns.ask;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.time.Duration;
import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 *
 * GiveMeCookies请求可以用cookie或InvalidRequest进行响应。请求者必须决定如何处理InvalidRequest应答。
 * 有时应该将其视为失败的未来，因此可以将应答映射到请求者端。还请参阅通用响应包装器，了解成功或错误的响应。
 */
public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {
    interface Command {}

    public static class GiveMeCookies implements Command {
        public final int count;
        public final ActorRef<Reply> replyTo;

        public GiveMeCookies(int count, ActorRef<Reply> replyTo) {
            this.count = count;
            this.replyTo = replyTo;
        }
    }

    /**
     * 回复消息接口
     */
    interface Reply {}

    /**
     * 回复消息-Cookie数量
     */
    public static class Cookies implements Reply {
        public final int count;

        public Cookies(int count) {
            this.count = count;
        }
    }

    /**
     * 回复消息-无效的请求
     *
     * 无效请求原因
     */
    public static class InvalidRequest implements Reply {
        public final String reason;

        public InvalidRequest(String reason) {
            this.reason = reason;
        }
    }

    /**
     * 创建Actor入口方法
     *
     * @return Behavior<Command>
     */
    public static Behavior<Command> create() {
        return Behaviors.setup(CookieFabric::new);
    }

    private CookieFabric(ActorContext<Command> context) {
        super(context);
    }

    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder().onMessage(GiveMeCookies.class, this::onGiveMeCookies).build();
    }

    /**
     * 处理GiveMeCookies消息，返回Cookies信息，如果超过超过5次后返回无效的请求InvalidRequest
     *
     * @param request GiveMeCookies
     * @return 返回对应行为
     */
    private Behavior<Command> onGiveMeCookies(GiveMeCookies request) {
        if (request.count >= 5) request.replyTo.tell(new InvalidRequest("Too many cookies."));
        else request.replyTo.tell(new Cookies(request.count));

        return this;
    }

    public void askAndPrint(ActorSystem<Void> system,
                            ActorRef<CookieFabric.Command> cookieFabric) {
        CompletionStage<Reply> result =
                AskPattern.ask(
                        cookieFabric,
                        replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
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
