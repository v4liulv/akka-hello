package com.tcfuture.akka.http;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 *
 * Akka HTTP的高级路由API提供了一个DSL，用于描述HTTP“路由”以及如何处理它们。每个路由由一个或多个级别组成，范围缩小到处理一种特定类型的请求
 *
 * 例如，一条路由可能以匹配path请求的开头，只有在匹配“/hello”时才匹配，然后将其范围缩小为仅处理HTTP get请求，
 * 然后将其处理为complete带有字符串文字的请求，这些请求将以HTTP OK的形式发送回字符串作为响应主体。
 * 然后，使用路由DSL创建的内容被“绑定”到端口以开始服务HTTP请求： Route
 */
public class HttpServerMinimal extends AllDirectives {

    public static void main(String[] args) throws Exception {
        // 使用下面定义的路由启动服务器
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", "27702");
        //overrides.put("akka.cluster.roles", Collections.singletonList(role));
        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("http_test"));
        ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "routes", config);

        final Http http = Http.get(system);

        //为了访问所有指令，我们需要一个定义路由的实例。
        HttpServerMinimal app = new HttpServerMinimal();

        final CompletionStage<ServerBinding> binding =
                http.newServerAt("localhost", 8080)
                        .bind(app.createRoute());

        System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
        System.in.read(); // let it run until user presses return

        binding.thenCompose(ServerBinding::unbind) //从端口解除绑定的触发器
                .thenAccept(unbound -> system.terminate()); // 完成后关闭
    }

    private Route createRoute() {
        return concat(
                path("hello", () ->
                        get(() ->
                                complete("<h1>Say hello to akka-http</h1>"))));
    }
}