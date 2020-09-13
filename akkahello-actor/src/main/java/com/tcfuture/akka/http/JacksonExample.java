package com.tcfuture.akka.http;

import akka.Done;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.PathMatchers.longSegment;

/**
 * @author liulv
 * 一个常见的用例是使用模型对象来答复请求，该模型对象将编组器将其转换为JSON。在这种情况下，显示为两条单独的路线。
 * 第一条路由查询异步数据库，并将结果编组为JSON响应。第二个从收到的请求中解组一个an ，将其保存到数据库中，并在
 * 完成后单击OK进行回复。CompletionStage<Optional<Item>>Order
 *
 * 运行此服务器时，可以通过curl -H "Content-Type: application/json" -X POST -d '{"items":[{"name":
 * "hhgtg","id":42}]}' http://localhost:8080/create-order终端上的库存更新-添加名为"hhgtg"并具有的项目
 * id=42。然后在浏览器中通过类似http：// localhost：8080 / item / 42的URL 或在终端上通过来查看清单curl
 * http://localhost:8080/item/42。
 *
 * 在本示例中，用于编组和解组JSON的逻辑由“ Jackson”库提供。见JSON支持），以获取有关该库集成的详细信息
 */
public class JacksonExample extends AllDirectives {

    public static void main(String[] args) throws Exception {
        // boot up server using the route as defined below
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", "27701");
        //overrides.put("akka.cluster.roles", Collections.singletonList(role));
        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("http_test"));
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes", config);

        final Http http = Http.get(system);

        //In order to access all directives we need an instance where the routes are define.
        JacksonExample app = new JacksonExample();

        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                        .bind(app.createRoute());

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding
                .thenCompose(ServerBinding::unbind) // trigger unbinding from the port
                .thenAccept(unbound -> system.terminate()); // and shutdown when done
    }

    // (fake) async database query api
    private CompletionStage<Optional<Item>> fetchItem(long itemId) {
        return CompletableFuture.completedFuture(Optional.of(new Item("foo", itemId)));
    }

    // (fake) async database query api
    private CompletionStage<Done> saveOrder(final Order order) {
        return CompletableFuture.completedFuture(Done.getInstance());
    }

    private Route createRoute() {

        return concat(
                get(() ->
                        pathPrefix("item", () ->
                                path(longSegment(), (Long id) -> {
                                    final CompletionStage<Optional<Item>> futureMaybeItem = fetchItem(id);
                                    return onSuccess(futureMaybeItem, maybeItem ->
                                            maybeItem.map(item -> completeOK(item, Jackson.marshaller()))
                                                    .orElseGet(() -> complete(StatusCodes.NOT_FOUND, "Not Found"))
                                    );
                                }))),
                post(() ->
                        path("create-order", () ->
                                entity(Jackson.unmarshaller(Order.class), order -> {
                                    CompletionStage<Done> futureSaved = saveOrder(order);
                                    return onSuccess(futureSaved, done ->
                                            complete("order created")
                                    );
                                })))
        );
    }

    @Getter
    private static class Item {

        final String name;
        final long id;

        @JsonCreator
        Item(@JsonProperty("name") String name,
             @JsonProperty("id") long id) {
            this.name = name;
            this.id = id;
        }
    }

    @Getter
    private static class Order {

        final List<Item> items;

        @JsonCreator
        Order(@JsonProperty("items") List<Item> items) {
            this.items = items;
        }
    }
}