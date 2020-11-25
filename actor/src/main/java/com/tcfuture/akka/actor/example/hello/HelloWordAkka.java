package com.tcfuture.akka.actor.example.hello;

import akka.actor.typed.ActorSystem;

import java.io.IOException;

/**
 * @author liulv
 *
 * HelloWordAkka的主方法创建了带有监护人的ActorSystem。监护人是引导应用程序的顶级Actor。监护人通常用行为来定义。包含初始引导程序的设置。
 *
 * 创建Actor和发送消息流程：
 *
 * 1. 创建ActorSystem，并创建监护Actor:GreeterMain
 * 2. 创建监护Actor:GreeterMain过程并创建名为greeter的子Actor:Gretter
 * 2. 给监护Actor:GreeterMain发送GreeterMain.SayHello消息
 * 3. 监护Actor:GreeterMain回复消息1：根据GreeterMain.SayHello.name=Charles名创建回复Actor:GreeterBot（max=3）
 * 4. 监护Actor:GreeterMain回复消息2：给子Actor:Gretter(greeter)发送消息-Greeter.Greet(command.name=Charles, Actor:GreeterBot)
 * 5. Actor:Gretter(greeter)答复1:打印 Hello + 第一个参数值 Charles = Hello Charles
 * 6. Actor:Gretter(greeter)答复2: 给消息中第二个参数Actor:GreeterBot发送消息Greeted(command.whom=Charles, getContext().getSelf())
 * 7. Actor:GreeterBot答复打印Greeting 累加计数 + 第一个参数值Charles，如果累加计数不等于max=3则给Actor:Gretter(greeter)发送
 * 消息reeter.Greet(message.whom, getContext().getSelf())，如果累加计数等于max 3则停止Behaviors.stopped()
 * 8. greeter接收到Greet类型消息后回复，重复6、7步骤，直到计数等于max 3次
 *
 * 总结：其实就是实现3次对话的过程，这个过程不会重复创建Actor
 * Actor:GreeterMain创建时间创建了Actor:Gretter
 * 给Actor:GreeterMain发送指令SayHello消息， Actor:GreeterMain根据消息name和定义对话次数 创建Actor:GreeterBot
 * 并且给Actor:Gretter发送Greet消息，包含Actor名和需要进行回复的Actor:GreeterBot,
 * 然后gretter重复与GreeterBot对话了3次，并打印信息
 */
public class HelloWordAkka {
    public static void main(String[] args) {
        //#actor-system
        //创建名字为helloakka的ActorSystem，并创建了名称为greeter监护Actor<Greeter.Greet>
        final ActorSystem<GreeterMain.SayHello> greeterMain =
                ActorSystem.create(GreeterMain.create(), "hello_akka");
        System.out.println(greeterMain);
        //#actor-system

        //#main-send-messages
        //Actor System 发送新消息SayHello："Charles" 信息, 向这个ActorRef引用的Actor发送一条消息
        greeterMain.tell(new GreeterMain.SayHello("Charles"));
        //#main-send-messages

        try {
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (IOException ignored) {
        } finally {
            greeterMain.terminate();
        }
    }
}
