package com.tcfuture.akka.actor.interactionpatterns.child;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

/**
 * @author liulv
 *
 * 使用场景：
 * 在某些情况下，只有在从其他Actor收集多个答案响应之后，才能创建并发回对请求的完整响应。对于这些类型的交互，最好将工作
 * 委托给每个“会话”的子actor。子actor还可以包含实现重试、超时失败、尾部截断、进度检查等的任意逻辑。
 *
 * 子元素是用它执行工作所需的上下文创建的，包括它可以响应的ActorRef。当完整的结果出现时，孩子会以结果做出反应，然后停止。
 *
 * 请注意，这本质上就是ask的实现方式，如果您所需要的只是一个带有超时的响应，那么最好使用ask。
 *
 * 由于会话Actor的协议不是公共API，而是父Actor的实现细节，因此使用显式协议并调整会话Actor与之交互的Actor的消息可能
 * 并不总是有意义的。对于这个用例，可以表示Actor可以接收任何消息(对象)。
 *
 * 在构建结果之前，单个传入请求应该与其他Actor进行多次交互，例如多个结果的聚合您需要处理确认和重试消息，以便至少传递一次
 *
 * 1. LeaveHome --> Home
 * 2. Home --spawn--> perpareToLeaveHome
 * 3. perpareToLeaveHome --getKeys-->KeyCabinet
 * 4. perpareToLeaveHome --GetWallet-->Drawer
 * 5. Drawer --wallet--> perpareToLeaveHome
 * 6. KeyCabinet --Keys-->perpareToLeaveHome
 * 7. perpareToLeaveHome --readyToLeaveHome-->
 * 8. perpareToLeaveHome --stop
 *
 */
public class ActorChildSession {
}

// dummy data types just for this sample
class Keys {}

class Wallet {}

class KeyCabinet {

    public static class GetKeys {
        public final String whoseKeys;
        public final ActorRef<Keys> replyTo;

        public GetKeys(String whoseKeys, ActorRef<Keys> respondTo) {
            this.whoseKeys = whoseKeys;
            this.replyTo = respondTo;
        }
    }

    public static Behavior<GetKeys> create() {
        return Behaviors.receiveMessage(KeyCabinet::onGetKeys);
    }

    private static Behavior<GetKeys> onGetKeys(GetKeys message) {
        message.replyTo.tell(new Keys());
        return Behaviors.same();
    }
}

class Drawer {

    public static class GetWallet {
        public final String whoseWallet;
        public final ActorRef<Wallet> replyTo;

        public GetWallet(String whoseWallet, ActorRef<Wallet> replyTo) {
            this.whoseWallet = whoseWallet;
            this.replyTo = replyTo;
        }
    }

    public static Behavior<GetWallet> create() {
        return Behaviors.receiveMessage(Drawer::onGetWallet);
    }

    private static Behavior<GetWallet> onGetWallet(GetWallet message) {
        message.replyTo.tell(new Wallet());
        return Behaviors.same();
    }
}

class Home {

    public interface Command {}

    public static class LeaveHome implements Command {
        public final String who;
        public final ActorRef<ReadyToLeaveHome> respondTo;

        public LeaveHome(String who, ActorRef<ReadyToLeaveHome> respondTo) {
            this.who = who;
            this.respondTo = respondTo;
        }
    }

    public static class ReadyToLeaveHome {
        public final String who;
        public final Keys keys;
        public final Wallet wallet;

        public ReadyToLeaveHome(String who, Keys keys, Wallet wallet) {
            this.who = who;
            this.keys = keys;
            this.wallet = wallet;
        }
    }

    private final ActorContext<Command> context;

    private final ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private final ActorRef<Drawer.GetWallet> drawer;

    private Home(ActorContext<Command> context) {
        this.context = context;
        this.keyCabinet = context.spawn(KeyCabinet.create(), "key-cabinet");
        this.drawer = context.spawn(Drawer.create(), "drawer");
    }

    private Behavior<Command> behavior() {
        return Behaviors.receive(Command.class)
                .onMessage(LeaveHome.class, this::onLeaveHome)
                .build();
    }

    private Behavior<Command> onLeaveHome(LeaveHome message) {
        context.spawn(
                PrepareToLeaveHome.create(message.who, message.respondTo, keyCabinet, drawer),
                "leaving" + message.who);
        return Behaviors.same();
    }

    // actor behavior
    public static Behavior<Command> create() {
        return Behaviors.setup(context -> new Home(context).behavior());
    }
}

// per session actor behavior
class PrepareToLeaveHome extends AbstractBehavior<Object> {
    static Behavior<Object> create(
            String whoIsLeaving,
            ActorRef<Home.ReadyToLeaveHome> replyTo,
            ActorRef<KeyCabinet.GetKeys> keyCabinet,
            ActorRef<Drawer.GetWallet> drawer) {
        return Behaviors.setup(
                context -> new PrepareToLeaveHome(context, whoIsLeaving, replyTo, keyCabinet, drawer));
    }

    private final String whoIsLeaving;
    private final ActorRef<Home.ReadyToLeaveHome> replyTo;
    private final ActorRef<KeyCabinet.GetKeys> keyCabinet;
    private final ActorRef<Drawer.GetWallet> drawer;
    private Optional<Wallet> wallet = Optional.empty();
    private Optional<Keys> keys = Optional.empty();

    private PrepareToLeaveHome(
            ActorContext<Object> context,
            String whoIsLeaving,
            ActorRef<Home.ReadyToLeaveHome> replyTo,
            ActorRef<KeyCabinet.GetKeys> keyCabinet,
            ActorRef<Drawer.GetWallet> drawer) {
        super(context);
        this.whoIsLeaving = whoIsLeaving;
        this.replyTo = replyTo;
        this.keyCabinet = keyCabinet;
        this.drawer = drawer;
    }

    @Override
    public Receive<Object> createReceive() {
        return newReceiveBuilder()
                .onMessage(Wallet.class, this::onWallet)
                .onMessage(Keys.class, this::onKeys)
                .build();
    }

    private Behavior<Object> onWallet(Wallet wallet) {
        this.wallet = Optional.of(wallet);
        return completeOrContinue();
    }

    private Behavior<Object> onKeys(Keys keys) {
        this.keys = Optional.of(keys);
        return completeOrContinue();
    }

    private Behavior<Object> completeOrContinue() {
        if (wallet.isPresent() && keys.isPresent()) {
            replyTo.tell(new Home.ReadyToLeaveHome(whoIsLeaving, keys.get(), wallet.get()));
            return Behaviors.stopped();
        } else {
            return this;
        }
    }
}
