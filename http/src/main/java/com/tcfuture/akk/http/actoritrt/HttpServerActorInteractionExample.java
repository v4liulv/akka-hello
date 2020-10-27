package com.tcfuture.akk.http.actoritrt;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Akka HTTP路由可以轻松地与Actor进行交互。在此示例中，一条路线允许以即发即弃的方式放置出价，而第二条路线包含
 * 与actor的请求-响应交互。结果响应将呈现为json，并在响应从actor到达时返回。
 *
 * @author liulv
 * @since 1.0.0
 */
public class HttpServerActorInteractionExample extends AllDirectives {

    //ActorSystem属性
    private final ActorSystem<Auction.Message> system;
    //ActorRef 拍卖
    private final ActorRef<Auction.Message> auction;

    public static void main(String[] args) throws Exception {
        Config config = ConfigFactory.load("http_test");
        // boot up server using the route as defined below
        ActorSystem<Auction.Message> system = ActorSystem.create(Auction.create(), "routes", config);

        final Http http = Http.get(system);

        //In order to access all directives we need an instance where the routes are define.
        HttpServerActorInteractionExample app = new HttpServerActorInteractionExample(system);

        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                .bind(app.createRoute());

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return


        binding.thenCompose(ServerBinding::unbind) //解除端口绑定
                .thenAccept(unbound -> system.terminate()); // 当binding 挡掉后结束actor
    }

    private HttpServerActorInteractionExample(final ActorSystem<Auction.Message> system) {
        this.system = system;
        this.auction = system;
    }

    private Route createRoute() {
        return concat(
                path("auction", () -> concat(
                        put(() ->
                            parameter(StringUnmarshallers.INTEGER, "bid", bid ->
                                    parameter("user", user -> {
                                        //竞标，发完即丢弃，不用等待响应
                                        auction.tell(new Auction.Bid(user, bid));
                                        return complete(StatusCodes.ACCEPTED, "bid placed");
                                    })
                            )),
                        //向actor查询当前拍卖状态
                        get(() ->
                            //CompletionStage<Optional<Auction.Bids>> bids =AskPattern.ask(auction,Auction.GetBids::new, Duration.ofSeconds(5),system.scheduler());
                            //return completeOKWithFuture(bids, Jackson.marshaller());
                            onSuccess(getBids(), opt -> {
                                if (opt.isPresent()) {
                                    //return complete(StatusCodes.NOT_FOUND,"bids" +"为空");
                                    //opt.get();
                                    Auction.Bids bids = opt.get();

                                    if(bids.getBids().size() == 0){
                                        return complete(StatusCodes.NOT_FOUND, "bids为空");
                                    }else {
                                        return complete(StatusCodes.OK, bids, Jackson.<Auction.Bids>marshaller());
                                       /* String bidsJson =
                                                JSON.toJSONString(bids.getBids().get(0));
                                        System.out.println(bids.getBids().get(0).userId);
                                        return complete(StatusCodes.OK, bidsJson);*/
                                    }
                                } else {
                                    return complete(StatusCodes.NOT_FOUND,"bids为空");
                                }
                            })
                        )
                )));
    }

    private CompletionStage<Optional<Auction.Bids>> getBids(){
        return AskPattern.ask(auction,Auction.GetBids::new, Duration.ofSeconds(5), system.scheduler());
    }

    static class Auction extends AbstractBehavior<Auction.Message> {

        //竞标列表
        private final List<Bid> bids = new ArrayList<>();

        interface Message  {}

        //竞标者信息
        @AllArgsConstructor
        @Getter
        public static final class Bid implements Message {
            final String userId;
            final int offer;

         /*   @JsonCreator
            public Bid(String userId, int offer){
                this.userId = userId;
                this.offer = offer;
            }*/
        }

        //获取竞标信息
        @AllArgsConstructor
        static final class GetBids implements Message {
            final ActorRef<Optional<Bids>> replyTo;
        }

        //全部竞标信息
        @Getter
        @AllArgsConstructor
        static final class Bids implements Message{
            public final List<Bid> bids;
        }

        public Auction(ActorContext<Message> context) {
            super(context);
        }

        public static Behavior<Message> create() {
            return Behaviors.setup(Auction::new);
        }

        @Override
        public Receive<Message> createReceive() {
            return newReceiveBuilder()
                    .onMessage(Bid.class, this::onBid)
                    .onMessage(GetBids.class, this::onGetBids)
                    .build();
        }

        /**
         * 开始竞标消息回复，将竞标信息添加到竞标集合中
         *
         * @param bid Bid
         * @return
         */
        private Behavior<Message> onBid(Bid bid) {
            bids.add(bid);
            getContext().getLog().info("Bid complete: {}, {}", bid.userId, bid.offer);
            return this;
        }

        /**
         * 获取当前全部的竞标信息
         *
         * @param getBids GetBids
         * @return Bids-示例中存储的竞标集合构建Bids实例返回
         */
        private Behavior<Message> onGetBids(GetBids getBids) {
            Bids rBids = new Bids(bids);
            getContext().getLog().info("获取全部的Bids，size: {}, {}",  bids.size(), rBids.toString());
            if(rBids.getBids().size() > 0){
                getBids.replyTo.tell(Optional.of(rBids));
            }else {
                getBids.replyTo.tell(Optional.empty());
            }
            return this;
        }
    }
}
