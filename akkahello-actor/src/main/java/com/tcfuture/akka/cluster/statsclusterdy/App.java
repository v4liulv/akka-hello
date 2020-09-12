package com.tcfuture.akka.cluster.statsclusterdy;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Routers;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.typed.Cluster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author liulv
 *
 * 逻辑：
 * 1. 创建集群
 * 2. 如果集群角色为compute
 *  a. 通过Routers.pool创建多个worker pool 行为，通过worker pool 行为创建 worker actor: WorkerRouter
 *    worker 创建定时器每隔30秒给自己发送钩子消息Message.EvictCache.INSTANCE，用于清楚单词缓存
 *  b. 通过a步骤中创建的workers作为参数传递给Service创建 StatsService actor, workers会缓存到Jvm中
 *  c. 将 StatsService actor 注册到 receptionist
 * 3. 如果集群角色为client
 *   a. 获取receptionist中注册的可用路由, 创建路由actor: ServiceRouter
 *   b. 将路由actor-ServiceRouter作为参数创建client actor: Client
 * 4. Actor消息处理
 *  a. client actor 通过Behaviors.withTimers创建定时延迟2秒消息Message.Tick.INSTANCE
 *  b. client actor 接收到Message.Tick.INSTANCE消息后处理
 *    1. 打印“发送 process 请求”
 *    2. 发送Message.ProcessText("this is the text that will be analyzed")消息给ServiceRouter
 *    3. 这里使用的是回复client actor ref使用了消息适配器，需要回复的到的是Messages.Response,但是最终是将
 *    其值转换为Message.ServiceResponse消息
 *  c. StatsActor接收到Message.ProcessText消息调用process方法处理
 *    1. 打印 “委托请求”
 *    2. 将ProcessText实例中text进行单词切分
 *    3. 创建子 actor - StatsAggregator, 传递的参数包含全部处理数据的works、单词list、需要回复ActorRef(也就是client actor)
 *  d. 创建StatsAggregator actor并处理
 *    1. 将单词list size 存储到expectedResponses
 *    2. 需要回复的消息ActorRef<Message.Response>缓存到全局变量replyTo
 *    3. 通过 getContext().setReceiveTimeout(Duration.ofSeconds(3), Message.Timeout.INSTANCE);
 *    来设置处理消息超时，如果处理消息超时3秒，则发送Message.Timeout.INSTANCE消息给自己, 也就是失败处理
 *      失败处理：
 *      3.1 如果失败接收到Message.Timeout.INSTANCE消息处理
 *      3.2 调用onTimeout方法处理
 *      3.3 发送失败消息Message.JobFailed("服务不可以，请稍后重试")消息给client
 *      3.4 停止actor
 *      不超时则进行下面步骤
 *    4. 创建消息适配器， 消息的类型为essage.Processed.class， 适配处理是通过Processed的长度创建计算完成消息
 *    即Message.CalculationComplete
 *    5. 循环单词list,并告诉workers处理每个单词，需要回复是上面构建的消息适配器， 需要回复的是ActorRef<Message.Processed>
 *        但是实质里面的消息内容是Message.CalculationComplete
 *    6. workers负载分配work actor处理消息，接收到Message.Process消息
 *  e. Workers Actor接收到Message.Process消息并处理
 *    1. 调用process方法处理消息
 *    2. 打印 “Worker 处理请求 ” + 单词
 *    3. 先从全局常量map缓存中读取是否存储单词和对应长度
 *    4. 如缓存map中无此单词则将单词和长度加入到缓存map中
 *    5. 回复消息Message.Processed(单词，缓存中单词长度)
 *  d. StatsAggregator actor 通过消息适配器转换其实接收到的通过单词长度构建的计算完成消息Message.CalculationComplete.
 *     处理消息Message.CalculationComplete
 *     1. 通过一个list集合results存储每个单词的长度
 *     2. 如果list集合results的size等于单词list的size时候证明text的每个单词都已经全部分配给worker处理完毕了
 *      2.1 进行聚和计算求平均值
 *      2.2 将平均值发送给回复方也就是client actor
 *      2.3 停止client actor
 *    否则执行发送单词给work处理，循环e. d步骤，直到text单词全部处理完成
 *   e. client actor接收到处理完成的消息或超时异常失败
 *     1. client actor 通过消息适配器接收到Response成功或者失败子类，将子类值创建ServiceResponse的reslt
 *     构建了ServiceResponse消息
 *     2. 最终接收到ServiceResponse消息
 *     3. 打印 “处理后结果 ：” + 结果值
 *   end....
 *
 *   需要注意的地方，里面两处使用了消息适配器：
 *     1. client actor 发送消息告诉回复act ref 使用使用消息适配器，需要回复的消息是Messages.Response,但是最终是将
 *     其值转换为Message.ServiceResponse消息
 *     2. StatsAggregator actor 使用了消息适配器需要回复的消息是Message.Processed，最终将processed.length作为构造参数值
 *     构造了Message.CalculationComplete消息
 *
 *   需要关注地方：
 *     1. 使用 Routers.pool创建workers, 并将workers传递给service
 *     2. sercie又将其传递给子聚和actor用于发送处理
 *     3. clinet获取 service actor是通过路由方式Routers.group获取，这里不会真正创建actor
 *     4. 创建actor 使用了大量的定时延迟器进行格外处理，如定时清理全局变量的缓存，定时自动发送请求钩子消息等
 *
 *   需要补充地方：
 *    这里没有使用机器receptionist注册和订阅方式获取worker actor, 集群receptionist注册和订阅可参考transformation
 *    例子：worker注册
 *    context.getSystem().receptionist().tell(Receptionist.register(WORKER_SERVICE_KEY,
 *                             context.getSelf().narrow()));
 *    service订阅
 *    ActorRef<Receptionist.Listing> subscriptionAdapter =
 *                 context.messageAdapter(Receptionist.Listing.class, listing ->
 *                     new Massage.WorkersUpdated(listing.getServiceInstances(Worker.WORKER_SERVICE_KEY)));
 *         context.getSystem().receptionist().tell(Receptionist.subscribe(Worker.WORKER_SERVICE_KEY,
 *                 subscriptionAdapter));
 *    接收到注册后，会受到订阅的消息，然后发送WorkersUpdated消息，接收到此消息后，将workers添加到全局变量中
 *    WorkersUpdated是通过Set<ActorRef<TransformText>>存储的
 *
 *   获取workers方式区别：
 *   1. 这里使用Routers.pool方式创建，并且作为参数传递到service, 这里是因为集群角色一样，如果集群角色不一致，
 *   则不能使用此方式
 *
 *   transformation方式
 *   1. 集群订阅是推荐的方式，但是要格外类型-实例存储，并通过全局变量存储
 */
