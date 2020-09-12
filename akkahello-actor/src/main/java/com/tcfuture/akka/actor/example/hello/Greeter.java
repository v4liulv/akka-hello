package com.tcfuture.akka.actor.example.hello;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

import java.util.Objects;

/**
 * （人名）:接收问候某人的命令，并以一个Greeted来回应，以确认问候已经发生
 */
// #greeter
public class Greeter extends AbstractBehavior<Greeter.Greet> {

  //消息类型Greet
  public static final class Greet {
    public final String whom;
    public final ActorRef<Greeted> replyTo;

    public Greet(String whom, ActorRef<Greeted> replyTo) {
      this.whom = whom;
      this.replyTo = replyTo;
    }
  }

  //消息类型Greeted
  public static final class Greeted {
    public final String whom;
    public final ActorRef<Greet> from;

    public Greeted(String whom, ActorRef<Greet> from) {
      this.whom = whom;
      this.from = from;
    }

// #greeter
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Greeted greeted = (Greeted) o;
      return Objects.equals(whom, greeted.whom) &&
              Objects.equals(from, greeted.from);
    }

    @Override
    public int hashCode() {
      return Objects.hash(whom, from);
    }

    @Override
    public String toString() {
      return "Greeted{" +
              "whom='" + whom + '\'' +
              ", from=" + from +
              '}';
    }
// #greeter
  }

  public static Behavior<Greet> create() {
    return Behaviors.setup(Greeter::new);
  }

  private Greeter(ActorContext<Greet> context) {
    super(context);
  }

  @Override
  public Receive<Greet> createReceive() {
    //如果消息类型为Greet，触发处理onGreet方法，处理方法的参数值为消息也就是Greet
    return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
  }

  private Behavior<Greet> onGreet(Greet command) {
    getContext().getLog().info("Hello {}!", command.whom);
    //#greeter-send-message
    command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
    //#greeter-send-message
    return this;
  }
}
// #greeter

