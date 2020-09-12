package com.tcfuture.akka.actor.example.helloword.old;
import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;

/**
 * @author liulv
 *
 * 它只处理一种类型的消息Greeting，并记录该消息的内容。
 */
public class Printer extends AbstractActor {
    static public Props props() {
        return Props.create(Printer.class, () -> new Printer());
    }

    static public class Greeting {
        public final String message;

        public Greeting(String message) {
            this.message = message;
        }
    }

    //它通过Logging.getLogger(getContext().getSystem(), this);创建一个日志器.通过这样做，我们可以在 Actor 中编写log.info() ，
    // 而不需要任何额外的连接。
    private LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public Printer() {
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greeting.class, greeting -> {
                    log.info(greeting.message);
                })
                .build();
    }
}
