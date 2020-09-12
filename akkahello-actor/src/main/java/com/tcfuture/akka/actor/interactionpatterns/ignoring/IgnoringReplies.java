package com.tcfuture.akka.actor.interactionpatterns.ignoring;

/**
 * @author liulv
 *
 * 在某些情况下，Actor对特定请求消息有响应，但您对响应不感兴趣。在这种情况下，您可以传递system.ignoreRef()，
 * 将请求-响应转换为“发射-忘记”。
 *
 * 顾名思义，system.ignoreRef()返回一个忽略发送给它的任何消息的ActorRef。
 */
public class IgnoringReplies {
    /**
     * 对于与上面的请求响应相同的协议，如果发送方希望忽略应答，它可以为replyTo传递system.ignoreRef()，
     * 它可以通过ActorContext.getSystem().ignoreref()访问该应答
     */

    //cookieFabric.tell(
    //        new CookieFabric.Request("don't send cookies back", context.getSystem().ignoreRef()));

}
