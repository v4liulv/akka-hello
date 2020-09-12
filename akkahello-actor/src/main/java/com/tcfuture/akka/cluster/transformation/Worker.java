package com.tcfuture.akka.cluster.transformation;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;

/**
 * @author liulv
 */
public class Worker {

    /**
     * receptionist注册的服务key
     */
    public static ServiceKey<Massage.TransformText> WORKER_SERVICE_KEY =
            ServiceKey.create(Massage.TransformText.class, "worker");

    /**
     * 创建Actor，先将自己注册到receptionist
     *
     * 创建actor 行为， 接受TransformText消息，并将消息中的转换为大写后存储到TextTransformed并回复消息给接受者（消息中的发送的replyTo)
     *
     * @return
     */
    public static Behavior<Massage.Command> create() {
        return Behaviors.setup(
                context -> {
                    context.getLog().info("向receptionist接待员注册自己");
                    //一定要注册，不然订阅消息的接收不到worker信息
                    context.getSystem().receptionist().tell(Receptionist.register(WORKER_SERVICE_KEY,
                            context.getSelf().narrow()));


                    return Behaviors.receive(Massage.Command.class)
                            .onMessage(Massage.TransformText.class, command -> {
                                command.replyTo.tell(new Massage.TextTransformed(command.text.toUpperCase()));
                                return Behaviors.same();
                            }).build();
                }
        );
    }

}
