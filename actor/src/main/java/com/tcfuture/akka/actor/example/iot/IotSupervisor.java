package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * @author liulv
 *
 * 了解了actor的层次结构和行为之后，剩下的问题是如何将物联网系统的顶层组件映射到actor。用户监护人可以是代表整个应用程序的actor。换句话说，我们
 * 将在我们的物联网系统中有一个单一的顶级actor。创建和管理设备和仪表板的组件将是该actor的子组件。
 *
 * 不是使用println()，而是通过context.getLog()使用Akka的内置日志记录工具。
 *
 * 除了记录它的启动，应用程序几乎不做其他事情。但是，我们已经有了第一个参与者，并且准备添加其他参与者。
 *
 * 接下来需要做：
 * 1. 为设备创建表示形式。
 * 2. 创建设备管理组件。
 * 3. 向设备组添加查询功能。
 */
public class IotSupervisor extends AbstractBehavior<Void> {

    public static Behavior<Void> create(){
        return Behaviors.setup(IotSupervisor::new);
    }

    private IotSupervisor(ActorContext<Void> context) {
        super(context);
        context.getLog().info("IoT Application started");
    }

    // No need to handle any messages
    @Override
    public Receive<Void> createReceive() {
        return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
    }

    private IotSupervisor onPostStop() {
        getContext().getLog().info("IoT Application stopped");
        return this;
    }

}