public class App {
    //创建服务密钥。给定的ID应该使用给定的协议唯一地定义服务。
    static final ServiceKey<Message.ProcessText> STATS_SERVICE_KEY =
            ServiceKey.create(Message.ProcessText.class, "StatsService");

    private static class RootBehavior {

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                //创建集群
                Cluster cluster = Cluster.get(context.getSystem());

                //如果角色为compute
                if (cluster.selfMember().hasRole("compute")) {
                    //在每个计算节点上，都有一个服务实例委派给N个本地工人
                    final int numberOfWorkers = context.getSystem().settings().config().getInt("stats-service.workers-per-node");
                    Behavior<Message.Process> workerPoolBehavior =
                            Routers.pool(numberOfWorkers,
                                    StatsWorker.create().<Message.Process>narrow())
                                    //通过使用一致的哈希路由消息。暂时未知干嘛~~~
                                    .withConsistentHashingRouting(1, process -> process.word);
                    //创建workers actor
                    ActorRef<Message.Process> workers =
                            context.spawn(workerPoolBehavior, "WorkerRouter");
                    //创建Service actor 把当前创建workers actor作为参数
                                      ActorRef<Message.CommandService> service =
                            context.spawn(StatsService.create(), "StatsService");

                    // published through the receptionist to the other nodes in the cluster
                    //通过前台发布到集群中的其他节点
                    context.getSystem().receptionist().tell(Receptionist.register(STATS_SERVICE_KEY, service.narrow()));
                }

                //如果角色为client
                if (cluster.selfMember().hasRole("client")) {
                    //一个路由器，它将跟踪注册到[[akka.actor.typed.receptionist.Receptionist]]的可用路由, 并通过随机选择遍历。
                    ActorRef<Message.ProcessText> serviceRouter =
                            context.spawn(Routers.group(STATS_SERVICE_KEY), "ServiceRouter");
                    context.spawn(StatsClient.create(serviceRouter), "Client");
                }

                return Behaviors.empty();
            });
        }
    }

    private static void startup(String role, int port) {
        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.remote.artery.canonical.port", port);
        overrides.put("akka.cluster.roles", Collections.singletonList(role));

        Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load("stats"));

       ActorSystem.create(RootBehavior.create(), "ClusterSystem", config);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            startup("compute", 25251);
            startup("compute", 25252);
            startup("compute", 0);
            startup("client", 0);
        } else {
            if (args.length != 2)
                throw new IllegalArgumentException("Usage: role port");
            startup(args[0], Integer.parseInt(args[1]));
        }
    }
}
