package com.tcfuture.akka.http.streaming;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

/**
 *
 *
 * @author liulv
 * @since 1.0.0
 */
public class HttpServerStreamRandomNumbers extends AllDirectives {

    /**
     * 创建路由Route
     *
     * @return Route
     */
    private Route createRoute(){
        //随机数
        final Random rnd = new Random();
        // 流是可重用的，所以我们可以在这里定义它
        // 并对每个请求使用它
        Source<Integer, NotUsed> numbers = Source.fromIterator(() -> Stream.generate(rnd::nextInt).iterator());

        return concat(path("random", () ->
                get(() ->
                        complete(HttpEntities.create(ContentTypes.TEXT_PLAIN_UTF8,
                                numbers.map(x -> ByteString.fromString(x + "\n"))
                                ))
                        )
        ));
    }

    public static void main(String[] args) throws IOException {
        Config config = ConfigFactory.load("http_test");
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes", config);

        final Http http = Http.get(system);
        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080).bind(new HttpServerStreamRandomNumbers().createRoute());

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding.thenCompose(ServerBinding::unbind)
                .thenAccept(unbind -> system.terminate());
    }
}
