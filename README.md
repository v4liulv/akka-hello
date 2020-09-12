<h1>Akka初体验<h1>

<h2>目录</h2>

[[TOC]]

# 1. Akka简介

## 1.1. 为什么要用Akka

Akka提供可扩展的实时事务处理。

Akka是一个运行时与编程模型一致的系统，为以下目标设计：

* 垂直扩展（并发）
* 水平扩展（远程调用）
* 高容错

在Akka的世界里，只有一个内容需要学习和管理，具有高内聚和高一致的语义。

Akka是一种高度可扩展的软件，这不仅仅表现在性能方面，也表现在它所适用的应用的大小。Akka的核心，Akka-actor是非常小的，可以非常方便地放进你的应用中，提供你需要的异步无锁并行功能，不会有任何困扰。

你可以任意选择Akka的某些部分集成到你的应用中，也可以使用完整的包——Akka 微内核，它是一个独立的容器，可以直接部署你的Akka应用。随着CPU核数越来越多，即使你只使用一台电脑，Akka也可作为一种提供卓越性能的选择。 Akka还同时提供多种并发范型，允许用户选择正确的工具来完成工作。

## 1.2. 使用Akka带来的好处

* Akka提供一种Actor并发模型，其粒度比线程小很多，这意味着你可以在项目中使用大量的Actor。

* Akka提供了一套容错机制，允许在Actor出错时进行一些恢复或者重置操作

* AKKA不仅可以在单击上构建高并发程序，也可以在网络中构建分布式程序，并提供位置透明的Actor定位服务

## 1.3. Actor

actor是akka执行的基本单元，比线程更轻量级，使用akka可以忘掉线程了。事实上，线程调度已经被akka封装。

 actor生命周期

## 1.4. 消息投递

* Akka应用是有消息驱动的，消息是除了Actor之外最重要的核心组件。在Actor之前投递消息应该满足不可变性，也就是不便模式。

* 消息投递有3种策略：之多一次投递，至少一次投递，精确的消息投递。BUT ，没必要在akka层面保证消息的可靠性，一般在业务层在保证。

* Akka可以在一定程度上保证顺序性，但不具备传递性，见《java高并发程序设计 P295》。

## 1.5. 模块

Akka的模块化做得非常好，它为不同的功能提供了不同的Jar包。

* akka-actor-2.0.jar – 标准Actor, 有类型Actor，等等
* akka-remote-2.0.jar – 远程Actor
* akka-slf4j-2.0.jar – SLF4J事件处理监听器
* akka-testkit-2.0.jar – 用于测试Actor的工具包
* akka-kernel-2.0.jar – Akka微内核，可运行一个基本的最小应用服务器
* akka--mailbox-2.0.jar – Akka可容错邮箱

要查看每个Akka模块的jar包依赖见 [依赖](http://www.gtan.com/akka_doc/dev/building-akka.html#dependencies) 章节. 虽然不重要不过
akka-actor 没有外部依赖 (除了scala-library.jar JAR包).

## 1.6. 如何使用和部署Akka

Akka 可以有几种使用方式:

* 作为一个库: 以普通jar包的形式放在classpath上，或放到web应用中的 WEB-INF/lib位置

* 作为一个独立的应用程序，使用 Microkernel（微内核），自己有一个main类来初始化Actor系统

## 1.7. 参考资料

* 书籍《java高并发程序设计》
* [AKKA官方文档](https://akka.io/docs/)

# 2. 入门示例

主要学习akka的actor和remote模块，那么工程也分两个model:

* akkahello-actor 模块
* akkahello-remote 模块

## 2.1. 示例模块

> Actor模块目前有两个示例：
> * Hello Word 示例程序[HelloWordAkka](#22-HelloWordAkka)
> * 物联网示例[用例](#23-物联网示例[用例)


## 2.2. HelloWordAkka

本示例来自于[官方示例](http://doc.akka.io/docs/akka/2.6/java/guide/introduction.html), 文中找到 Using Akka with Maven 。点击“Akka Main in Java”下载示例。

示例程序包package：com.tcfuture.akka.actor.example.hello

### 2.2.1. HelloWord功能

该示例由三个actors组成:

* Greeter: 接收到某人的问候（Greet）命令，然后用一个（Greeted）来回应，以确认问候已经发生。
* GreeterBot: 接收来自Greeter的回复，并发送一些额外的问候信息，并收集回复，直到达到给定的最大数量的消息。
* GreeterMain: 监护的actor, 连接一切。

### 2.2.2. 定义Actor和消息

每个Actor为它可以接收的消息定义了一个类型T。Case类和Case对象是非常好的消息，因为它们是不可变的，并且支持模式匹配，我们将在Actor中利用这一点来匹配它接收到的消息。

HelloWord Actor中使用了三种不同的消息：

* Greet: 命令发生到Greeter actor的问候
* Greeted: 从Greeter actor确认问候已经发生的回复
* SayHello: 命令GreeterMain开始问候过程

在定义actor和他们的消息时，请记住以下建议:

* 由于消息是Actor的公共API，所以用好的名称和丰富的语义和特定领域的含义定义消息是一个很好的实践，即使它们只是包装了您的数据类型。这将使它更容易使用、理解和调试基于actor的系统。

* 消息应该是不可变的，因为它们在不同的线程之间共享。

* 通过静态工厂方法获取Actor的初始行为是一个很好的实践。

* 将actor关联的消息作为静态类放在AbstractBehaavior的类中是一个很好的实践。这使得更容易理解Actor期望和处理的消息类型。

#### 2.2.2.1. 最佳实践

让我们看看Greeter、GreeterBot和GreeterMain的实现是如何演示这些最佳实践的:

##### 2.2.2.1.1. The Greeter actor

``` java
public class Greeter extends AbstractBehavior<Greeter.Greet> {

  public static final class Greet {
    public final String whom;
    public final ActorRef<Greeted> replyTo;

    public Greet(String whom, ActorRef<Greeted> replyTo) {
      this.whom = whom;
      this.replyTo = replyTo;
    }
  }

  public static final class Greeted {
    public final String whom;
    public final ActorRef<Greet> from;

    public Greeted(String whom, ActorRef<Greet> from) {
      this.whom = whom;
      this.from = from;
    }

  }

  public static Behavior<Greet> create() {
    return Behaviors.setup(Greeter::new);
  }

  private Greeter(ActorContext<Greet> context) {
    super(context);
  }

  @Override
  public Receive<Greet> createReceive() {
    return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
  }

  private Behavior<Greet> onGreet(Greet command) {
    getContext().getLog().info("Hello {}!", command.whom);
    command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
    return this;
  }
}
```

这一小段代码定义了两种消息类型，一种用于命令Actor向某人打招呼，另一种用于Actor确认它已经这样做了。Greet类型不仅包含要打招呼的对象的信息，它还包含消息发送者提供的ActorRef，以便Greeter Actor可以发送回确认消息。

在newReceiveBuilder行为工厂的帮助下，Actor的行为被定义为Greeter AbstractBehavior。处理下一条消息会导致可能与此消息不同的新行为。可以通过
修改当前实例来更新状态，因为它是可变的。在这种情况下，我们不需要更新任何状态，所以我们返回这个没有任何字段更新，这意味着下一个行为“与当前行为相同”。

此行为处理的消息类型被声明为类Greet。通常，Actor处理不止一种特定的消息类型，然后就会有一个通用接口，actor可以处理的所有消息都可以实现这个接口。

在最后一行中，我们看到Greeter Actor向另一个Actor发送消息，这是使用tell方法完成的。它是一个不阻塞调用者线程的异步操作。

由于replyTo地址被声明为ActorRef<Greeted>类型，编译器将只允许我们发送这种类型的消息，其他用法将是编译器错误。

Actor接受的消息类型与所有应答类型一起定义了该Actor所说的协议; 在本例中，它是一个简单的请求-应答协议，但Actor可以在需要时建模任意复杂的协议。协议
与在一个精心包装的范围内实现它的行为捆绑在一起—Greeter类。

##### 2.2.2.1.2. The Greeter bot actor

``` java
package $package$;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class GreeterBot extends AbstractBehavior<Greeter.Greeted> {

    public static Behavior<Greeter.Greeted> create(int max) {
        return Behaviors.setup(context -> new GreeterBot(context, max));
    }

    private final int max;
    private int greetingCounter;

    private GreeterBot(ActorContext<Greeter.Greeted> context, int max) {
        super(context);
        this.max = max;
    }

    @Override
    public Receive<Greeter.Greeted> createReceive() {
        return newReceiveBuilder().onMessage(Greeter.Greeted.class, this::onGreeted).build();
    }

    private Behavior<Greeter.Greeted> onGreeted(Greeter.Greeted message) {
        greetingCounter++;
        getContext().getLog().info("Greeting {} for {}", greetingCounter, message.whom);
        if (greetingCounter == max) {
            return Behaviors.stopped();
        } else {
            message.from.tell(new Greeter.Greet(message.whom, getContext().getSelf()));
            return this;
        }
    }
}
```

请注意该Actor是如何使用实例变量管理计数器的。由于Actor实例一次只处理一条消息，因此不需要像synchronized或AtomicInteger这样的并发保护。

##### 2.2.2.1.3. The Greeter main actor

第三个Actor生成Greeter和GreeterBot，并开始交互，创建Actor，下面将讨论spawn所做的事情。

``` java
package $package$;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class GreeterMain extends AbstractBehavior<GreeterMain.SayHello> {

    public static class SayHello {
        public final String name;

        public SayHello(String name) {
            this.name = name;
        }
    }

    private final ActorRef<Greeter.Greet> greeter;

    public static Behavior<SayHello> create() {
        return Behaviors.setup(GreeterMain::new);
    }

    private GreeterMain(ActorContext<SayHello> context) {
        super(context);
        greeter = context.spawn(Greeter.create(), "greeter");
    }

    @Override
    public Receive<SayHello> createReceive() {
        return newReceiveBuilder().onMessage(SayHello.class, this::onSayHello).build();
    }

    private Behavior<SayHello> onSayHello(SayHello command) {
        ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.name);
        greeter.tell(new Greeter.Greet(command.name, replyTo));
        return this;
    }
}
```

### 2.2.3. 创建Actors

到目前为止，我们已经了解了Actor及其消息的定义。现在让我们更深入地了解一下位置透明性的强大功能，并看看如何创建Actor实例。

#### 2.2.3.1. 位置透明度的力量

在Akka中，不能使用new关键字创建Actor的实例。相反，您可以使用工厂spawn方法创建Actor实例。Spawn不返回一个actor实例，而是返回一个引用akka.actor.typed. ActorRef，它指向actor实例。这种级别的间接增加了分布式系统的强大功能和灵活性。

在Akka的位置并不重要。位置透明性意味着ActorRef可以在保留相同语义的同时，表示正在运行的actor在进程中或在远程机器上的实例。如果需要，运行时可以通过ch优化系统。

#### 2.2.3.2. Akka-ActorSystem

ActorSystem是Akka的初始入口点，通常每个应用程序只创建一个ActorSystem。ActorSystem有一个名字和一个监控Actor。应用程序的引导通常在监护人Actor中完成。

这个示例演员是GreeterMain。

``` java
final ActorSystem<GreeterMain.SayHello> greeterMain = ActorSystem.create(GreeterMain.create(), "helloakka");
```

使用Behaviors.setup引导应用程序

``` java
package $package$;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;

public class GreeterMain extends AbstractBehavior<GreeterMain.SayHello> {

    public static class SayHello {
        public final String name;

        public SayHello(String name) {
            this.name = name;
        }
    }

    private final ActorRef<Greeter.Greet> greeter;

    public static Behavior<SayHello> create() {
        return Behaviors.setup(GreeterMain::new);
    }

    private GreeterMain(ActorContext<SayHello> context) {
        super(context);
        greeter = context.spawn(Greeter.create(), "greeter");
    }

    @Override
    public Receive<SayHello> createReceive() {
        return newReceiveBuilder().onMessage(SayHello.class, this::onSayHello).build();
    }

    private Behavior<SayHello> onSayHello(SayHello command) {
        ActorRef<Greeter.Greeted> replyTo =
                getContext().spawn(GreeterBot.create(3), command.name);
        greeter.tell(new Greeter.Greet(command.name, replyTo));
        return this;
    }
}
```

#### 2.2.3.3. 生产子Actors

其他的Actor是使用ActorContext上的spawn方法创建的。GreeterMain在启动时以这种方式创建一个Greeter actor，并在每次收到SayHello消息时创建一个新的GreeterBot。

``` java
greeter = context.spawn(Greeter.create(), "greeter");
ActorRef<Greeter.Greeted> replyTo =
        getContext().spawn(GreeterBot.create(3), command.name);
greeter.tell(new Greeter.Greet(command.name, replyTo));
```

#### 2.2.3.4. 异步通信

Actor是被动的和消息驱动的。Actor在收到消息之前不会做任何事情。Actor使用异步消息进行通信。这确保了发送方不会一直等待收件人处理它们的消息。相反，发件人将邮件放入收件人的邮箱，可以自由地做其他工作。Actor的邮箱本质上是一个具有排序语义的消息队列。同一Actor发送的多个消息的顺序被保留，但是可以与另一个Actor发送的消息交织在一起。

您可能想知道，当Actor不处理消息时，也就是在做实际的工作时，它在做什么? 它处于挂起状态，在这种状态下，它不消耗除内存以外的任何资源。再次展示了Actor轻量级、高效的特性。

#### 2.2.3.5. 向Actor发送消息

要将消息放入Actor的邮箱，请使用ActorRef上的tell方法。例如，Hello World的主类向Greeter Actor发送这样的消息:

``` java
greeterMain.tell(new GreeterMain.SayHello("Charles"));
```

Greeter Actor也会向Printer Actor发送一条消息

``` java
command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
```

### 2.2.4. Actor测试

Hello World示例中的测试演示了JUnit框架的使用。测试覆盖不完整。它展示了如何测试Actor代码，并提供了一些基本概念。

``` java
package $package$;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;

public class AkkaQuickstartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testGreeterActorSendingOfGreeting() {
        TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
        ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
        underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
        testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
    }
}
```

#### 2.2.4.1. 测试类的定义

``` java
public class AkkaQuickstartTest {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();
```

通过使用TestKitJunitResource JUnit规则，可以包含对JUnit的支持。这将自动创建并清除ActorTestKit。要了解如何使用testkit，请直接查看[完整的文档](https://doc.akka.io/docs/akka/2.6/typed/testing-async.html)。

##### 2.2.4.1.1. 测试方法

测试使用TestProbe来询问和验证预期的行为。让我们看一看源代码片段:

``` java
@Test
public void testGreeterActorSendingOfGreeting() {
    TestProbe<Greeter.Greeted> testProbe = testKit.createTestProbe();
    ActorRef<Greeter.Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
    underTest.tell(new Greeter.Greet("Charles", testProbe.getRef()));
    testProbe.expectMessage(new Greeter.Greeted("Charles", underTest));
}
```

有了对TestProbe的引用后，我们将其作为Greet消息的一部分传递给Greeter。然后，我们验证Greeter回应说问候已经发生。

示例代码只是触及了ActorTestKit中可用功能的表面。Akka测试详细示例请查看[完整的测试概述](https://developer.lightbend.com/guides/akka-quickstart-java/testing-actors.html)相关内容。

## 2.3. 物联网示例用例

### 2.3.1. 示例简介

#### 2.3.1.1. 示例思路

写散文时，最难的部分往往是写开头几句。在开始构建Akka系统时，也有类似的“空白画布”的感觉。你可能会想:哪个应该是第一个Actor?它应该住在哪里?它应该做什么?幸运的是，与散文体不同的是，已有的最佳实践可以指导我们完成这些初始步骤。在本文档的其余部分中，我们将研究一个简单Akka应用程序的核心逻辑，向您介绍Actor，并向您展示如何使用它们来制定解决方案。这个示例演示了一些常见的模式，这些模式将帮助您启动Akka项目。

#### 2.3.1.2. 先决条件

您应该已经下载[Hello Word Akka示例](https://gitlab.tcfuture.tech/akka/akka-hello)程序中Actor模块的`java/com/tcfuture/akka/actor/hello`包，并参考本文档[HelloWordAkka示例介绍](#33-HelloWordAkka)。您将使用它作为一个种子项目，并添加本教程中描述的功能。

> **请注意**  
Akka模块的Java和Scala dsl都捆绑在同一个JAR中。为了获得平稳的开发体验，在使用Eclipse或IntelliJ这样的IDE时，您可以禁用自动导入器，使其在使用Scala时不建议javadsl导入，或者相反。看到IDE的技巧。

#### 2.3.1.3. 示例范围

在本文中，我们将使用Akka构建物联网(IoT)系统的一部分，该系统报告来自客户家中安装的传感器设备的数据。这个例子着重于**温度读数**。目标用例允许客户登录并查看来自家中不同区域的最后报告的温度。您可以想象，这种传感器还可以收集相对湿度或其他有趣的数据，应用程序可能支持读取和更改设备配置，甚至可能在传感器状态超出特定范围时向房主发出警报。

在真实的系统中，应用程序将通过移动应用程序或浏览器向客户公开。本文只关注存储温度的核心逻辑，这些温度可以通过网络协议(如HTTP)调用。它还包括编写测试来帮助您适应并熟练地使用测试角色。

#### 2.3.1.4. 组成部分

应用程序由两个主要组件组成:

* **设备数据收集:** — 维护远程设备的本地表示。一个家庭的多个传感器设备被组织成一个设备。

* **用户控制面板:** — 定期从登录用户家中的设备收集数据，并以报告的形式显示结果。

#### 2.3.1.5. 体系结构

下图演示了示例应用程序体系结构。因为我们对每个传感器设备的状态感兴趣，所以我们将设备建模为Actor。运行中的应用程序将根据需要创建尽可能多的设备Actor和设备组实例。

![123](https://liulv.work/images/img/123.png)


#### 2.3.1.6. 应用技能

示例将使用到Actor的那些开发技能：

* Actor层次结构以及它如何影响Actor的行为
* 如何为Actor选择合适的粒度
* 如何将协议定义为消息
* 典型的谈话方式

### 2.3.2. Actor架构

#### 2.3.2.1. Maven依赖

```xml
<properties>
  <akka.version>2.6.8</akka.version>
  <scala.binary.version>2.13</scala.binary.version>
</properties>
<dependency>
  <groupId>com.typesafe.akka</groupId>
  <artifactId>akka-actor-typed_${scala.binary.version}</artifactId>
  <version>${akka.version}</version>
</dependency>
```

#### 2.3.2.2. 介绍

使用Akka使您不必为actor系统创建基础设施，也不必编写控制基本行为所需的底层代码。为了理解这一点，让我们看看您在代码中创建的Actor与Akka在内部为您创建和管理的Actor之间的关系，Actor生命周期和失败处理。

#### 2.3.2.3. Actor层次结构

在Akka中Actor永远属于父Actor。您可以通过调用ActorContext.spawn()来创建一个actor。创建者Actor成为新创建的子Actor的父Actor。您可能会问，您创建的第一个actor的父元素是谁?

如下所示，所有Actors都有一个共同的父Actor，即用户监护人，它是在启动ActorSystem时定义和创建的。正如我们在《快速入门指南》中提到的，actor的创建将返回一个引用，该引用是一个有效的URL。例如，如果我们从用户guardian中使用`context.spawn(someBehavior, "someActor")`创建一个名为someActor的actor，其引用将包括路径`/user/someActor`。

![20200802224604](https://liulv.work/images/img/20200802224604.png)

实际上，在启动第一个Actor之前，Akka已经在系统中创建了两个Actor。这些内置Actor的名称包含监护人(guardian)。监控Actor包括:

* / 所谓的根监护者（guardian）- 它是系统中所有角色的父级，也是系统本身终止时最后一个停止的角色。

* /system 系统的监护者 - Akka或构建在Akka之上的其他库可以在系统名称空间中创建Actor。

* /user 用户的监护者 - 这是您为启动应用程序中的所有其他Actor而提供的顶级Actor。

查看actor层次结构的最简单方法是打印ActorRef实例。在这个小实验中，我们创建一个actor，打印它的引用，创建这个actor的子元素，然后打印子元素的引用。我们从Hello World项目开始，如果您还没有下载它，请从Lightbend技术中心下载Quickstart项目。

在Hello World项目中，导航到`com.tcfuture.akka.actor.architecture`示例包，并为下面代码片段中的每个类创建一个Java文件，并复制各自的内容。保存文件并运行`com.tcfuture.akka.actor.example.hello.PrintMyActorRefActor.ActorHierarchyExperiments`从构建工具或IDE中观察输出。

```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class PrintMyActorRefActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(PrintMyActorRefActor::new);
  }

  private PrintMyActorRefActor(ActorContext<String> context) {
    super(context);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("printit", this::printIt).build();
  }

  private Behavior<String> printIt() {
    ActorRef<String> secondRef = getContext().spawn(Behaviors.empty(), "second-actor");
    System.out.println("Second: " + secondRef);
    return this;
  }
}

class Main extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(Main::new);
  }

  private Main(ActorContext<String> context) {
    super(context);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("start", this::start).build();
  }

  private Behavior<String> start() {
    ActorRef<String> firstRef = getContext().spawn(PrintMyActorRefActor.create(), "first-actor");

    System.out.println("First: " + firstRef);
    firstRef.tell("printit");
    return Behaviors.same();
  }
}

public class ActorHierarchyExperiments {
  public static void main(String[] args) {
    ActorRef<String> testSystem = ActorSystem.create(Main.create(), "testSystem");
    testSystem.tell("start");
  }
}
```

请注意消息要求第一个actor执行其工作的方式。我们使用父节点的引用发送消息:`firstRef.tell("printit", ActorRef.noSender())`。当代码执行时，输出包括对第一个actor的引用以及它作为printit大小写的一部分创建的子元素。您的输出应该类似于以下内容:

```log
First: Actor[akka://testSystem/user/first-actor#1053618476]
Second: Actor[akka://testSystem/user/first-actor/second-actor#-1544706041]
```

输出打印如下：

![20200802230207](https://liulv.work/images/img/20200802230207.png)

注意引用的结构:

* 两个路径都从`akka://testSystem/`开始。因为所有的actor引用都是有效的url，所以akka://是协议字段的值。

* 接下来，就像在万维网上一样，URL标识系统。在本例中，系统被命名为testSystem，但它可以是任何其他名称。如果启用了多个系统之间的远程通信，URL的这一部分将包含主机名，以便其他系统可以在网络上找到它。

* 因为第二个Actor的引用包含路径`/first-actor/`，所以它将其标识为第一个Actor的子元素。

* actor引用的最后一部分#1053618476或#-1544706041是一个在大多数情况下可以忽略的惟一标识符。

现在您已经了解了actor层次结构的样子，您可能想知道:为什么我们需要这个层次结构?它是用来做什么的?

层次结构的一个重要角色是安全地管理Actor的生命周期。接下来让我们考虑这个问题，看看这些知识如何帮助我们编写更好的代码。

#### 2.3.2.4. Actor生命周期

Actor在创建时出现，然后在用户请求时停止。当一个Actor被停止时，它的所有子Actor也会被递归地停止。此行为极大地简化了资源清理工作，并有助于避免资源泄漏，比如由于打开套接字和文件而导致的泄漏。事实上，在处理低级多线程代码时，一个经常被忽视的困难是各种并发资源的生命周期管理。

为了停止一个Actor，推荐的模式是在actor内部返回Behaviors.stopped()来停止它自己，通常作为对一些用户定义的停止消息的响应，或者当actor完成它的工作时。从父节点调用context.stop(childRef)来停止子节点从技术上讲是可行的，但是不可能通过这种方式停止任意的(非子节点)节点。

Akka actor API公开了一些生命周期信号，例如PostStop是在actor停止之后发送的。在此之后不处理任何消息。

让我们在一个简单的实验中使用PostStop生命周期信号来观察停止Actor时的行为。首先，在您的项目中添加以下2个actor类:

```java
class StartStopActor1 extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(StartStopActor1::new);
  }

  private StartStopActor1(ActorContext<String> context) {
    super(context);
    System.out.println("first started");

    context.spawn(StartStopActor2.create(), "second");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
        .onMessageEquals("stop", Behaviors::stopped)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<String> onPostStop() {
    System.out.println("first stopped");
    return this;
  }
}

class StartStopActor2 extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(StartStopActor2::new);
  }

  private StartStopActor2(ActorContext<String> context) {
    super(context);
    System.out.println("second started");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
  }

  private Behavior<String> onPostStop() {
    System.out.println("second stopped");
    return this;
  }
}
```

创建一个像上面那样的“main”类启动actor，然后发送一个“stop”消息:

```java
ActorRef<String> first = context.spawn(StartStopActor1.create(), "first");
first.tell("stop");
```

运行结果如下：

```
first started
second started
second stopped
first stopped
```

![20200803002422](https://cdn.jsdelivr.net/gh/v4liulv/PicPed/img/20200803002422.png)

当我们先停止actor时，它会先停止它的child actor，然后再停止它自己。这种顺序是严格的，子节点的所有后停止信号都在父节点的后停止信号处理之前处理。

#### 2.3.2.5. 故障处理

父Actor和子Actor在整个生命周期中都是相互联系的。每当一个Actor失败时(抛出一个异常或一个未处理的异常从接收中冒出)，失败信息就会传播到监督策略，然后由其决定如何处理由Actor引起的异常。监控策略通常由父actor在生成子actor时定义。这样，父母就成了孩子的监督者。默认的管理策略是阻止孩子。如果你不定义策略，所有的失败都会导致停止。

让我们通过一个简单的实验来观察重启监管策略。添加以下类到你的项目，就像你做的前面的:

```java
class SupervisingActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(SupervisingActor::new);
  }

  private final ActorRef<String> child;

  private SupervisingActor(ActorContext<String> context) {
    super(context);
    child =
        context.spawn(
            Behaviors.supervise(SupervisedActor.create()).onFailure(SupervisorStrategy.restart()),
            "supervised-actor");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("failChild", this::onFailChild).build();
  }

  private Behavior<String> onFailChild() {
    child.tell("fail");
    return this;
  }
}

class SupervisedActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(SupervisedActor::new);
  }

  private SupervisedActor(ActorContext<String> context) {
    super(context);
    System.out.println("supervised actor started");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
        .onMessageEquals("fail", this::fail)
        .onSignal(PreRestart.class, signal -> preRestart())
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private Behavior<String> fail() {
    System.out.println("supervised actor fails now");
    throw new RuntimeException("I failed!");
  }

  private Behavior<String> preRestart() {
    System.out.println("second will be restarted");
    return this;
  }

  private Behavior<String> postStop() {
    System.out.println("second stopped");
    return this;
  }
}
```

并运行：

```java
ActorRef<String> supervisingActor =
    context.spawn(SupervisingActor.create(), "supervising-actor");
supervisingActor.tell("failChild");
```

您应该会看到类似如下的输出:

```
First: Actor[akka://ActorSystem/user/supervising-actor#637280321]
supervised actor started
supervised actor fails now
second will be restarted
[2020-08-03 00:29:08,938] [ERROR] [akka.actor.typed.Behavior$] [ActorSystem-akka.actor.default-dispatcher-3] [akka://ActorSystem/user/supervising-actor/supervised-actor] - Supervisor RestartSupervisor saw failure: I failed!
java.lang.RuntimeException: I failed!
	at com.tcfuture.akka.actor.architecture.SupervisedActor.fail(FailureHandling.java:103)
	at akka.actor.typed.javadsl.ReceiveBuilder$$anon$2.apply(ReceiveBuilder.scala:88)
	at akka.actor.typed.javadsl.ReceiveBuilder$$anon$2.apply(ReceiveBuilder.scala:86)
	at akka.actor.typed.javadsl.BuiltReceive.receive(ReceiveBuilder.scala:213)
	at akka.actor.typed.javadsl.BuiltReceive.receiveMessage(ReceiveBuilder.scala:204)
	at akka.actor.typed.javadsl.Receive.receive(Receive.scala:53)
	at akka.actor.typed.javadsl.AbstractBehavior.receive(AbstractBehavior.scala:64)
	at akka.actor.typed.Behavior$.interpret(Behavior.scala:274)
	at akka.actor.typed.Behavior$.interpretMessage(Behavior.scala:230)
	at akka.actor.typed.internal.InterceptorImpl$$anon$2.apply(InterceptorImpl.scala:57)
	at akka.actor.typed.internal.RestartSupervisor.aroundReceive(Supervision.scala:263)
	at akka.actor.typed.internal.InterceptorImpl.receive(InterceptorImpl.scala:85)
	at akka.actor.typed.Behavior$.interpret(Behavior.scala:274)
	at akka.actor.typed.Behavior$.interpretMessage(Behavior.scala:230)
	at akka.actor.typed.internal.adapter.ActorAdapter.handleMessage(ActorAdapter.scala:129)
	at akka.actor.typed.internal.adapter.ActorAdapter.aroundReceive(ActorAdapter.scala:106)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:577)
	at akka.actor.ActorCell.invoke(ActorCell.scala:547)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:270)
	at akka.dispatch.Mailbox.run(Mailbox.scala:231)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:243)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
supervised actor started
```

我们看到，在失败后，被监督的Actor被停止并立即重新启动。我们还会看到一个日志条目报告所处理的异常，在本例中是我们的测试异常。在本例中，我们还使用了PreRestart信号，该信号在重启之前被处理。


#### 2.3.2.6. 总结

我们已经了解了Akka是如何在层次结构中管理Actor的，在层次结构中父母监督他们的孩子并处理异常。我们看到了如何创建一个非常简单的actor和child。接下来，我们将通过对从设备Actor获取信息所需的通信进行建模，将这些知识应用到示例用例中。稍后，我们将处理如何在组中管理Actor。

### 2.3.3. 物联网应用Actor

#### 2.3.3.1. 介绍

了解了Actor的层次结构和行为之后，剩下的问题是如何将物联网系统的顶层组件映射到Actor。用户监护人(/ root guardian)可以是代表整个应用程序的Actor。换句话说，我们将在我们的物联网系统中有一个单一的顶级Actor。创建和管理设备和仪表板的组件将是该Actor的子组件。这允许我们将示例用例架构图重构为一个角色树:

![arch_tree_diagram](https://cdn.jsdelivr.net/gh/v4liulv/PicPed/img/arch_tree_diagram.png)

我们可以用几行代码定义第一个ActorIotSupervisor。开始你的教程应用:

在com.tcfuture.akka.actor.iot包中创建一个新的IotSupervisor源文件。示例包中将以下代码粘贴到新文件中以定义IotSupervisor。

```java
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class IotSupervisor extends AbstractBehavior<Void> {

  public static Behavior<Void> create() {
    return Behaviors.setup(IotSupervisor::new);
  }

  private IotSupervisor(ActorContext<Void> context) {
    super(context);
    context.getLog().info("IoT Application started");
  }

  // No need to handle any messages
  @Override
  public Receive<Void> createReceive() {
    return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
  }

  private IotSupervisor onPostStop() {
    getContext().getLog().info("IoT Application stopped");
    return this;
  }
}
```

代码类似于我们在前面的实验中使用的actor示例，但是请注意，我们不是使用println()，而是通过context.getLog()使用Akka的内置日志记录工具。

要提供创建actor系统的主入口点，请向新的IotMain类添加以下代码。

```java
import akka.actor.typed.ActorSystem;

public class IotMain {

  public static void main(String[] args) {
    // Create ActorSystem and top level supervisor
    ActorSystem.create(IotSupervisor.create(), "iot-system");
  }
}
```

除了记录它的启动，应用程序几乎不做其他事情。但是，我们已经有了第一个Actor，并且准备添加其他Actor。

运行结果如下：

```log
[2020-08-03 00:42:51,904] [INFO] [com.tcfuture.akka.actor.example.iot.IotSupervisor] [iot-system-akka.actor.default-dispatcher-3] [akka://iot-system/user] - IoT Application started
```

#### 2.3.3.2. 接下来怎么做

在接下来的章节中，我们将逐步发展应用程序，通过:
1. 为设备创建表示形式。
2. 创建设备管理组件。
3. 向设备组添加查询功能。

### 2.3.4. 设备Working

#### 2.3.4.1. 介绍

在前面的主题中，我们解释了如何查看大型的Actor系统，也就是说，组件应该如何表示，Actor应该如何在层次结构中进行安排。在本部分中，我们将通过实现设备Actor来了解小型Actor。

如果我们使用对象，我们通常会将API设计为接口，即由实际实现填充的抽象方法集合。在Actor的世界中，协议取代了接口。虽然在编程语言中不可能形式化通用协议，但我们可以组成它们最基本的元素，消息。因此，我们将从识别我们想要发送给设备Actor的消息开始。

通常情况下，消息可以分为类别或模式。通过识别这些模式，您将发现在它们之间进行选择和实现将变得更加容易。第一个示例演示了请求-响应消息模式。

#### 2.3.4.2. 设备的标识消息

设备Actor的任务很简单:

* 收集温度测量
* 当被问及时，报告最后测量的温度

然而，设备可能在没有立即测量温度的情况下启动。因此，我们需要考虑温度不存在的情况。这还允许我们在没有写入部分的情况下测试actor的查询部分，因为设备actor可以报告一个空结果。所以使用我们使用**Optional<Double>**来记录温度不直接使用Double，允许为空结果。

```java
public class Device {

  public interface Command {}

  public static final class ReadTemperature implements Command {
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(ActorRef<RespondTemperature> replyTo) {
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final Optional<Double> value;

    public RespondTemperature(Optional<Double> value) {
      this.value = value;
    }
  }
}
```

请注意，ReadTemperature消息包含设备Actor在响应请求时将使用的ActorRef<RespondTemperature>。

这两条消息似乎涵盖了所需的功能。但是，我们选择的方法必须考虑到应用程序的分布式特性。虽然与本地JVM上的actor通信的基本机制与与远程actor通信的基本机制是相同的，但我们需要记住以下几点:

* 由于网络链路带宽和消息大小等因素也会起作用，因此在本地消息和远程消息的传递延迟方面会有明显的差异。

* 可靠性是一个问题，因为远程消息发送涉及到更多的步骤，这意味着可能出现更多的错误。

* 本地发送将传递对同一JVM中的消息的引用，而对发送的基础对象没有任何限制，而远程传输将对消息大小进行限制。

此外，虽然在同一个JVM中发送消息要可靠得多，但如果actor在处理消息时由于程序员错误而失败，其效果与在处理消息时由于远程主机崩溃而导致远程网络请求失败的效果相同。即使在这两种情况下，服务在一段时间后恢复(Actor由其监管者重新启动，主机由操作员或监视系统重新启动)，个别请求在崩溃期间会丢失。因此，将Actor编写成每条消息都有可能丢失的样子是一种安全而悲观的做法。

但是为了进一步理解协议中灵活性的需要，考虑Akka消息顺序和消息传递保证将有所帮助。Akka为消息发送提供以下行为:

* 最多一次交货，也就是说，不保证交货。
* 按发送方、接收方对维护消息顺序。

下面几节将更详细地讨论这种行为:

* [Message delivery](https://doc.akka.io/docs/akka/current/typed/guide/tutorial_3.html#message-delivery)
* [Message ordering](https://doc.akka.io/docs/akka/current/typed/guide/tutorial_3.html#message-ordering)

#### 2.3.4.3. 消息传递

消息传递子系统提供的传递语义通常可以分为以下几类:

* At-most-once delivery —— 每条消息被发送零次或一次;从更因果的角度来说，它意味着信息可能会丢失，但不会被复制。

* At-least-once delivery —— 可能会多次尝试传递每条消息，直到至少有一条成功为止;同样，从更因果的角度来说，这意味着信息可以被复制，但不会丢失。

* Exactly-once delivery —— 每条信息只传递一次给收件人;信息既不能丢失也不能复制。

第一种行为是Akka所使用的，它是最便宜的，并且具有最高的性能。它的实现开销最小，因为它可以以“发完就忘记”的方式完成，而无需在发送端或传输机制中保留状态。

第二种，至少一次成功，需要重试以弥补运输损失。这增加了在发送端保持状态和在接收端拥有一个确认机制的开销。

第三种，确切地说，一次交付必须成功不丢失是最昂贵的，并导致最差的性能:除了至少一次交付所增加的开销之外，它还要求在接收端保持状态，以便过滤重复交付。

在一个行为人系统中，我们需要确定保证的确切含义-在什么情况下系统认为交付已完成:

1. 什么时候在网络上发送消息?
2. 目标actor的主机何时接收到消息?
3. 何时将消息放入目标Actor的邮箱?
4. 消息目标actor何时开始处理消息?
5. 目标actor何时成功处理了消息?

大多数声称保证交付的框架和协议实际上提供了类似于第4点和第5点的内容。虽然这听起来很合理，但它真的有用吗?为了理解其中的含义，考虑一个简单的实际示例:一个用户试图下订单，而我们只想在订单实际上在orders数据库的磁盘上时声明它已成功处理。

如果我们依赖于消息的成功处理，那么一旦订单被提交给负责验证、处理并将其放入数据库的内部API, actor就会报告成功。不幸的是，在API被调用后立即会发生以下任何情况:

* 主机可能会崩溃。
* 反序列化可以失败。
* 验证失败。
* 数据库可能不可用。
* 可能会出现编程错误。

这说明了交付的保证不会转换为域级别的保证。我们只希望在订单实际上已完全处理并持久化后报告成功。惟一能够报告成功的实体是应用程序本身，因为只有它了解所需的域保证。没有一个通用的框架能够指出特定领域的细节，以及在该领域中什么被认为是成功的。

在这个特定的示例中，我们只希望在数据库写入成功后发出成功的信号，其中数据库确认订单现在已安全存储。由于这些原因，Akka将保证的责任推给了应用程序本身，也就是说，您必须自己使用Akka提供的工具来实现它们。这使您可以完全控制您想要提供的保证。现在，让我们考虑一下Akka提供的消息顺序，它可以方便地推断应用程序逻辑。

#### 2.3.4.4. 消息排序

在Akka中，对于给定的一对Actor，从第一个Actor直接发送到第二个Actor的消息将不会被无序地接收。这个词直接强调了这种保证只适用于与tell操作员直接发送到最终目的地，但不适用于雇用调解人。

如果：

* Actor A1 sends messages M1, M2, M3 to A2.
* Actor A3 sends messages M4, M5, M6 to A2.

这意味着，对于Akka消息:

* 如果交付M1，必须在M2和M3之前交付。
* 如果M2交付，必须在M3之前交付。
* 如果要交付M4，则必须在M5和M6之前交付。
* 如果要交付M5，则必须在M6之前交付。
* A2可以看到来自A1的消息与来自A3的消息交织在一起。
* 由于没有传递的保证，任何消息都可能被丢弃，也就是说没有到达A2。

这些保证实现了良好的平衡:让来自一个Actor的消息按顺序到达有利于构建易于推理的系统，而另一方面，允许来自不同Actor的消息交错到达，为Actor系统的有效实现提供了足够的自由。

有关送货保证的详情，请参阅[参考网页](https://doc.akka.io/docs/akka/current/general/message-delivery-reliability.html)

#### 2.3.4.5. 设备消息增加灵活性

我们的第一个查询协议是正确的，但是没有考虑到分布式应用程序的执行。如果我们想在查询设备Actor(因为超时请求)的Actor中实现resends，或者如果我们想查询多个Actor，我们需要能够关联请求和响应。因此，我们在我们的消息中增加了一个字段，以便请求者提供一个ID即requestId(我们将在后面的步骤中添加这个代码到我们的应用中):

```java
public class Device extends AbstractBehavior<Device.Command> {
  public interface Command {}

  public static final class ReadTemperature implements Command {
    final long requestId;
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
      this.requestId = requestId;
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final long requestId;
    final Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
      this.requestId = requestId;
      this.value = value;
    }
  }
}
```

#### 2.3.4.6. 实现设备actor及其读取协议

正如我们在Hello World示例中了解到的，每个Actor都定义了它将接受的消息类型。我们的设备Actor有责任为给定查询的响应使用相同的ID参数，这将使它看起来如下所示。

```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.Optional;

public class Device extends AbstractBehavior<Device.Command> {
  public interface Command {}

  public static final class ReadTemperature implements Command {
    final long requestId;
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
      this.requestId = requestId;
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final long requestId;
    final Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
      this.requestId = requestId;
      this.value = value;
    }
  }

  public static Behavior<Command> create(String groupId, String deviceId) {
    return Behaviors.setup(context -> new Device(context, groupId, deviceId));
  }

  private final String groupId;
  private final String deviceId;

  private Optional<Double> lastTemperatureReading = Optional.empty();

  private Device(ActorContext<Command> context, String groupId, String deviceId) {
    super(context);
    this.groupId = groupId;
    this.deviceId = deviceId;

    context.getLog().info("Device actor {}-{} started", groupId, deviceId);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(ReadTemperature.class, this::onReadTemperature)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onReadTemperature(ReadTemperature r) {
    r.replyTo.tell(new RespondTemperature(r.requestId, lastTemperatureReading));
    return this;
  }

  private Device onPostStop() {
    getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
    return this;
  }
}
```

在代码中请注意:

* 静态创建方法定义了如何为设备Actor构造行为。这些参数包括设备的ID和它所属的组，我们将在后面使用。

* 我们前面讨论的消息是在前面显示的Device类中定义的。

* 在设备类中，lastTemperatureReading的值最初设置为option.empty()，如果被查询，Actor将报告它。

#### 2.3.4.7. 测试Actor

基于上面的Actor，在项目的com.tcfuture.akka.actor.iot包下我们可以编写一个测试。将以下代码添加到DeviceTest.java文件中。

```java
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import org.junit.ClassRule;
import org.junit.Test;
import java.util.Optional;

import static org.junit.Assert.assertEquals;


public class DeviceTest {

  @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();

  @Test
  public void testReplyWithEmptyReadingIfNoTemperatureIsKnown() {
    TestProbe<Device.RespondTemperature> probe =
        testKit.createTestProbe(Device.RespondTemperature.class);
    ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));
    deviceActor.tell(new Device.ReadTemperature(42L, probe.getRef()));
    Device.RespondTemperature response = probe.receiveMessage();
    assertEquals(42L, response.requestId);
    assertEquals(Optional.empty(), response.value);
  }
}
```

测试方法执行结果

```log
[2020-08-03 01:45:52,355] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-9] [akka://DeviceTest/user/$a] - Device actor group-device started
[2020-08-03 01:45:52,364] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-9] [akka://DeviceTest/user/$a] - Reading temperature read Optional.empty with 42
```

现在，当Actor收到来自传感器的消息时，它需要一种方法来改变温度的状态。

#### 2.3.4.8. 添加写协议

写协议的目的是在actor接收到包含温度的消息时更新currentTemperature字段。同样，我们很容易将write协议定义为一个非常简单的消息，如下所示:

```java
public static final class RecordTemperature implements Command {
  final double value;

  public RecordTemperature(double value) {
    this.value = value;
  }
}
```

但是，这种方法没有考虑到记录温度消息的发送者永远不能确定该消息是否已被处理。我们已经看到，Akka并不保证这些消息的传递，而是让应用程序提供成功通知。在我们的情况下，一旦我们更新了上次的温度记录，我们就会向发送者发送确认信息，例如，回复一条温度恢复记录信息。与温度查询和响应一样，包含请求ID字段以提供最大的灵活性也是一个好主意。

```java
public static final class RecordTemperature implements Command {
  final long requestId;
  final double value;
  final ActorRef<TemperatureRecorded> replyTo;

  public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
    this.requestId = requestId;
    this.value = value;
    this.replyTo = replyTo;
  }
}

public static final class TemperatureRecorded {
  final long requestId;

  public TemperatureRecorded(long requestId) {
    this.requestId = requestId;
  }
}
```

#### 2.3.4.9. Actor读写消息

把读写协议放在一起，设备Actor看起来像下面的例子:

```java
import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Device extends AbstractBehavior<Device.Command> {

  public interface Command {}

  public static final class RecordTemperature implements Command {
    final long requestId;
    final double value;
    final ActorRef<TemperatureRecorded> replyTo;

    public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
      this.requestId = requestId;
      this.value = value;
      this.replyTo = replyTo;
    }
  }

  public static final class TemperatureRecorded {
    final long requestId;

    public TemperatureRecorded(long requestId) {
      this.requestId = requestId;
    }
  }

  public static final class ReadTemperature implements Command {
    final long requestId;
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
      this.requestId = requestId;
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final long requestId;
    final Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
      this.requestId = requestId;
      this.value = value;
    }
  }

  public static Behavior<Command> create(String groupId, String deviceId) {
    return Behaviors.setup(context -> new Device(context, groupId, deviceId));
  }

  private final String groupId;
  private final String deviceId;

  private Optional<Double> lastTemperatureReading = Optional.empty();

  private Device(ActorContext<Command> context, String groupId, String deviceId) {
    super(context);
    this.groupId = groupId;
    this.deviceId = deviceId;

    context.getLog().info("Device actor {}-{} started", groupId, deviceId);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(RecordTemperature.class, this::onRecordTemperature)
        .onMessage(ReadTemperature.class, this::onReadTemperature)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onRecordTemperature(RecordTemperature r) {
    getContext().getLog().info("Recorded temperature reading {} with {}", r.value, r.requestId);
    lastTemperatureReading = Optional.of(r.value);
    r.replyTo.tell(new TemperatureRecorded(r.requestId));
    return this;
  }

  private Behavior<Command> onReadTemperature(ReadTemperature r) {
    r.replyTo.tell(new RespondTemperature(r.requestId, lastTemperatureReading));
    return this;
  }

  private Behavior<Command> onPostStop() {
    getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
    return Behaviors.stopped();
  }
}
```

我们现在也应该写一个新的测试用例，同时使用读/查询和写/记录功能:

```java
@Test
public void testReplyWithLatestTemperatureReading() {
  TestProbe<Device.TemperatureRecorded> recordProbe =
      testKit.createTestProbe(Device.TemperatureRecorded.class);
  TestProbe<Device.RespondTemperature> readProbe =
      testKit.createTestProbe(Device.RespondTemperature.class);
  ActorRef<Device.Command> deviceActor = testKit.spawn(Device.create("group", "device"));

  deviceActor.tell(new Device.RecordTemperature(1L, 24.0, recordProbe.getRef()));
  assertEquals(1L, recordProbe.receiveMessage().requestId);

  deviceActor.tell(new Device.ReadTemperature(2L, readProbe.getRef()));
  Device.RespondTemperature response1 = readProbe.receiveMessage();
  assertEquals(2L, response1.requestId);
  assertEquals(Optional.of(24.0), response1.value);

  deviceActor.tell(new Device.RecordTemperature(3L, 55.0, recordProbe.getRef()));
  assertEquals(3L, recordProbe.receiveMessage().requestId);

  deviceActor.tell(new Device.ReadTemperature(4L, readProbe.getRef()));
  Device.RespondTemperature response2 = readProbe.receiveMessage();
  assertEquals(4L, response2.requestId);
  assertEquals(Optional.of(55.0), response2.value);
}
```

测试情况如下

```log
[2020-08-03 02:09:13,147] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-8] [akka://DeviceTest/user/$a] - Device actor group-device started
[2020-08-03 02:09:13,156] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-8] [akka://DeviceTest/user/$a] - Recorded temperature reading 24.0 with 1
[2020-08-03 02:09:13,158] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-8] [akka://DeviceTest/user/$a] - Reading temperature read Optional[24.0] with 2
[2020-08-03 02:09:13,159] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-3] [akka://DeviceTest/user/$a] - Recorded temperature reading 55.0 with 3
[2020-08-03 02:09:13,160] [INFO] [com.tcfuture.akka.actor.example.iot.Device] [DeviceTest-akka.actor.default-dispatcher-8] [akka://DeviceTest/user/$a] - Reading temperature read Optional[55.0] with 4
```

#### 2.3.4.10. 下一步做啥？

到目前为止，我们已经开始设计我们的总体架构，并且我们编写了直接对应于领域的第一个Actor。现在我们必须创建负责维护设备组和设备Actor本身的组件。

### 2.3.5. 设备组Working

#### 2.3.5.1. 介绍

让我们仔细看看用例所需的主要功能。在用于监视家庭温度的完整IoT系统中，将设备传感器连接到我们的系统的步骤可能如下所示：

1. 家庭中的传感器设备通过某种协议进行连接。
2. 管理网络连接的组件接受该连接。
3. 传感器提供其组和设备ID，以向我们系统的设备管理器组件注册。
4. 设备管理器组件通过查找或创建负责保持传感器状态的Actor来处理注册。
5. Actor以一个确认回应，公开了它的ActorRef。
6. 现在，网络组件ActorRef无需通过设备管理器即可使用传感器和设备Actor之间的通信。

步骤1和2在我们的教程系统的范围之外进行。在本章中，我们将开始处理步骤3-6，并为传感器创建一种向我们的系统注册并与Actor进行通信的方式。但是首先，我们要做出另一个体系结构决策-我们应该使用多少级Actor来表示设备组和设备传感器？

Akka程序员面临的主要设计挑战之一是为角色选择最佳粒度。在实践中，根据Actor之间交互的特征，通常有几种有效的方法来组织系统。例如，在我们的用例中，有可能让一个Actor维护所有组和设备-也许使用哈希映射。每个组都有一个Actor来跟踪同一家庭中所有设备的状态也是合理的。

以下准则可帮助我们选择最合适的Actor层次结构：

* 通常，更喜欢较大的粒度。引入比需要的更多的细粒度actor会导致更多的问题而不是解决的问题。
* 当系统需要以下条件时，请添加更精细的粒度：
* 更高的并发性。
* 具有许多状态的Actor之间的复杂对话。在下一章中，我们将看到一个很好的例子。
* 有足够的状态可以划分成较小的Actor。
* 多重无关的责任。使用单独的角色可以使个人失败并得到恢复，而对他人的影响很小。

#### 2.3.5.2. 设备管理器层次结构

考虑到上一节中概述的原理，我们将设备管理器组件建模为具有三个级别的Actor树：

* 顶级主管Actor代表设备的系统组件。它也是查找和创建设备组和设备Actor的入口。

* 在下一个级别，组Actor分别监督设备Actor的一个组ID（例如，一个住所）。他们还提供服务，例如查询其组中所有可用设备的温度读数。

* 设备Actor负责管理与实际设备传感器的所有交互，例如存储温度读数。

![device_manager_tree](https://liulv.work/images/img/device_manager_tree.png)

出于以下原因，我们选择了这种三层体系结构：

* 由个别Actor组成的小组：

    * 隔离组中发生的故障。如果单个角色管理了所有设备组，则一个组中的错误导致重启，将清除其他情况下无故障的组的状态。
    * 简化了查询属于一个组的所有设备的问题。每个小组Actor仅包含与其小组相关的状态。
    * 增加系统中的并行性。由于每个组都有专门的Actor，因此它们可以同时运行，我们可以同时查询多个组。
* 将传感器建模为单独的设备Actor。
    * 将一个设备Actor的故障与组中的其他设备隔离。
    * 增加了收集温度读数的并行度。来自不同传感器的网络连接直接与其各自的设备Actor进行通信，从而减少了争用点。

使用定义的体系结构，我们可以开始研究用于注册传感器的协议。


#### 2.3.5.3. 注册协议

第一步，我们需要设计用于注册设备以及创建负责该设备的组和设备Actor的协议。该协议将由DeviceManager组件本身提供，因为这是唯一已知的且可预先获得的Actor：设备组和设备Actor是按需创建的。

详细查看注册，我们可以概述必要的功能：

1. 当DeviceManager收到具有组和设备ID的请求时：
>* 如果管理者已经有该设备组的Actor，则将请求转发给它。
>* 否则，它将创建一个新的设备组Actor，然后转发该请求。

2. DeviceGroupActor接收为给定设备注册Actor的请求:
>* 如果组中已经有该设备的actor，则使用ActorRef现有设备actor的答复。
>* 否则，DeviceGroupactor首先创建一个设备actor，并用ActorRef新创建的设备actor的答复。

3. 传感器现在将具有ActorRef设备角色的权限，可以直接向其发送消息。

我们将用于传达注册请求及其确认的消息具有以下定义：

```java
public class DeviceManager extends AbstractBehavior<DeviceManager.Command> {

  public interface Command {}

  /**
    * 请求跟踪监控设备
    */
  public static final class RequestTrackDevice
          implements DeviceManager.Command, DeviceGroup.Command {
      //设备组ID
      public final String groupId;
      //设备ID
      public final String deviceId;
      //注册ActorRef
      public final ActorRef<DeviceRegistered> replyTo;

      public RequestTrackDevice(String groupId, String deviceId, ActorRef<DeviceRegistered> replyTo) {
          this.groupId = groupId;
          this.deviceId = deviceId;
          this.replyTo = replyTo;
      }
  }

  /**
    * 已经注册的设备
    */
  public static final class DeviceRegistered {
      //设备ActorRef
      public final ActorRef<Device.Command> device;

      public DeviceRegistered(ActorRef<Device.Command> device) {
          this.device = device;
      }
  }
```

在这种情况下，我们在消息中没有包含请求ID字段。由于注册仅发生一次，因此当组件将系统连接到某个网络协议时，该ID并不重要。但是，通常最好的做法是包括请求ID。

现在，我们将从头开始实现协议。在实践中，自上而下和自下而上的方法都可以使用，但是在我们的案例中，我们受益于自下而上的方法，因为它使我们可以立即编写新功能的测试，而无需模拟出需要的部分。以后再建。

#### 2.3.5.4. 向设备组Actor添加注册支持

小组Actor在注册方面有一些工作要做，包括：

* 处理对现有设备Actor的注册请求或通过创建新Actor。
* 跟踪组中存在哪些设备Actor，并在他们停止时将其从组中删除。

实现如下：

```java
public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {

  public interface Command {}

  private class DeviceTerminated implements Command {
    public final ActorRef<Device.Command> device;
    public final String groupId;
    public final String deviceId;

    DeviceTerminated(ActorRef<Device.Command> device, String groupId, String deviceId) {
      this.device = device;
      this.groupId = groupId;
      this.deviceId = deviceId;
    }
  }

  public static Behavior<Command> create(String groupId) {
    return Behaviors.setup(context -> new DeviceGroup(context, groupId));
  }

  private final String groupId;
  private final Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();

  private DeviceGroup(ActorContext<Command> context, String groupId) {
    super(context);
    this.groupId = groupId;
    context.getLog().info("DeviceGroup {} started", groupId);
  }

  private DeviceGroup onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
    
    if (this.groupId.equals(trackMsg.groupId)) {
      ActorRef<Device.Command> deviceActor = deviceIdToActor.get(trackMsg.deviceId);
      if (deviceActor != null) {
        trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
      } else {
        getContext().getLog().info("Creating device actor for {}", trackMsg.deviceId);
        deviceActor =
            getContext()
                .spawn(Device.create(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId);
        deviceIdToActor.put(trackMsg.deviceId, deviceActor);
        trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
      }
    } else {
      getContext()
          .getLog()
          .warn(
              "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
              groupId,
              this.groupId);
    }
    return this;
  }


  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(DeviceManager.RequestTrackDevice.class, this::onTrackDevice)
        .build();
  }

  private DeviceGroup onPostStop() {
    getContext().getLog().info("DeviceGroup {} stopped", groupId);
    return this;
  }
}
```

就像我们对设备所做的一样，我们测试了这一新功能。我们还测试了返回的两个不同ID的Actor实际上是否不同，并且我们还尝试记录每个设备的温度读数，以查看Actor是否在响应。

```java
@Test
public void testReplyToRegistrationRequests() {
  TestProbe<DeviceRegistered> probe = testKit.createTestProbe(DeviceRegistered.class);
  ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

  groupActor.tell(new RequestTrackDevice("group", "device", probe.getRef()));
  DeviceRegistered registered1 = probe.receiveMessage();

  // another deviceId
  groupActor.tell(new RequestTrackDevice("group", "device3", probe.getRef()));
  DeviceRegistered registered2 = probe.receiveMessage();
  assertNotEquals(registered1.device, registered2.device);

  // Check that the device actors are working
  TestProbe<Device.TemperatureRecorded> recordProbe =
      testKit.createTestProbe(Device.TemperatureRecorded.class);
  registered1.device.tell(new Device.RecordTemperature(0L, 1.0, recordProbe.getRef()));
  assertEquals(0L, recordProbe.receiveMessage().requestId);
  registered2.device.tell(new Device.RecordTemperature(1L, 2.0, recordProbe.getRef()));
  assertEquals(1L, recordProbe.receiveMessage().requestId);
}

@Test
public void testIgnoreWrongRegistrationRequests() {
  TestProbe<DeviceRegistered> probe = testKit.createTestProbe(DeviceRegistered.class);
  ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));
  groupActor.tell(new RequestTrackDevice("wrongGroup", "device1", probe.getRef()));
  probe.expectNoMessage();
}
```

如果已经存在用于注册请求的设备Actor，我们想使用现有的Actor而不是新的Actor。我们尚未对此进行测试，因此我们需要解决此问题：

```java
@Test
public void testReturnSameActorForSameDeviceId() {
  TestProbe<DeviceRegistered> probe = testKit.createTestProbe(DeviceRegistered.class);
  ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

  groupActor.tell(new RequestTrackDevice("group", "device", probe.getRef()));
  DeviceRegistered registered1 = probe.receiveMessage();

  // registering same again should be idempotent
  groupActor.tell(new RequestTrackDevice("group", "device", probe.getRef()));
  DeviceRegistered registered2 = probe.receiveMessage();
  assertEquals(registered1.device, registered2.device);
}
```

#### 2.3.5.5. 跟踪组内的设备Actor

到目前为止，我们已经实现了在组中注册设备Actor的逻辑。但是，设备增增减减（come and go），因此我们需要一种方法来从Map<String, ActorRef>中删除设备Actor。我们假定当设备被删除时，其相应的设备Actor被停止。正如我们前面所讨论的，监督只能处理错误情况，而不能优雅的停止。因此，当其中一个设备Actor停止时，我们需要通知父 Actor。

Akka提供了一个“死亡观察”功能（Death Watch feature），该功能允许一个 Actor 观察另一个 Actor，并在另一个 Actir 停止时得到通知。与监督不同，观察（watching）并不局限于父子关系，任何 Actor 只要知道 ActorRef 就可以观看其他 Actor。在被观察的Actor 停止后，观察者会收到一条 Terminated(actorRef) 消息，该消息还包含对被观察的 Actor 的引用。观察者可以显式处理此消息，也可以失败并出现DeathPactException。如果在被观察的 Actor 被停止后，该 Actor 不能再履行自己的职责，则后者很有用。在我们的例子中，组应该在一个设备停止后继续工作，所以我们需要处理Terminated(actorRef)消息。

我们的设备组 Actor 需要包括以下功能：

* 当新设备 Actor 被创建时开始观察（watching）。
* 当通知指示设备已停止时，从映射Map<String, ActorRef>中删除设备 Actor。

不幸的是，Terminated的消息只包含子 Actor 的ActorRef。我们需要 Actor 的 ID 将其从现有设备到设备的 Actor 映射中删除。为了能够进行删除，我们需要引入另一个占位符Map<ActorRef, String>，它允许我们找到与给定ActorRef对应的设备 ID。

添加用于标识 Actor 的功能后，代码如下：

```java
public class DeviceGroup extends AbstractBehavior<DeviceGroup.Command> {

  public interface Command {}

  private class DeviceTerminated implements Command {
    public final ActorRef<Device.Command> device;
    public final String groupId;
    public final String deviceId;

    DeviceTerminated(ActorRef<Device.Command> device, String groupId, String deviceId) {
      this.device = device;
      this.groupId = groupId;
      this.deviceId = deviceId;
    }
  }

  public static Behavior<Command> create(String groupId) {
    return Behaviors.setup(context -> new DeviceGroup(context, groupId));
  }

  private final String groupId;
  private final Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();

  private DeviceGroup(ActorContext<Command> context, String groupId) {
    super(context);
    this.groupId = groupId;
    context.getLog().info("DeviceGroup {} started", groupId);
  }

  private DeviceGroup onTrackDevice(DeviceManager.RequestTrackDevice trackMsg) {
    if (this.groupId.equals(trackMsg.groupId)) {
      ActorRef<Device.Command> deviceActor = deviceIdToActor.get(trackMsg.deviceId);
      if (deviceActor != null) {
        trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
      } else {
        getContext().getLog().info("Creating device actor for {}", trackMsg.deviceId);
        deviceActor =
            getContext()
                .spawn(Device.create(groupId, trackMsg.deviceId), "device-" + trackMsg.deviceId);
        getContext()
            .watchWith(deviceActor, new DeviceTerminated(deviceActor, groupId, trackMsg.deviceId));
        deviceIdToActor.put(trackMsg.deviceId, deviceActor);
        trackMsg.replyTo.tell(new DeviceManager.DeviceRegistered(deviceActor));
      }
    } else {
      getContext()
          .getLog()
          .warn(
              "Ignoring TrackDevice request for {}. This actor is responsible for {}.",
              groupId,
              this.groupId);
    }
    return this;
  }
```

到目前为止，我们还没有办法获得组设备 Actor 跟踪的设备，因此，我们还不能测试我们的新功能。为了使其可测试，我们添加了一个新的查询功能（消息RequestDeviceList），其中列出了当前活动的设备 ID：

```java
public static final class RequestDeviceList
    implements DeviceManager.Command, DeviceGroup.Command {
  final long requestId;
  final String groupId;
  final ActorRef<ReplyDeviceList> replyTo;

  public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
    this.requestId = requestId;
    this.groupId = groupId;
    this.replyTo = replyTo;
  }
}

public static final class ReplyDeviceList {
  final long requestId;
  final Set<String> ids;

  public ReplyDeviceList(long requestId, Set<String> ids) {
    this.requestId = requestId;
    this.ids = ids;
  }
}
```

我们几乎准备好测试移除设备。但是，我们仍然需要以下能力:

* 要从我们的测试用例外部停止设备Actor，我们必须向它发送一条消息。我们添加了一个钝化消息，指示Actor停止。
* 一旦设备Actor停止，就会收到通知。我们也可以用死亡看护来做这个。

```java
static enum Passivate implements Command {
  INSTANCE
}
```

```java
import java.util.Optional;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class Device extends AbstractBehavior<Device.Command> {

  public interface Command {}

  public static final class RecordTemperature implements Command {
    final long requestId;
    final double value;
    final ActorRef<TemperatureRecorded> replyTo;

    public RecordTemperature(long requestId, double value, ActorRef<TemperatureRecorded> replyTo) {
      this.requestId = requestId;
      this.value = value;
      this.replyTo = replyTo;
    }
  }

  public static final class TemperatureRecorded {
    final long requestId;

    public TemperatureRecorded(long requestId) {
      this.requestId = requestId;
    }
  }

  public static final class ReadTemperature implements Command {
    final long requestId;
    final ActorRef<RespondTemperature> replyTo;

    public ReadTemperature(long requestId, ActorRef<RespondTemperature> replyTo) {
      this.requestId = requestId;
      this.replyTo = replyTo;
    }
  }

  public static final class RespondTemperature {
    final long requestId;
    final Optional<Double> value;

    public RespondTemperature(long requestId, Optional<Double> value) {
      this.requestId = requestId;
      this.value = value;
    }
  }

  static enum Passivate implements Command {
    INSTANCE
  }

  public static Behavior<Command> create(String groupId, String deviceId) {
    return Behaviors.setup(context -> new Device(context, groupId, deviceId));
  }

  private final String groupId;
  private final String deviceId;

  private Optional<Double> lastTemperatureReading = Optional.empty();

  private Device(ActorContext<Command> context, String groupId, String deviceId) {
    super(context);
    this.groupId = groupId;
    this.deviceId = deviceId;

    context.getLog().info("Device actor {}-{} started", groupId, deviceId);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(RecordTemperature.class, this::onRecordTemperature)
        .onMessage(ReadTemperature.class, this::onReadTemperature)
        .onMessage(Passivate.class, m -> Behaviors.stopped())
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onRecordTemperature(RecordTemperature r) {
    getContext().getLog().info("Recorded temperature reading {} with {}", r.value, r.requestId);
    lastTemperatureReading = Optional.of(r.value);
    r.replyTo.tell(new TemperatureRecorded(r.requestId));
    return this;
  }

  private Behavior<Command> onReadTemperature(ReadTemperature r) {
    r.replyTo.tell(new RespondTemperature(r.requestId, lastTemperatureReading));
    return this;
  }

  private Behavior<Command> onPostStop() {
    getContext().getLog().info("Device actor {}-{} stopped", groupId, deviceId);
    return Behaviors.stopped();
  }
}
```

我们现在再添加两个测试用例。在第一个测试中，我们测试在添加了一些设备之后，是否能返回正确的 ID 列表。第二个测试用例确保在设备 Actor 停止后正确删除设备 ID

```java
@Test
public void testListActiveDevices() {

  //创建 test probe 快捷方式用于testkit actor system, 应该接受的消息类型为DeviceRegistered
  TestProbe<DeviceRegistered> registeredProbe = testKit.createTestProbe(DeviceRegistered.class);
  //testKit
  ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

  groupActor.tell(new RequestTrackDevice("group", "device1", registeredProbe.getRef()));
  registeredProbe.receiveMessage();

  groupActor.tell(new RequestTrackDevice("group", "device2", registeredProbe.getRef()));
  registeredProbe.receiveMessage();

  TestProbe<ReplyDeviceList> deviceListProbe = testKit.createTestProbe(ReplyDeviceList.class);

  groupActor.tell(new RequestDeviceList(0L, "group", deviceListProbe.getRef()));
  ReplyDeviceList reply = deviceListProbe.receiveMessage();
  assertEquals(0L, reply.requestId);
  assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);
}

@Test
public void testListActiveDevicesAfterOneShutsDown() {
  TestProbe<DeviceRegistered> registeredProbe = testKit.createTestProbe(DeviceRegistered.class);
  ActorRef<DeviceGroup.Command> groupActor = testKit.spawn(DeviceGroup.create("group"));

  groupActor.tell(new RequestTrackDevice("group", "device1", registeredProbe.getRef()));
  DeviceRegistered registered1 = registeredProbe.receiveMessage();

  groupActor.tell(new RequestTrackDevice("group", "device2", registeredProbe.getRef()));
  DeviceRegistered registered2 = registeredProbe.receiveMessage();

  ActorRef<Device.Command> toShutDown = registered1.device;

  TestProbe<ReplyDeviceList> deviceListProbe = testKit.createTestProbe(ReplyDeviceList.class);

  groupActor.tell(new RequestDeviceList(0L, "group", deviceListProbe.getRef()));
  ReplyDeviceList reply = deviceListProbe.receiveMessage();
  assertEquals(0L, reply.requestId);
  assertEquals(Stream.of("device1", "device2").collect(Collectors.toSet()), reply.ids);

  toShutDown.tell(Device.Passivate.INSTANCE);
  registeredProbe.expectTerminated(toShutDown, registeredProbe.getRemainingOrDefault());

  // using awaitAssert to retry because it might take longer for the groupActor
  // to see the Terminated, that order is undefined
  registeredProbe.awaitAssert(
      () -> {
        groupActor.tell(new RequestDeviceList(1L, "group", deviceListProbe.getRef()));
        ReplyDeviceList r = deviceListProbe.receiveMessage();
        assertEquals(1L, r.requestId);
        assertEquals(Stream.of("device2").collect(Collectors.toSet()), r.ids);
        return null;
      });
}
```

### 2.3.6. 创建设备管理器 Actor

在我们的层次结构中，我们需要在DeviceManager源文件中为设备管理器组件创建入口点。此 Actor 与设备组 Actor 非常相似，但创建的是设备组 Actor 而不是设备 Actor：

```java
public class DeviceManager extends AbstractBehavior<DeviceManager.Command> {

  public interface Command {}

  public static final class RequestTrackDevice
      implements DeviceManager.Command, DeviceGroup.Command {
    public final String groupId;
    public final String deviceId;
    public final ActorRef<DeviceRegistered> replyTo;

    public RequestTrackDevice(String groupId, String deviceId, ActorRef<DeviceRegistered> replyTo) {
      this.groupId = groupId;
      this.deviceId = deviceId;
      this.replyTo = replyTo;
    }
  }

  public static final class DeviceRegistered {
    public final ActorRef<Device.Command> device;

    public DeviceRegistered(ActorRef<Device.Command> device) {
      this.device = device;
    }
  }

  public static final class RequestDeviceList
      implements DeviceManager.Command, DeviceGroup.Command {
    final long requestId;
    final String groupId;
    final ActorRef<ReplyDeviceList> replyTo;

    public RequestDeviceList(long requestId, String groupId, ActorRef<ReplyDeviceList> replyTo) {
      this.requestId = requestId;
      this.groupId = groupId;
      this.replyTo = replyTo;
    }
  }

  public static final class ReplyDeviceList {
    final long requestId;
    final Set<String> ids;

    public ReplyDeviceList(long requestId, Set<String> ids) {
      this.requestId = requestId;
      this.ids = ids;
    }
  }

  private static class DeviceGroupTerminated implements DeviceManager.Command {
    public final String groupId;

    DeviceGroupTerminated(String groupId) {
      this.groupId = groupId;
    }
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(DeviceManager::new);
  }

  private final Map<String, ActorRef<DeviceGroup.Command>> groupIdToActor = new HashMap<>();

  private DeviceManager(ActorContext<Command> context) {
    super(context);
    context.getLog().info("DeviceManager started");
  }

  private DeviceManager onTrackDevice(RequestTrackDevice trackMsg) {
    String groupId = trackMsg.groupId;
    ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(groupId);
    if (ref != null) {
      ref.tell(trackMsg);
    } else {
      getContext().getLog().info("Creating device group actor for {}", groupId);
      ActorRef<DeviceGroup.Command> groupActor =
          getContext().spawn(DeviceGroup.create(groupId), "group-" + groupId);
      getContext().watchWith(groupActor, new DeviceGroupTerminated(groupId));
      groupActor.tell(trackMsg);
      groupIdToActor.put(groupId, groupActor);
    }
    return this;
  }

  private DeviceManager onRequestDeviceList(RequestDeviceList request) {
    ActorRef<DeviceGroup.Command> ref = groupIdToActor.get(request.groupId);
    if (ref != null) {
      ref.tell(request);
    } else {
      request.replyTo.tell(new ReplyDeviceList(request.requestId, Collections.emptySet()));
    }
    return this;
  }

  private DeviceManager onTerminated(DeviceGroupTerminated t) {
    getContext().getLog().info("Device group actor for {} has been terminated", t.groupId);
    groupIdToActor.remove(t.groupId);
    return this;
  }

  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(RequestTrackDevice.class, this::onTrackDevice)
        .onMessage(RequestDeviceList.class, this::onRequestDeviceList)
        .onMessage(DeviceGroupTerminated.class, this::onTerminated)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private DeviceManager onPostStop() {
    getContext().getLog().info("DeviceManager stopped");
    return this;
  }
}
```

我们将设备管理器的测试留给你作为练习，因为它与我们为设备组 Actor 编写的测试非常相似。

#### 2.3.6.1. 下一步做什么

我们现在有了一个用于注册和跟踪设备以及记录测量值的分层组件。我们已经了解了如何实现不同类型的对话模式，例如：

* 请求响应（Request-respond），用于温度记录。
* 代理响应（Delegate-respond），用于设备注册。
* 创建监视终止（Create-watch-terminate），用于将组和设备 Actor 创建为子级。

在下一章中，我们将介绍组查询功能，这将建立一种新的分散收集（scatter-gather）对话模式。特别地，我们将实现允许用户查询属于一个组的所有设备的状态的功能。

### 2.3.7. 查询设备组

#### 2.3.7.1. 简介

到目前为止，我们所看到的对话模式很简单，因为它们要求 Actor 保持很少或根本就没有状态。明确地：

* 设备 Actor 返回一个不需要状态更改的读取
* 记录温度，更新单个字段
* 设备组 Actor 通过添加或删除映射中的条目来维护组成员身份

在本部分中，我们将使用一个更复杂的示例。由于房主会对整个家庭的温度感兴趣，我们的目标是能够查询一个组中的所有设备 Actor。让我们先研究一下这样的查询 API 应该如何工作。

#### 2.3.7.2. 处理可能的情况

我们面临的第一个问题是，一个组的成员是动态的。每个传感器设备都由一个可以随时停止的 Actor 表示。在查询开始时，我们可以询问所有现有设备 Actor 当前的温度。但是，在查询的生命周期中：

* 设备 Actor 可能会停止工作，无法用温度读数做出响应。
* 一个新的设备 Actor 可能会启动，并且不会包含在查询中，因为我们不知道它。

这些问题可以用许多不同的方式来解决，但重要的是要解决所期望的行为。以下工作对于我们的用例是很有用的：

* 当查询到达时，组 Actor 将获取现有设备 Actor 的快照（snapshot），并且只向这些 Actor 询问温度。
* 查询到达后启动的 Actor 可以被忽略。
* 如果快照中的某个 Actor 在查询期间停止而没有应答，我们将向查询消息的发送者报告它停止的事实。

除了设备 Actor 动态地变化之外，一些 Actor 可能需要很长时间来响应。例如，它们可能被困在一个意外的无限循环中，或者由于一个 bug 而失败，并放弃我们的请求。我们不希望查询无限期地继续，因此在以下任何一种情况下，我们都会认为它是完成的：

* 快照中的所有 Actor 要么已响应，要么确认已停止。
* 我们达到了预定的（pre-defined）最后期限。

考虑到这些决定，再加上快照中的设备可能刚刚启动但尚未接收到要记录的温度，我们可以针对温度查询为每个设备 Actor 定义四种状态：

* 它有一个可用的温度：Temperature。
* 它已经响应，但还没有可用的温度：TemperatureNotAvailable。
* 它在响应之前已停止：DeviceNotAvailable。
* 它在最后期限之前没有响应：DeviceTimedOut。

在消息类型中汇总这些信息，我们可以将以下代码添加到DeviceGroup：

```java
public static final class RequestAllTemperatures
    implements DeviceGroupQuery.Command, DeviceGroup.Command, Command {

  final long requestId;
  final String groupId;
  final ActorRef<RespondAllTemperatures> replyTo;

  public RequestAllTemperatures(
      long requestId, String groupId, ActorRef<RespondAllTemperatures> replyTo) {
    this.requestId = requestId;
    this.groupId = groupId;
    this.replyTo = replyTo;
  }
}

public static final class RespondAllTemperatures {
  final long requestId;
  final Map<String, TemperatureReading> temperatures;

  public RespondAllTemperatures(long requestId, Map<String, TemperatureReading> temperatures) {
    this.requestId = requestId;
    this.temperatures = temperatures;
  }
}

public interface TemperatureReading {}

public static final class Temperature implements TemperatureReading {
  public final double value;

  public Temperature(double value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Temperature that = (Temperature) o;

    return Double.compare(that.value, value) == 0;
  }

  @Override
  public int hashCode() {
    long temp = Double.doubleToLongBits(value);
    return (int) (temp ^ (temp >>> 32));
  }

  @Override
  public String toString() {
    return "Temperature{" + "value=" + value + '}';
  }
}

public enum TemperatureNotAvailable implements TemperatureReading {
  INSTANCE
}

public enum DeviceNotAvailable implements TemperatureReading {
  INSTANCE
}

public enum DeviceTimedOut implements TemperatureReading {
  INSTANCE
}
```

#### 2.3.7.3. 实现查询功能

实现查询的一种方法是向组设备 Actor 添加代码。然而，在实践中，这可能非常麻烦并且容易出错。请记住，当我们启动查询时，我们需要获取当前设备的快照并启动计时器，以便强制执行截止时间。同时，另一个查询可以到达。对于第二个查询，我们需要跟踪完全相同的信息，但与前一个查询隔离。这将要求我们在查询和设备 Actor 之间维护单独的映射。

相反，我们将实现一种更简单、更优雅的方法。我们将创建一个表示单个查询的 Actor，并代表组 Actor 执行完成查询所需的任务。到目前为止，我们已经创建了属于典型域对象（classical domain）的 Actor，但是现在，我们将创建一个表示流程或任务而不是实体的 Actor。我们通过保持我们的组设备 Actor 简单和能够更好地隔离测试查询功能而受益。

##### 2.3.7.3.1. 定义查询 Actor

首先，我们需要设计查询 Actor 的生命周期。这包括识别其初始状态、将要采取的第一个操作以及清除（如果需要）。查询 Actor 需要以下信息：

* 要查询的活动设备 Actor 的快照和 ID。
* 启动查询的请求的 ID（以便我们可以在响应中包含它）。
* 发送查询的 Actor 的引用。我们会直接给这个 Actor 响应。
* 指示查询等待响应的期限。将其作为参数将简化测试。

##### 2.3.7.3.2. 设置查询超时

由于我们需要一种方法来指示我们愿意等待响应的时间，现在是时候引入一个我们还没有使用的新的 Akka 特性，即内置的调度器（built-in scheduler）功能了。使用调度器（scheduler）很简单：

* 我们可以从ActorSystem中获取调度器，而ActorSystem又可以从 Actor 的上下文中访问：getContext().getSystem().scheduler()。这需要一个ExecutionContext，它是将执行计时器任务本身的线程池。在我们的示例中，我们通过传入getContext().dispatcher()来使用与 Actor 相同的调度器。
* scheduler.scheduleOnce(time, actorRef, message, executor, sender)方法将在指定的time将消息message调度到Future，并将其发送给 Actor 的ActorRef。

我们需要创建一个表示查询超时的消息。为此，我们创建了一个没有任何参数的简单消息CollectionTimeout。scheduleOnce的返回值是Cancellable，如果查询及时成功完成，可以使用它取消定时器。在查询开始时，我们需要询问每个设备 Actor 当前的温度。为了能够快速检测那些在ReadTemperature信息之前停止的设备，我们还将观察每个 Actor。这样，对于那些在查询生命周期中停止的消息，我们就可以得到Terminated消息，因此我们不需要等到超时时再将这些消息标记为不可用。

综上所述，DeviceGroupQuery Actor 的代码大致如下：

```java
public class DeviceGroupQuery extends AbstractActor {
  public static final class CollectionTimeout {
  }

  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  final Map<ActorRef, String> actorToDeviceId;
  final long requestId;
  final ActorRef requester;

  Cancellable queryTimeoutTimer;

  public DeviceGroupQuery(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout) {
    this.actorToDeviceId = actorToDeviceId;
    this.requestId = requestId;
    this.requester = requester;

    queryTimeoutTimer = getContext().getSystem().scheduler().scheduleOnce(
            timeout, getSelf(), new CollectionTimeout(), getContext().dispatcher(), getSelf()
    );
  }

  public static Props props(Map<ActorRef, String> actorToDeviceId, long requestId, ActorRef requester, FiniteDuration timeout) {
    return Props.create(DeviceGroupQuery.class, () -> new DeviceGroupQuery(actorToDeviceId, requestId, requester, timeout));
  }

  @Override
  public void preStart() {
    for (ActorRef deviceActor : actorToDeviceId.keySet()) {
      getContext().watch(deviceActor);
      deviceActor.tell(new Device.ReadTemperature(0L), getSelf());
    }
  }

  @Override
  public void postStop() {
    queryTimeoutTimer.cancel();
  }
}
```

##### 2.3.7.3.3. 跟踪 Actor 状态 

除了挂起的计时器之外，查询actor还有一个有状态的方面，即跟踪已响应、已停止或未响应的actor集。我们在actor中的可变HashMap中跟踪此状态。

对于我们的用例:

1. 我们跟踪状态
> * 用一个Map收集已经收到回复的Actor
> * 用一个Set收集我们仍在等待的Actor

2. 我们有三件事要做
> * 我们可以从其中一个设备接收到 RespondTemperature 信息。
> * 我们可以收到一个DeviceTerminated消息的设备Actor已经停止在此期间。
> * 我们可以在截止日期前收到 CollectionTimeout。

要做到这一点，添加以下到您的DeviceGroupQuery源文件:

```java
private Map<String, DeviceManager.TemperatureReading> repliesSoFar = new HashMap<>();
private final Set<String> stillWaiting;

@Override
public Receive<Command> createReceive() {
  return newReceiveBuilder()
      .onMessage(WrappedRespondTemperature.class, this::onRespondTemperature)
      .onMessage(DeviceTerminated.class, this::onDeviceTerminated)
      .onMessage(CollectionTimeout.class, this::onCollectionTimeout)
      .build();
}

private Behavior<Command> onRespondTemperature(WrappedRespondTemperature r) {
  DeviceManager.TemperatureReading reading =
      r.response
          .value
          .map(v -> (DeviceManager.TemperatureReading) new DeviceManager.Temperature(v))
          .orElse(DeviceManager.TemperatureNotAvailable.INSTANCE);

  String deviceId = r.response.deviceId;
  repliesSoFar.put(deviceId, reading);
  stillWaiting.remove(deviceId);

  return respondWhenAllCollected();
}

private Behavior<Command> onDeviceTerminated(DeviceTerminated terminated) {
  if (stillWaiting.contains(terminated.deviceId)) {
    repliesSoFar.put(terminated.deviceId, DeviceManager.DeviceNotAvailable.INSTANCE);
    stillWaiting.remove(terminated.deviceId);
  }
  return respondWhenAllCollected();
}

private Behavior<Command> onCollectionTimeout(CollectionTimeout timeout) {
  for (String deviceId : stillWaiting) {
    repliesSoFar.put(deviceId, DeviceManager.DeviceTimedOut.INSTANCE);
  }
  stillWaiting.clear();
  return respondWhenAllCollected();
}
```

对于RespondTemperature和DeviceTerminated，我们通过更新repliesso来跟踪回复，并从stillWaiting中删除actor。为此，我们可以使用已经出现在DeviceTerminated消息中的actor标识符。对于我们的RespondTemperature信息，我们需要添加如下信息:

```java
public static final class RespondTemperature {
  final long requestId;
  final String deviceId;
  final Optional<Double> value;

  public RespondTemperature(long requestId, String deviceId, Optional<Double> value) {
    this.requestId = requestId;
    this.deviceId = deviceId;
    this.value = value;
  }
}
```

```java
private Behavior<Command> onReadTemperature(ReadTemperature r) {
  r.replyTo.tell(new RespondTemperature(r.requestId, deviceId, lastTemperatureReading));
  return this;
}
```

在处理完每条消息后，我们将委托给respondWhenAllCollected方法，我们将很快讨论这个方法。

在超时的情况下，我们需要获取所有还没有回复的Actor(集合中的成员仍然在等待)，并在最终的回复中放置一个DeviceTimedOut作为状态。

现在我们必须弄清楚在respondWhenAllCollected中要做什么。首先，我们需要在repliesSoFar的map中记录新结果，并将actor从静止等待中删除。下一步是检查是否还有我们正在等待的Actor。如果没有，我们将查询结果发送给原始请求者，并停止查询actor。否则，我们需要更新repliesSoFar和stillWaiting结构并等待更多消息。

有了这些知识，我们可以创建respondWhenAllCollected方法:

```java
private Behavior<Command> respondWhenAllCollected() {
  if (stillWaiting.isEmpty()) {
    requester.tell(new DeviceManager.RespondAllTemperatures(requestId, repliesSoFar));
    return Behaviors.stopped();
  } else {
    return this;
  }
}
```

我们的查询actor现在已经完成:

```java
public class DeviceGroupQuery extends AbstractBehavior<DeviceGroupQuery.Command> {

  public interface Command {}

  private static enum CollectionTimeout implements Command {
    INSTANCE
  }

  static class WrappedRespondTemperature implements Command {
    final Device.RespondTemperature response;

    WrappedRespondTemperature(Device.RespondTemperature response) {
      this.response = response;
    }
  }

  private static class DeviceTerminated implements Command {
    final String deviceId;

    private DeviceTerminated(String deviceId) {
      this.deviceId = deviceId;
    }
  }

  public static Behavior<Command> create(
      Map<String, ActorRef<Device.Command>> deviceIdToActor,
      long requestId,
      ActorRef<DeviceManager.RespondAllTemperatures> requester,
      Duration timeout) {
    return Behaviors.setup(
        context ->
            Behaviors.withTimers(
                timers ->
                    new DeviceGroupQuery(
                        deviceIdToActor, requestId, requester, timeout, context, timers)));
  }

  private final long requestId;
  private final ActorRef<DeviceManager.RespondAllTemperatures> requester;
  private Map<String, DeviceManager.TemperatureReading> repliesSoFar = new HashMap<>();
  private final Set<String> stillWaiting;


  private DeviceGroupQuery(
      Map<String, ActorRef<Device.Command>> deviceIdToActor,
      long requestId,
      ActorRef<DeviceManager.RespondAllTemperatures> requester,
      Duration timeout,
      ActorContext<Command> context,
      TimerScheduler<Command> timers) {
    super(context);
    this.requestId = requestId;
    this.requester = requester;

    timers.startSingleTimer(CollectionTimeout.INSTANCE, timeout);

    ActorRef<Device.RespondTemperature> respondTemperatureAdapter =
        context.messageAdapter(Device.RespondTemperature.class, WrappedRespondTemperature::new);

    for (Map.Entry<String, ActorRef<Device.Command>> entry : deviceIdToActor.entrySet()) {
      context.watchWith(entry.getValue(), new DeviceTerminated(entry.getKey()));
      entry.getValue().tell(new Device.ReadTemperature(0L, respondTemperatureAdapter));
    }
    stillWaiting = new HashSet<>(deviceIdToActor.keySet());
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(WrappedRespondTemperature.class, this::onRespondTemperature)
        .onMessage(DeviceTerminated.class, this::onDeviceTerminated)
        .onMessage(CollectionTimeout.class, this::onCollectionTimeout)
        .build();
  }

  private Behavior<Command> onRespondTemperature(WrappedRespondTemperature r) {
    DeviceManager.TemperatureReading reading =
        r.response
            .value
            .map(v -> (DeviceManager.TemperatureReading) new DeviceManager.Temperature(v))
            .orElse(DeviceManager.TemperatureNotAvailable.INSTANCE);

    String deviceId = r.response.deviceId;
    repliesSoFar.put(deviceId, reading);
    stillWaiting.remove(deviceId);

    return respondWhenAllCollected();
  }

  private Behavior<Command> onDeviceTerminated(DeviceTerminated terminated) {
    if (stillWaiting.contains(terminated.deviceId)) {
      repliesSoFar.put(terminated.deviceId, DeviceManager.DeviceNotAvailable.INSTANCE);
      stillWaiting.remove(terminated.deviceId);
    }
    return respondWhenAllCollected();
  }

  private Behavior<Command> onCollectionTimeout(CollectionTimeout timeout) {
    for (String deviceId : stillWaiting) {
      repliesSoFar.put(deviceId, DeviceManager.DeviceTimedOut.INSTANCE);
    }
    stillWaiting.clear();
    return respondWhenAllCollected();
  }

  private Behavior<Command> respondWhenAllCollected() {
    if (stillWaiting.isEmpty()) {
      requester.tell(new DeviceManager.RespondAllTemperatures(requestId, repliesSoFar));
      return Behaviors.stopped();
    } else {
      return this;
    }
  }

}
```

##### 2.3.7.3.4. 测试查询 Actor

现在，让我们验证查询 Actor 实现的正确性。我们需要单独测试各种场景，以确保一切都按预期工作。为了能够做到这一点，我们需要以某种方式模拟设备 Actor 来运行各种正常或故障场景。幸运的是，我们将合作者（collaborators）列表（实际上是一个Map）作为查询 Actor 的参数，这样我们就可以传入TestKit引用。在我们的第一个测试中，我们在有两个设备的情况下进行测试，两个设备都报告了温度：

```java
@Test
public void testReturnTemperatureValueForWorkingDevices() {
  TestProbe<RespondAllTemperatures> requester =
      testKit.createTestProbe(RespondAllTemperatures.class);
  TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
  TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

  Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
  deviceIdToActor.put("device1", device1.getRef());
  deviceIdToActor.put("device2", device2.getRef());

  ActorRef<DeviceGroupQuery.Command> queryActor =
      testKit.spawn(
          DeviceGroupQuery.create(
              deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

  device1.expectMessageClass(Device.ReadTemperature.class);
  device2.expectMessageClass(Device.ReadTemperature.class);

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

  RespondAllTemperatures response = requester.receiveMessage();
  assertEquals(1L, response.requestId);

  Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
  expectedTemperatures.put("device1", new Temperature(1.0));
  expectedTemperatures.put("device2", new Temperature(2.0));

  assertEquals(expectedTemperatures, response.temperatures);
}
```

这是令人高兴的情况，但我们知道，有时设备不能提供温度测量。这个场景与前一个稍有不同:

```java
@Test
public void testReturnTemperatureNotAvailableForDevicesWithNoReadings() {
  TestProbe<RespondAllTemperatures> requester =
      testKit.createTestProbe(RespondAllTemperatures.class);
  TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
  TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

  Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
  deviceIdToActor.put("device1", device1.getRef());
  deviceIdToActor.put("device2", device2.getRef());

  ActorRef<DeviceGroupQuery.Command> queryActor =
      testKit.spawn(
          DeviceGroupQuery.create(
              deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

  assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
  assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device1", Optional.empty())));

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

  RespondAllTemperatures response = requester.receiveMessage();
  assertEquals(1L, response.requestId);

  Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
  expectedTemperatures.put("device1", TemperatureNotAvailable.INSTANCE);
  expectedTemperatures.put("device2", new Temperature(2.0));

  assertEquals(expectedTemperatures, response.temperatures);
}
```

我们也知道，有时候设备操作者会在回答之前停下来:

```java
@Test
public void testReturnDeviceNotAvailableIfDeviceStopsBeforeAnswering() {
  TestProbe<RespondAllTemperatures> requester =
      testKit.createTestProbe(RespondAllTemperatures.class);
  TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
  TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

  Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
  deviceIdToActor.put("device1", device1.getRef());
  deviceIdToActor.put("device2", device2.getRef());

  ActorRef<DeviceGroupQuery.Command> queryActor =
      testKit.spawn(
          DeviceGroupQuery.create(
              deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

  assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
  assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

  device2.stop();

  RespondAllTemperatures response = requester.receiveMessage();
  assertEquals(1L, response.requestId);

  Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
  expectedTemperatures.put("device1", new Temperature(1.0));
  expectedTemperatures.put("device2", DeviceNotAvailable.INSTANCE);

  assertEquals(expectedTemperatures, response.temperatures);
}
```

如果你还记得，还有另一种情况与设备Actor停止有关。有可能我们从一个设备Actor那里得到一个正常的回复，但是之后会收到一个终止的相同Actor的回复。在这种情况下，我们希望保留第一个回复，不将该设备标记为DeviceNotAvailable。我们也应该测试一下:

```java
@Test
public void testReturnTemperatureReadingEvenIfDeviceStopsAfterAnswering() {
  TestProbe<RespondAllTemperatures> requester =
      testKit.createTestProbe(RespondAllTemperatures.class);
  TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
  TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

  Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
  deviceIdToActor.put("device1", device1.getRef());
  deviceIdToActor.put("device2", device2.getRef());

  ActorRef<DeviceGroupQuery.Command> queryActor =
      testKit.spawn(
          DeviceGroupQuery.create(
              deviceIdToActor, 1L, requester.getRef(), Duration.ofSeconds(3)));

  assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
  assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device2", Optional.of(2.0))));

  device2.stop();

  RespondAllTemperatures response = requester.receiveMessage();
  assertEquals(1L, response.requestId);

  Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
  expectedTemperatures.put("device1", new Temperature(1.0));
  expectedTemperatures.put("device2", new Temperature(2.0));

  assertEquals(expectedTemperatures, response.temperatures);
}
```

最后一种情况是，不是所有设备都及时响应。为了保持我们的测试相对快速，我们将用更小的超时构造DeviceGroupQuery actor:

```java
@Test
public void testReturnDeviceTimedOutIfDeviceDoesNotAnswerInTime() {
  TestProbe<RespondAllTemperatures> requester =
      testKit.createTestProbe(RespondAllTemperatures.class);
  TestProbe<Device.Command> device1 = testKit.createTestProbe(Device.Command.class);
  TestProbe<Device.Command> device2 = testKit.createTestProbe(Device.Command.class);

  Map<String, ActorRef<Device.Command>> deviceIdToActor = new HashMap<>();
  deviceIdToActor.put("device1", device1.getRef());
  deviceIdToActor.put("device2", device2.getRef());

  ActorRef<DeviceGroupQuery.Command> queryActor =
      testKit.spawn(
          DeviceGroupQuery.create(
              deviceIdToActor, 1L, requester.getRef(), Duration.ofMillis(200)));

  assertEquals(0L, device1.expectMessageClass(Device.ReadTemperature.class).requestId);
  assertEquals(0L, device2.expectMessageClass(Device.ReadTemperature.class).requestId);

  queryActor.tell(
      new DeviceGroupQuery.WrappedRespondTemperature(
          new Device.RespondTemperature(0L, "device1", Optional.of(1.0))));

  // no reply from device2

  RespondAllTemperatures response = requester.receiveMessage();
  assertEquals(1L, response.requestId);

  Map<String, TemperatureReading> expectedTemperatures = new HashMap<>();
  expectedTemperatures.put("device1", new Temperature(1.0));
  expectedTemperatures.put("device2", DeviceTimedOut.INSTANCE);

  assertEquals(expectedTemperatures, response.temperatures);
}
```

我们的查询现在按预期工作，现在应该在DeviceGroup actor中包括这个新功能。

#### 2.3.7.4. 向组添加查询功能

现在在组Actor中包含查询特性相当简单。我们在查询actor本身中完成了所有繁重的工作，组actor只需要使用正确的初始参数创建它，而不需要其他东西。

```java
private DeviceGroup onAllTemperatures(DeviceManager.RequestAllTemperatures r) {
    // since Java collections are mutable, we want to avoid sharing them between actors (since
    // multiple Actors (threads)
    // modifying the same mutable data-structure is not safe), and perform a defensive copy of the
    // mutable map:
    //
    // Feel free to use your favourite immutable data-structures library with Akka in Java
    // applications!
    Map<String, ActorRef<Device.Command>> deviceIdToActorCopy = new HashMap<>(this.deviceIdToActor);

    getContext()
        .spawnAnonymous(
            DeviceGroupQuery.create(
                deviceIdToActorCopy, r.requestId, r.replyTo, Duration.ofSeconds(3)));

    return this;
  }


  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        // ... other cases omitted
        .onMessage(
            DeviceManager.RequestAllTemperatures.class,
            r -> r.groupId.equals(groupId),
            this::onAllTemperatures)
        .build();
  }
```


#### 2.3.7.5. 总结

在物联网（`IoT`）系统的背景下，本指南介绍了以下概念。如有必要，你可以通过以下链接进行查看：

- [Actor 的层级结构及其生命周期](https://github.com/guobinhit/akka-guide/blob/master/articles/getting-started-guide/tutorial_1.md)
- [灵活性设计消息的重要性](https://github.com/guobinhit/akka-guide/blob/master/articles/getting-started-guide/tutorial_3.md)
- [如何监视和停止 Actor](https://github.com/guobinhit/akka-guide/blob/master/articles/getting-started-guide/tutorial_4.md)

### 2.3.8. 下一步是什么？
要继续你的 Akka 之旅，我们建议：

- 开始用 Akka 构建你自己的应用程序，如果你陷入困境的话，希望你能参与到我们的「[社区](https://akka.io/get-involved/)」中，寻求帮助。
- 如果你想了解更多的背景知识，请阅读参考文件的其余部分，并查看一些关于 Akka 的「[书籍和视频](https://doc.akka.io/docs/akka/current/additional/books.html)」。

要从本指南获得完整的应用程序，你可能需要提供 UI 或 API。为此，我们建议你查看以下技术，看看哪些适合你：

- 「[Akka HTTP](https://doc.akka.io/docs/akka/current/additional/books.html)」是一个 HTTP 服务和客户端的库，使发布和使用 HTTP 端点（`endpoints`）成为可能。
- 「[Play Framework](https://doc.akka.io/docs/akka/current/additional/books.html)」是一个成熟的 Web 框架，它构建在 Akka HTTP 之上，它能与 Akka 很好地集成，可用于创建一个完整的现代 Web 用户界面。
- 「[Lagom](https://doc.akka.io/docs/akka/current/additional/books.html)」是一个基于 Akka 的独立的微服务框架，它编码了 Akka 和 Play 的许多最佳实践。

----------

**英文原文链接**：[Part 5: Querying Device Groups](https://doc.akka.io/docs/akka/current/guide/tutorial_5.html).


# 3. 通用概念

## 3.1. 术语及概念

在本章中，我们试图建立一个通用的术语来定义一个坚实的基础，用于交流 Akka 所针对的并发和分布式系统。请注意，对于这其中的许多术语，并没有一个统一的定义。我们试图给出将在 Akka 文档范围内使用的定义。

### 3.1.1. 并发 vs. 并行
并发和并行是相关的概念，但有一些小的区别。并发意味着两个或多个任务正在取得进展，即使它们可能不会同时执行。例如，这可以通过时间切片来实现，其中部分任务按顺序执行，并与其他任务的部分混合。另一方面，当执行的任务可以真正同时进行时，就会出现并行。

### 3.1.2. 异步 vs. 同步
如果调用者在方法返回值或引发异常之前无法取得进展，则认为方法调用是同步的。另一方面，异步调用允许调用者在有限的步骤之后继续进行，并且可以通过一些附加机制（它可能是已注册的回调、Future或消息）来通知方法的完成。

同步 API 可以使用阻塞来实现同步，但这不是必要的。CPU 密集型任务可能会产生类似于阻塞的行为。一般来说，最好使用异步 API，因为它们保证系统能够进行。Actor 本质上是异步的：Actor 可以在消息发送之后进行其他任务，而不必等待实际的传递发生。

### 3.1.3. 非阻塞 vs. 阻塞
如果一个线程的延迟可以无限期地延迟其他一些线程，这就是我们讨论的阻塞。一个很好的例子是，一个线程可以使用互斥来独占使用一个资源。如果一个线程无限期地占用资源（例如意外运行无限循环），则等待该资源的其他线程将无法进行。相反，非阻塞意味着没有线程能够无限期地延迟其他线程。

非阻塞操作优先于阻塞操作，因为当系统包含阻塞操作时，系统的总体进度并不能得到很好的保证。

### 3.1.4. 死锁 vs. 饥饿 vs. 活锁
当多个 Actor 在等待对方达到某个特定的状态以便能够取得进展时，就会出现死锁（Deadlock）。由于没有其他 Actor 达到某种状态（一个Catch-22问题），所有受影响的子系统都无法继续运行。死锁与阻塞密切相关，因为 Actor 线程能够无限期地延迟其他线程的进程。

在死锁的情况下，没有 Actor 可以取得进展，相反，当有 Actor 可以取得进展，但可能有一个或多个 Actor 不能取得进展时，就会发生饥饿（Starvation）。典型的场景是一个调度算法，它总是选择高优先级的任务而不是低优先级的任务。如果传入的高优先级任务的数量一直足够多，那么低优先级任务将永远不会完成。

活锁（Livelock）类似于死锁，因为没有 Actor 取得进展。不同之处在于，Actor 不会被冻结在等待他人进展的状态中，而是不断地改变自己的状态。一个示例场景是，两个 Actor 有两个相同资源可用时。他们每一个都试图获得资源，但他们也会检查对方是否也需要资源。如果资源是由另一个 Actor 请求的，他们会尝试获取该资源的另一个实例。在不幸的情况下，两个 Actor 可能会在两种资源之间“反弹（bounce）”，从不获取资源，但总是屈服于另一种资源。

### 3.1.5. 竟态条件
当一组事件的顺序的假设可能被外部的非确定性（non-deterministic）因素影响时，我们称之为竟态条件（Race condition）。当多个线程具有共享可变状态时，常常会出现竟态条件，并且线程在该状态上的操作可能会交错进行，从而导致意外的行为。虽然这是一个常见的情况，但是共享状态不需要有竟态条件。例如，客户机向服务器发送无序数据包（如 UDP 数据报）P1和P2。由于数据包可能通过不同的网络路由传输，因此服务器可能先接收到P2，然后接收到P1。如果消息不包含有关其发送顺序的信息，则服务器无法确定它们是以不同的顺序发送的。根据包（packets）的含义，这可能导致竟态条件。

注释：Akka 提供的关于在给定的两个 Actor 之间发送的消息的唯一保证是，他们的顺序始终保持不变。详见「消息传递可靠性」。

### 3.1.6. 非阻塞保证（进度条件）
如前几节所讨论的，阻塞是不可取的，原因有几个，包括死锁的危险和系统中吞吐量的降低。在下面的章节中，我们将讨论具有不同强度的各种非阻塞特性。

### 3.1.7. 等待自由
如果保证每个调用都以有限的步骤完成，则方法是无等待的，即等待自由（wait-freedom）。如果一个方法是有界“无等待”的，那么步骤的数量有一个有限的上限。

根据这个定义，无等待方法永远不会被阻塞，因此不会发生死锁。此外，由于每个 Actor 都可以在有限的步骤之后（调用完成时）继续进行，因此无等待方法没有饥饿。

### 3.1.8. 锁自由
锁自由（lock-freedom）比等待自由更弱。在无锁调用的情况下，某些方法以有限的步数完成可能导致无限的等待。这个定义意味着没有死锁的调用是不可能的。另一方面，某些调用以有限的步骤完成的保证不足以保证所有调用最终都完成。换句话说，锁自由不足以保证不发生饥饿。

### 3.1.9. 障碍自由
障碍自由（obstruction-freedom）是本文讨论的最薄弱的非阻塞保证。如果一个方法在某个时间点之后独立执行（其他线程不执行任何步骤，例如：挂起），则该方法称为无障碍的，它以有限的步骤完成。所有锁自由对象都是无障碍的，但相反的情况通常是不正确的。

乐观并发控制（OCC）方法通常是无障碍的。OCC 方法是，每个 Actor 都试图在共享对象上执行其操作，但如果 Actor 检测到来自其他对象的冲突，则会回滚修改，并根据某些计划重试。如果有一个时间点，其中一个 Actor 是唯一的尝试者，那么操作将成功

## 3.2. Actor系统
Actor 是封装状态和行为的对象，它们通过交换放在收件人邮箱中的消息进行专门的通信。从某种意义上说，Actor 是面向对象编程最严格的形式，但最好将其视为“人”：当与 Actor 一起建模解决方案时，设想一组人员并为其分配子任务，将其功能安排到组织结构中，并考虑如何升级失败（所有这些都得益于非实际地与人打交道，这意味着我们不需要关心他们的情感状态或道德问题）。结果可以作为构建软件实现的一个心理脚手架（`mental scaffolding`）。

- **注释**：`ActorSystem`是一个重量级架构，它将分配`1 … N`个线程，因此为每个逻辑应用程序都创建一个线程。

### 3.2.1. 层次结构
就像在经济组织中一样，Actor 自然形成等级制度。一个负责监督程序中某个函数的 Actor 可能希望将其任务拆分为更小、更易于管理的部分。为此，它启动了由它监督的子 Actor。在「[这里](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/supervision.md)」解释监督细节的同时，我们将集中讨论本节中的基本概念。唯一的先决条件是要知道每个 Actor 只有一个监督者，这就是创建它的 Actor。

Actor 系统的典型特征是，任务被拆分和委托，直到它们变得足够小，可以一块处理。这样做，不仅任务本身结构清晰，而且结果 Actor 可以根据他们应该处理哪些消息、应该如何正常反应以及应该如何处理失败来进行推理。如果一个 Actor 没有处理特定情况的方法，它会向其监督 Actor 发送相应的失败消息，请求帮助。然后，递归结构允许在正确的级别处理故障。

将其与易于转入防御编程（`defensive programming`）的分层软件设计进行比较，目的是不泄漏任何故障：如果问题传达给了正确的人，那么可以找到比试图将所有事情“隐藏”在“地毯下”更好的解决方案。

现在，设计这样一个系统的困难在于如何决定谁应该监督什么。没有单一的最佳解决方案，但有一些指导方针可能会有所帮助：

- 如果一个 Actor 管理另一个 Actor 正在做的工作，例如通过传递子任务，那么管理 Actor 应该监督子 Actor。原因是管理 Actor 知道预期的故障类型以及如何处理。
- 如果一个 Actor 携带非常重要的数据（即，如果可以避免，其状态不会丢失），则该 Actor 应向其监督的子 Actor 找出任何可能危险的子任务，并处理这些子 Actor 的故障。根据请求的性质，最好为每个请求创建一个新的子级，这样可以简化收集答复的状态管理。这就是 Erlang 的“错误内核模式”。
- 如果一个 Actor 依靠另一个 Actor 来履行职责，它应该观察另一个 Actor 的活动，并在接到终止通知后采取行动。这与监督不同，因为监督方对监督策略没有影响，应该注意的是，单独的功能依赖性并不是决定将某个子 Actor 放在层级中何处的标准。

这些规则总是有例外的，但是不管你是遵守规则还是违反规则，你都应该有一个理由。

### 3.2.2. 配置容器

Actor 系统作为 Actor 的协作集合，是管理共享设施（如调度服务、配置、日志记录等）的自然单元。具有不同配置的多个 Actor 系统可以在同一个 JVM 中共存，没有问题，Akka 本身没有全局共享状态。将其与一个节点内或通过网络连接的 Actor 系统之间的透明通信结合起来，可以看到 Actor 系统本身可以用作功能层次结构中的构建块。

### 3.2.3. Actor 最佳实践

 1. Actor 应该像好的同事一样：高效地工作，而不是不必要地打扰其他人，并且避免占用资源。翻译成编程语言，这意味着以事件驱动的方式处理事件并生成响应（或更多请求）。Actor 不应在可能是锁、网络套接字等外部实体上阻塞，即占用线程时被动等待，除非这是不可避免的；对于后一种情况，请参见下文。
 2. 不要在 Actor 之间传递可变对象。为了确保这一点，最好选择不可变的消息。如果通过将它们的可变状态暴露到外部来破坏 Actor 的封装，则会返回正常的 Java 并发域，并存在所有的缺点。
 3. Actor 被设计成行为和状态的容器，接受这一点意味着不经常在消息中发送行为（使用 Scala 闭包可能很诱人）。其中一个风险是不小心在 Actor 之间共享可变状态，而这种对 Actor 模型的违反不幸地破坏了所有属性。
 4. 顶级 Actor 是错误内核的最核心部分，因此要谨慎地创建它们，并且更倾向于真正的分层系统。这对于故障处理（同时考虑配置的粒度和性能）有好处，而且它还减少了对守护者 Actor 的压力，如果使用过度，这是一个单一的竞争点。

### 3.2.4. 你不应该关心的事
Actor 系统管理配置使用的资源，以便运行其包含的 Actor。在一个这样的系统中，可能有数百万的 Actor，毕竟所有的赞歌（`mantra`）都是将他们视为丰富的，并且他们在每个实例的开销只有大约 300 字节。当然，在大型系统中处理消息的确切顺序不受应用程序作者的控制，但这也是无意的。

### 3.2.5. 终止 ActorSystem

当你知道应用程序的所有操作都已完成时，可以调`ActorSystem`的`terminate`方法。这将停止守护者 Actor，而守护者 Actor 又将递归地停止其所有子 Actor，即系统守护者。

如果要在终止`ActorSystem`时执行某些操作，请查看「[协调关闭](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/actors.md#%E5%8D%8F%E8%B0%83%E5%85%B3%E9%97%AD)」。

----------

**英文原文链接**：[Actor Systems](https://doc.akka.io/docs/akka/current/general/actor-systems.html).

----------


## 3.3. 什么是 Actor？
在「[Actor 系统](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/actor-systems.md)」这一节中，我们介绍了 Actor 是如何形成层次结构的，以及其是构建应用程序的最小单元。本节将单独地研究 Actor，解释在实现它时遇到的概念。有关所有细节的更深入探讨，请参阅「[Actors](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/actors.md)」。

Actor 是状态、行为、邮箱、子 Actor 和监督者策略（`Supervisor Strategy`）的容器。所有这些都封装在一个 Actor 的引用之后。一个值得注意的方面是，Actor 有一个明确的生命周期，当不再被引用时它们不会被自动销毁；在创建了一个生命周期之后，你有责任确保它最终会被终止，这也让你能够控制当 Actor 终止时如何释放资源。

### 3.3.1. Actor 引用
如下面详细介绍的，为了从 Actor 模型中获益，需要将 Actor 对象从外部屏蔽。因此，使用 Actor 引用将 Actor 表示为外部对象，这些引用是可以自由地传递且不受限制的对象。这种分为内部对象和外部对象的方法可以实现所有所需操作的透明性：在不需要更新其他地方引用的情况下重新启动 Actor，将实际的 Actor 对象放在远程主机上，在完全不同的应用程序中向 Actor 发送消息。但最重要的一点是，除非 Actor 不明智地发布了这些信息，否则不可能从外部观察 Actor 的内部并掌握其状态。

### 3.3.2. 状态
Actor 对象通常包含一些反映 Actor 可能处于的状态的变量。这可以是一个显式状态机（例如，使用「[FSM](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/fsm.md)」模块），也可以是一个计数器、一组监听器、挂起的请求等。这些数据使 Actor 有价值，并且必须防止其他 Actor 损坏它们。好消息是，从概念上讲，Akka 的每个 Actor 都有自己的轻量级线程，这完全与系统的其他部分隔离开来。这意味着，不必使用锁来同步访问，你可以编写 Actor 代码，而不必担心并发性。

在幕后，Akka 将在一组真正的线程上运行一组 Actor，在这些线程中，通常许多 Actor 共享一个线程，随后对一个 Actor 的调用可能最终在不同的线程上进行处理。Akka 确保这个实现细节不会影响处理 Actor 的状态。

因为内部状态对 Actor 的操作至关重要，所以状态不一致是致命的。因此，当 Actor 失败并由其监督者重新启动时，将从头开始创建状态，就像第一次创建 Actor 时一样。这是为了使系统能够自我修复。

或者，可以通过持久化接收到的消息并在重新启动后重播（请参见「[持久化](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/persistence.md)」），将 Actor 的状态自动恢复到重新启动前的状态。

### 3.3.3. 行为
每次处理消息时，它都与 Actor 的当前行为相匹配。行为（`Behavior`）指的是一个函数，它定义了在该时间点对消息做出反应时要采取的操作，例如，如果客户端被授权，就转发一个请求，否则就拒绝它。这种行为可能会随着时间的推移而改变，例如，由于不同的客户端随着时间的推移而获得授权，或者因为 Actor 可能会进入“停止服务”模式，然后返回。这些更改是通过从行为逻辑中读取的状态变量中对它们进行编码来实现的，或者函数本身可以在运行时交换出来，请参阅`become`和`unbecome`操作。但是，在构造 Actor 对象期间定义的初始行为是特殊的，因为重新启动 Actor 会将其行为重置为初始行为。

### 3.3.4. 邮箱
Actor 的目的是处理消息，这些消息是从其他 Actor（或从 Actor 系统外部）发送给 Actor 的。连接发送方和接收方的部分是 Actor 的邮箱：每个 Actor 只有一个邮箱，所有发送方都将其����息排队。排队是按发送操作的时间顺序发生的，这意味着由于在线程间分发 Actor 的明显随机性，不同 Actor 发送的消息在运行时可能没有定义顺序。另一方面，从同一个 Actor 向同一个目标发送多条消息将以相同的顺序将它们排队。

有不同的邮箱实现可供选择，默认为`FIFO`：Actor 处理的消息的顺序与它们排队的顺序匹配。这通常是一个很好的默认值，但是应用程序可能需要将某些消息优先于其他消息。在这种情况下，优先级邮箱将不总是在末尾排队，而是在消息优先级指定的位置排队，甚至可能在前面。当使用这样的队列时，处理的消息的顺序将自然地由队列的算法定义，通常不是`FIFO`。

Akka 与其他一些 Actor 模型实现不同的一个重要特性是，当前行为必须始终处理下一条出列的消息，没有扫描邮箱以查找下一条匹配的消息。除非重写此行为，否则处理消息失败通常被视为失败。

### 3.3.5. 子 Actor
每个 Actor 都可能是一个监督者：如果它为分配子任务创建子 Actor，它将自动对它们进行监督。子列表在 Actor 的上下文中维护，并且 Actor 可以访问它。对列表的修改是通过创建`(context.actorOf(...))`或停止`(context.stop(child))`子项来完成的，这些操作会立即反映出来。实际的创建和终止操作以异步方式在后台发生，因此它们不会“阻塞”其监督者。

### 3.3.6. 监督者策略
Actor 的最后一个部分是其处理子 Actor 错误的策略。对于「[监督和监控](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/supervision.md)」中描述的每一个传入策略，Akka 将透明地进行故障处理。由于该策略是如何构建 Actor 系统的基础，因此一旦创建了 Actor，就不能更改它。

考虑到每个 Actor 只有一个这样的策略，这意味着如果不同的策略应用于一个 Actor 的不同子代，那么这些子代应该按照匹配的策略分组到中级监督者之下，然后根据任务拆分为子任务，再一次组织 Actor 系统。

### 3.3.7. 当 Actor 终止
一旦一个 Actor 终止，即以一种不被重启处理的方式失败、自行停止或被其监督者停止，它将释放其资源，将其邮箱中的所有剩余邮件排入系统的“死信邮箱”，该邮箱将它们作为死信（`DeadLetters`）转发到事件流（`EventStream`）。然后在 Actor 引用中用系统邮箱替换原 Actor 的邮箱，将所有新消息作为死信重定向到事件流。但是，这是在尽最大努力的基础上完成的，因此不要依赖它来构建“有保证的消息传递”。

我们的测试启发了我们不只是静默地转储消息的原因：我们在发送死信的事件总线上注册`TestEventListener`，它将记录收到的每个死信的警告，这对于更快地破译测试失败非常有帮助。可以想象，此功能也可以用于其他目的。


----------

**英文原文链接**：[What is an Actor?](https://doc.akka.io/docs/akka/current/general/actors.html).

## 3.4. 监督和监控
本章概述了监督（`supervision`）背后的概念、提供的原语及其语义。有关如何转换为真实代码的详细信息，请参阅 Scala 和 Java API 的相应章节。

### 3.4.1. 示例项目
你可以查看「[监督示例项目](https://developer.lightbend.com/start/?group=akka&project=akka-samples-supervision-java)」，以了解实际使用的情况。

### 3.4.2. 监督意味着什么？
正如「[Actor 系统](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/actor-systems.md)」中监督所描述的，Actor 之间的依赖关系是：`supervisor`将任务委托给子级，因此必须对其失败作出响应。当子级检测到故障（即抛出异常）时，它会挂起自身及其所有下级，并向其监督者发送一条消息，也就是故障信号。根据监督工作的性质和失败的性质，监督者有以下四种选择：

- 恢复子级，保持其累积的内部状态
- 重新启动子级，清除其累积的内部状态
- 永久停止子级
- 使失败升级，从而使自己失败（*译者说，即继续向上一级监督者发送失败消息*）

始终将一个 Actor 视为监管层级的一部分是很重要的，这解释了第四个选择的存在（作为一个监督者也从属于上一级的另一个监督者），并对前三个有影响：恢复一个 Actor 恢复其所有子级，重新启动一个 Actor 需要重新启动其所有子级（如需更多详细信息，请参见下文），同样，终止 Actor 也将终止其所有子级。需要注意的是，`Actor`类的`preRestart`钩子的默认行为是在重新启动之前终止它的所有子级，但是这个钩子可以被重写；递归重新启动应用于执行这个钩子之后剩下的所有子级。

每个监督者都配置了一个函数，将所有可能的故障原因（即异常）转换为上面给出的四个选项之一；值得注意的是，该函数不将故障 Actor 的身份（`identity`）作为输入。这样的结构似乎不够灵活，很容易找到类似的例子，例如希望将不同的策略应用于不同的子级。在这一点上，重要的是要理解监督是关于形成一个递归的故障处理结构。如果你试图在一个层面上做的太多，就很难解释了，因此在这种情况下，建议的方法是增加一个层面（`level`）的监督。

Akka 实现了一种称为“父母监督（`parental supervision`）”的特殊形式。Actor 只能由其他 Actor 创建，其中顶级 Actor 由库提供，每个创建的 Actor 都由其父 Actor 监督。这种限制使得 Actor 监督层次的形成变得含蓄，并鼓励做出合理的设计决策。应当指出的是，这也保证了 Actor 不能成为孤儿，也不能从外部依附于监管者，否则他们可能会不知不觉地被抓起来（`which might otherwise catch them unawares`）。此外，这也为 Actor 应用程序（子树，`sub-trees of`）生成了一个自然、干净的关闭过程。

- **警告**：与监督（`supervision`）相关的父子通信通过特殊的系统消息进行，这些消息的邮箱与用户消息分开。这意味着与监督相关的事件并不是相对于普通消息确定的顺序。一般来说，用户不能影响正常消息和故障通知的顺序。有关详细信息和示例，请参阅「[讨论：消息排序](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/message-delivery-reliability.md#%E8%AE%A8%E8%AE%BA%E6%B6%88%E6%81%AF%E6%8E%92%E5%BA%8F)」部分。

### 3.4.3. 顶级监督者
![top-level-supervisors](https://github.com/guobinhit/akka-guide/blob/master/images/supervision/top-level-supervisors.png)

一个 Actor 系统在创建过程中至少会启动三个 Actor，如上图所示。有关 Actor 路径的详细信息，请参阅「[Actor 路径的顶级范围](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/addressing.md#actor-%E8%B7%AF%E5%BE%84%E7%9A%84%E9%A1%B6%E7%BA%A7%E8%8C%83%E5%9B%B4)」。

- `/user`: The Guardian Actor，最可能与之交互的 Actor 是所有用户创建的 Actor 的父级，守护者名为`/user`。使用`system.actorOf()`创建的 Actor 是此 Actor 的子级。这意味着当这个守护者终止时，系统中的所有正常 Actor 也将关闭。这也意味着守护者的监管策略决定了顶级正常 Actor 的监督方式。自 Akka 2.1 开始，可以使用`akka.actor.guardian-supervisor-strategy`来配置它，该设置采用了一个`SupervisorStrategyConfigurator`的完全限定类名。当守护者升级失败时，根守护者的响应将会终止守护者，这实际上将关闭整个 Actor 系统。
- `/system`: The System Guardian，为了实现有序的关闭顺序，引入了这个特殊的守护者，当所有正常的 Actor 都终止，日志记录也保持活动状态，即使日志记录本身也是使用 Actor 实现的。这是通过让系统守护者监视（`watch`）用户守护者并在接收到`Terminated`消息时启动自己的关闭来实现的。顶层系统 Actor 使用一种策略进行监督，该策略将在所有类型的`Exception`（其中，`ActorInitializationException`和`ActorKilledException`除外）上无限期重新启动，这将终止相关的子级。所有其他可抛的异常事件都会升级，这将关闭整个 Actor 系统。
- `/`: The Root Guardian，根守护者是所有所谓的“顶级” Actor 的祖父（`grand-parent`）级，并使用`SupervisorStrategy.stoppingStrategy`监督「[Actor 路径的顶级范围](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/addressing.md#actor-%E8%B7%AF%E5%BE%84%E7%9A%84%E9%A1%B6%E7%BA%A7%E8%8C%83%E5%9B%B4)」中提到的所有特殊 Actor，其目的是在任何类型的`Exception`情况下终止子 Actor。所有其他可抛的异常都会升级……但是给谁？因为每个真正的 Actor 都有一个监督者，所以根守护者的监督者不能是真正的 Actor。因为这意味着它是在“气泡的外面”，所以它被称为“气泡行者（`bubble-walker`）”。这是一个虚构的`ActorRef`，它在出现问题的第一个征兆时停止其子系统，并在根守护程序完全终止（所有子系统递归停止）后将 Actor 系统的`isTerminated`状态设置为`true`。

### 3.4.4. 重启意味着什么？
当与处理特定消息时失败的 Actor 一起出现时，失败的原因分为三类：

- 接收到特定的系统性（即编程）错误消息
- 处理消息过程中使用的某些外部资源出现故障
- Actor 的内部状态已损坏

除非能明确识别故障，否则不能排除第三种原因，这就导致了内部状态需要清除的结论。如果监督者决定其其他子级或本身不受损坏的影响，例如，由于有意识地应用了错误内核模式，因此最好重新启动子级。这是通过创建底层`Actor`类的新实例并将失败的实例替换为子`ActorRef`中的新实例来实现的；这样做的能力是将 Actor 封装在特殊引用中的原因之一。然后，新的 Actor 将继续处理其邮箱，这意味着重新启动在 Actor 除本身之外是不可见的，但有一个明显的例外，即发生故障的消息不会被重新处理。

重新启动期间事件的精确顺序如下：

 1. 挂起 Actor（这意味着在恢复之前它不会处理正常消息），并递归挂起所有子级
 2. 调用旧实例的`preRestart`钩子（默认为向所有子实例发送终止请求并调用`postStop`）
 3. 等待在`preRestart`期间被请求终止（使用`context.stop()`）的所有子级实际终止；就像所有 Actor 操作都是非阻塞的一样，最后一个被杀死的子级的终止通知将影响到下一步的进展。
 4. 通过再次调用最初提供的工厂来创建新的 Actor 实例
 5. 在新实例上调用`postRestart`（默认情况下，该实例还调用`preStart`）
 6. 向步骤 3 中未杀死的所有子级发送重新启动请求；从步骤 2 开始，重新启动的子级将递归地执行相同的过程。
 7. 恢复 Actor

### 3.4.5. 生命周期监控意味着什么？
- **注释**：Akka 中的生命周期监控通常被称为`DeathWatch`。

与上面描述的父母和子女之间的特殊关系不同，每个 Actor 可以监视（`monitor`）任何其他 Actor。由于 Actor 从完全活跃地创造中出现，并且在受影响的监督者之外无法看到重新启动，因此可用于监控的唯一状态更改是从活跃到死亡的过渡。因此，监控（`Monitoring`）被用来将一个 Actor 与另一个 Actor 联系起来，这样它就可以对另一个 Actor 的终止做出反应，而不是对失败做出反应的监督。

生命周期监控是使用监控 Actor 要接收的`Terminated`消息来实现的，在该消息中，默认行为是如果不进行其他处理，则抛出一个特殊的`DeathPactException`。为了开始监听`Terminated`消息，需要调用`ActorContext.watch(targetActorRef)`。若要停止监听，则需要调用`ActorContext.unwatch(targetActorRef)`。一个重要的属性是，不管监控请求和目标终止的顺序如何，消息都将被传递，即使在注册时目标已经死了，你仍然会收到消息。

如果监督者无法重新启动其子级，并且必须终止它们（例如，在 Actor 初始化期间发生错误时），则监控特别有用。在这种情况下，它应该监控这些子级并重新创建它们，或者计划自己在稍后重试。

另一个常见的用例是，Actor 需要在缺少外部资源的情况下失败，外部资源也可能是其自己的子资源之一。如果第三方通过`system.stop(child)`方法或发送`PoisonPill`终止子级，则监督者可能会受到影响。

#### 3.4.5.1. 使用 BackoffSupervisor 模式延迟重新启动
作为内置模式提供的`akka.pattern.BackoffSupervisor`实现了所谓的指数退避监督策略，在失败时再次启动子 Actor，并且每次重新启动之间的时间延迟越来越大。

当启动的 Actor 失败（故障可以用两种不同的方式来表示，通过一个 Actor 停止或崩溃）时，此模式非常有用，因为某些外部资源不可用，我们需要给它一些时间重新启动。一个主要示例是当「[PersistentActor](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/persistence.md)」因持久性失败而失败（通过停止）时，这表明数据库可能已关闭或过载，在这种情况下，在启动持久性 Actor 之前给它一点时间来恢复是很有意义的。

下面的 Scala 片段演示了如何创建一个退避监督者，在给定的 EchoActor 因故障停止后，该监督者将以 3、6、12、24 和最后 30 秒的间隔启动：

```scala
val childProps = Props(classOf[EchoActor])

val supervisor = BackoffSupervisor.props(
  Backoff.onStop(
    childProps,
    childName = "myEcho",
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxNrOfRetries = -1
  ))

system.actorOf(supervisor, name = "echoSupervisor")
```
与上述 Scala 代码等价的 Java 代码为：

```java
import java.time.Duration;

final Props childProps = Props.create(EchoActor.class);
final Props supervisorProps =
    BackoffSupervisor.props(
        Backoff.onStop(
            childProps,
            "myEcho",
            Duration.ofSeconds(3),
            Duration.ofSeconds(30),
            0.2)); // adds 20% "noise" to vary the intervals slightly

system.actorOf(supervisorProps, "echoSupervisor");
```
为了避免多个 Actor 在完全相同的时间点重新启动，例如，由于共享资源（如数据库在相同配置的时间间隔后关闭和重新启动），因此强烈建议使用`randomFactor`为回退间隔添加一点额外的变化。通过在重新启动间隔中增加额外的随机性，Actor 将在稍微不同的时间点开始，从而避免大流量峰值冲击恢复共享数据库或他们所需的其他资源。

还可以将`akka.pattern.BackoffSupervisor` Actor 配置为在 Actor 崩溃且监控策略决定应重新启动时，在延迟之后重新启动 Actor。

下面的 Scala 片段演示了如何创建一个退避监督者，在给定的 EchoActor 因某些异常而崩溃后，该监督者将以 3、6、12、24 和最后 30 秒的间隔启动：

```scala
val childProps = Props(classOf[EchoActor])

val supervisor = BackoffSupervisor.props(
  Backoff.onFailure(
    childProps,
    childName = "myEcho",
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxNrOfRetries = -1
  ))

system.actorOf(supervisor, name = "echoSupervisor")
```
与上述 Scala 代码等价的 Java 代码为：

```java
import java.time.Duration;

final Props childProps = Props.create(EchoActor.class);
final Props supervisorProps =
    BackoffSupervisor.props(
        Backoff.onFailure(
            childProps,
            "myEcho",
            Duration.ofSeconds(3),
            Duration.ofSeconds(30),
            0.2)); // adds 20% "noise" to vary the intervals slightly

system.actorOf(supervisorProps, "echoSupervisor");
```
`akka.pattern.BackoffOptions`可用于自定义退避监督者 Actor 的行为，以下是一些示例：

```scala
val supervisor = BackoffSupervisor.props(
  Backoff.onStop(
    childProps,
    childName = "myEcho",
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxNrOfRetries = -1
  ).withManualReset // the child must send BackoffSupervisor.Reset to its parent
    .withDefaultStoppingStrategy // Stop at any Exception thrown
)
```
上面的代码设置了一个退避监督者，要求子 Actor 在成功处理消息时向其父级发送`akka.pattern.BackoffSupervisor.Reset`消息，从而重置后退。它还使用默认的停止策略，任何异常都会导致子 Actor 停止。

```scala
val supervisor = BackoffSupervisor.props(
  Backoff.onFailure(
    childProps,
    childName = "myEcho",
    minBackoff = 3.seconds,
    maxBackoff = 30.seconds,
    randomFactor = 0.2, // adds 20% "noise" to vary the intervals slightly
    maxNrOfRetries = -1
  ).withAutoReset(10.seconds) // reset if the child does not throw any errors within 10 seconds
    .withSupervisorStrategy(
      OneForOneStrategy() {
        case _: MyException ⇒ SupervisorStrategy.Restart
        case _              ⇒ SupervisorStrategy.Escalate
      }))
```
上面的代码设置了一个退避监督者，如果抛出`MyException`，在退避后重新启动子级，而任何其他异常都将被升级。如果子 Actor 在 10 秒内没有抛出任何错误，则会自动重置后退。

### 3.4.6. One-For-One 策略 vs. All-For-One 策略
Akka 提供了两种监管策略：一种是`OneForOneStrategy`，另一种是`AllForOneStrategy`。两者都配置了从异常类型到监督指令（见上文）的映射，并限制了在终止之前允许子级失败的频率。它们之间的区别在于前者只将获得的指令应用于失败的子级，而后者也将其应用于所有的子级。通常，你应该使用`OneForOneStrategy`，如果没有明确指定，它也是默认的。

`AllForOneStrategy`适用于子级群体之间有很强的依赖性，以至于一个子 Actor 的失败会影响其他子 Actor 的功能，即他们之间的联系是不可分割的。由于重新启动无法清除邮箱，因此通常最好在失败时终止子级，并在监督者（通过监视子级的生命周期）中显式地重新创建它们；否则，你必须确保任何 Actor 都可以接受在重新启动之前排队但在重新启动之后处理消息。

在`All-For-One`策略中，通常停止一个子级将不会自动终止其他子级；通过监控他们的生命周期可以完成：如果监督者不处理`Terminated`消息，它将抛出`DeathPactException`（这取决于它的监督者），它将重新启动，默认的`preRestart`将终止其所有的子级。

请注意，创建的一次性 Actor 来自一个`all-for-one`监督者，通过临时 Actor 的失败升级影响所有其他的 Actor。如果不需要这样做，可以安装一个中间监督者；这可以通过为`worker`声明一个大小为 1 的路由器来实现，详见「[路由](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/routing.md)」。

----------

**英文原文链接**：[Supervision and Monitoring](https://doc.akka.io/docs/akka/current/general/supervision.html).


## 3.5. Actor引用、路径和地址

本章描述如何在可能的分布式 Actor 系统中标识和定位 Actor。它与这样一个核心理念紧密相连：「[Actor 系统](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/actor-systems.md)」形成了内在的监督层次结构，并且 Actor 之间的通信在跨多个网络节点的位置方面是透明的。

![20200805105312](https://liulv.work/images/img/20200805105312.png)

上图显示了 Actor 系统中最重要的实体之间的关系，有关详细信息，请继续阅读。

### 3.5.1. 什么是 Actor 的引用？

Actor 引用是`ActorRef`的一个子类型，其首要目的是支持将消息发送给它所代表的 Actor。每个 Actor 都可以通过`self`字段访问其规范（本地）引用；默认情况下，对于发送给其他 Actor 的所有消息，此引用也作为发送者引用包含在内。相应地，在消息处理期间，Actor 可以通过`sender()`方法访问表示当前消息发送者的引用。

根据 Actor 系统的配置，支持几种不同类型的 Actor 引用：

- 纯本地 Actor 引用由未配置为支持网络功能的 Actor 系统使用。如果通过网络连接发送到远程 JVM，这些 Actor 引用将不起作用。
- 启用远程处理时，支持网络功能的 Actor 系统使用本地 Actor 引用，这些引用表示同一个 JVM 中的 Actor。为了在发送到其他网络节点时也可以访问，这些引用包括协议和远程寻址信息。
- 本地 Actor 引用的一个子类型用于路由器（即 Actor 混合在`Router`特性中）。它的逻辑结构与前面提到的本地引用相同，但是向它们发送消息会直接发送给它们的一个子级。
- 远程 Actor 引用表示可以使用远程通信访问的 Actor，即向其发送消息将透明地序列化消息并将其发送到远程 JVM。
- 有几种特殊类型的 Actor 引用，其行为类似于所有实际用途的本地 Actor 引用：
  - `PromiseActorRef`是一个`Promise`为了完成 Actor 响应的特殊表示。`akka.pattern.ask`创建这个 Actor 引用。
  - `DeadLetterActorRef`是死信服务的默认实现，Akka 将其目的地关闭或不存在的所有消息路由到该服务。
  - `EmptyLocalActorRef`是 Akka 在查找不存在的本地 Actor 路径时返回的：它相当于一个`DeadLetterActorRef`，但它保留了自己的路径，以便 Akka 可以通过网络发送它，并将其与该路径的其他现有 Actor 引用进行比较，其中一些引用可能是在 Actor 死亡之前获得的。
- 还有一些一次性（`one-off`）的内部实现，你应该永远都不会真正看到：
  - 有一个 Actor 引用，它不代表一个 Actor，只作为根守护者的伪监督者（`pseudo-supervisor`），我们称之为“在时空的气泡中行走的人”。
  - 在实际启动 Actor 创建工具之前启动的第一个日志记录服务是一个假 Actor 引用，它接受日志事件并将其直接打印到标准输出；它是`Logging.StandardOutLogger`。

### 3.5.2. 什么是 Actor 路径？

由于 Actor 是以严格的层次结构方式创建的，因此存在一个唯一的 Actor 名称序列，该序列通过递归地沿着子级和父级之间的监督链接向下到 Actor 系统的根来给出。这个序列可以看作是文件系统中的封闭文件夹，因此我们采用名称`path`来引用它，尽管 Actor 层次结构与文件系统层次结构有一些基本的区别。

一个 Actor 路径由一个锚点组成，该锚点标识 Actor 系统，然后连接从根守护者到指定的 Actor 的路径元素；路径元素是被遍历的 Actor 的名称，由斜线分隔。

#### 3.5.2.1. Actor 的引用和路径有什么区别？
Actor 引用指定一个单独的 Actor，引用的生命周期与该 Actor 的生命周期匹配；Actor 路径表示一个可能由 Actor 位置或不由 Actor 位置标识的名称，并且路径本身没有生命周期，它永远不会变为无效。你可以在不创建 Actor 的情况下创建 Actor 路径，但在不创建相应的 Actor 的情况下无法创建 Actor 引用。

你可以创建一个 Actor，终止它，然后使用相同的 Actor 路径创建一个新的 Actor。新创建的 Actor 是原 Actor 的化身。他们不是同一个 Actor。Actor 引用旧的化身对新的化身无效。发送到旧 Actor 引用的消息将不会传递到新的化身，即使它们具有相同的路径。

#### 3.5.2.2. Actor 路径锚点
每个 Actor 路径都有一个地址组件，描述了协议和位置，通过这些协议和位置可以访问相应的 Actor，路径中的元素是从根目录向上的层次结构中 Actor 的名称。例如：

```
"akka://my-sys/user/service-a/worker1"                   // purely local
"akka.tcp://my-sys@host.example.com:5678/user/service-b" // remote
```

在这里，`akka.tcp`是 2.4 版本的默认远程传输；其他传输是可插拔的。主机和端口部分（如示例中的`host.example.com:5678`）的解释取决于所使用的传输机制，但必须遵守 URI 结构规则。

#### 3.5.2.3. 逻辑 Actor 路径
通过跟踪指向根守护者的父级监督链接获得的唯一路径称为逻辑 Actor 路径。此路径与 Actor 的创建祖先完全匹配，因此只要设置了 Actor 系统的远程处理配置（以及路径的地址组件），它就完全具有确定性。

#### 3.5.2.4. 物理 Actor 路径
虽然逻辑 Actor 路径描述了一个 Actor 系统中的功能位置，但是基于配置的远程部署意味着可以在与其父系统不同的网络主机上创建 Actor，即在不同的 Actor 系统中。在这种情况下，从根守护者向上遵循 Actor 路径需要遍历网络，这是一个昂贵的操作。因此，每个 Actor 也有一个物理路径，从实际 Actor 对象所在的 Actor 系统的根守护者开始。当查询其他 Actor 时，使用此路径作为发送者引用，将允许他们直接回复此 Actor，从而最小化路由所导致的延迟。

一个重要的方面是，物理 Actor 路径从不跨越多个 Actor 系统或 JVM。这意味着，如果一个 Actor 的祖先被远程监控，那么它的逻辑路径（监督层次）和物理路径（Actor 部署）可能会发生偏离。

#### 3.5.2.5. Actor 路径别名或符号链接？

在一些实际的文件系统中，你可能会想到一个 Actor 的“路径别名”或“符号链接”，即一个 Actor 可以使用多个路径访问。但是，你应该注意，Actor 层次结构不同于文件系统层次结构。不能自由地创建 Actor 路径（如符号链接）来引用任意的 Actor。如上述逻辑和物理 Actor 路径部分所述，Actor 路径必须是表示监督层次结构的逻辑路径，或者是表示 Actor 部署的物理路径。

### 3.5.3. 如何获得 Actor 引用？
对于如何获取 Actor 引用，有两个通用的类别：通过创建 Actor 或通过查找 Actor，后者的功能包括从具体的 Actor 路径创建 Actor 引用和查询逻辑的 Actor 层次结构。

#### 3.5.3.1. 创建 Actor
Actor 系统通常是通过使用`ActorSystem.actorOf`方法在守护 Actor 下创建 Actor，然后从创建的 Actor 中使用`ActorContext.actorOf`来生成 Actor 树来启动的。这些方法返回对新创建的 Actor 的引用。每个 Actor 都可以（通过其`ActorContext`）直接访问其父级、自身及其子级的引用。这些引用可以在消息中发送给其他 Actor，从而使这些 Actor 能够直接回复。

#### 3.5.3.2. 用具体的路径查找 Actor
此外，可以使用`ActorSystem.actorSelection`方法查找 Actor 引用。选择（`selection`）可用于与所述 Actor 通信，并且在传递每条消息时查找所述选择对应的 Actor。

要获取绑定到特定 Actor 生命周期的`ActorRef`，你需要向 Actor 发送消息，例如内置标识消息，并使用来自 Actor 的答复的`sender()`引用。

#### 3.5.3.3. 绝对路径 vs. 相对路径
除了`ActorSystem.actorSelection`，还有`ActorContext.actorSelection`，在任何 Actor 中都可以作为`context.actorSelection`使用。这将生成一个 Actor 选择，与`ActorSystem`上的孪生兄弟非常相似，但它不是从 Actor 树的根开始查找路径，而是从当前 Actor 开始。由两个点（`".."`）组成的路径元素可用于访问父 Actor。例如，你可以向特定的兄弟姐妹 Actor 发送消息：

```
context.actorSelection("../brother") ! msg
```

绝对路径也可以用通常的方式在上下文中查找，即

```
context.actorSelection("/user/serviceA") ! msg
```

这都将按预期工作。

#### 3.5.3.4. 查询逻辑 Actor 层次结构

由于 Actor 系统形成了类似于文件系统的层次结构，因此在路径上进行匹配的方式与 Unix shells 支持的方式相同：你可以用通配符(`*«*»*`和`«?»`）替换（部分）路径元素名制定一个可以匹配零个或更多实际 Actor 的选择。因为结果不是单个 Actor 引用，所以它具有不同的`ActorSelection`类型，并且不支持`ActorRef`执行的完整操作集。可以使用`ActorSystem.actorSelection`和`ActorContext.actorSelection`方法来制定选择，并且确实支持发送消息：

```
context.actorSelection("../*") ! msg
```

将向包括当前 Actor 在内的所有兄弟姐妹 Actor 发送`msg`。对于使用`actorSelection`获取的引用，将遍历监督层次结构以执行消息发送。由于与选定内容匹配的 Actor 的确切集合可能会发生变化，即使消息正在传递给收件人，也不可能观看选定内容的实时变化。为了做到这一点，通过发送一个请求并收集所有答案，提取发送者引用，然后观察所有发现的具体 Actor 来解决不确定性。这种解决选择的方案可以在将来的版本中加以改进。

总结：`actorOf` vs. `actorSelection`

- **注释**：以上部分的详细描述可以总结和记忆如下：
  - `actorOf`只创建了一个新的 Actor，它将其创建为调用此方法的上下文（可能是任何 Actor 或 Actor 系统）的直接子级。
  - `actorSelection`仅在传递消息时查找现有的 Actor，即不创建 Actor，或在创建选择时验证 Actor 的存在。

### 3.5.4. Actor 引用和路径相等
`ActorRef`的相等符合`ActorRef`对应于目标 Actor 化身的意图。当两个 Actor 引用具有相同的路径并指向相同的 Actor 化身时，它们将被比较为相等的。指向终止的 Actor 的引用与指向具有相同路径的其他（重新创建的）Actor 的引用不同。请注意，由失败引起的 Actor 重新启动仍然意味着它是同一个 Actor 的化身，即对于`ActorRef`的使用者来说，重新启动是不可见的。

如果需要跟踪集合中的 Actor 引用，而不关心具体的 Actor 化身，则可以使用`ActorPath`作为键，因为在比较 Actor 路径时不考虑目标 Actor 的标识符。

### 3.5.5. 复用 Actor 路径
当一个 Actor 被终止时，它的引用将指向死信邮箱，`DeathWatch`将发布其最终的转换，一般情况下，它不会再次恢复生命（因为 Actor 的生命周期不允许这样做）。虽然可以在以后用相同的路径创建一个 Actor，因为如果不保留所有已创建的 Actor 集，就不可能执行相反的操作，但这不是一个好的实践：用`actorSelection`向一个“死亡”的 Actor 发送的消息会突然重新开始工作，但没有任何顺序保证。在这个转换和任何其他事件之间，该路径所代表的新 Actor 可能会接收到发往该路径所代表的前一个 Actor 的消息。

在非常特殊的情况下，这可能是正确的做法，但一定要将处理这一点严格限制在 Actor 的监督者身上，因为只有这样的 Actor 才能可靠地检测到名字的正确注销，在此之前，新子 Actor 的创建将失败。

当测试对象依赖于在特定路径上实例时，也可能需要在测试期间使用它。在这种情况下，最好模拟其监督者，以便将`Terminated`消息转发到测试过程中的适当点，以便后者等待正确的名称注销。

### 3.5.6. 远程部署的交互作用

当 Actor 创建子节点时，Actor 系统的部署程序将决定新 Actor 是驻留在同一个 JVM 中，还是驻留在另一个节点上。在第二种情况下，Actor 的创建将通过网络连接触发，在不同的 JVM 中发生，从而在不同的 Actor 系统中发生。远程系统将把新 Actor 放在为此目的保留的特殊路径下，新 Actor 的监督者将是远程 Actor 引用（表示触发其创建的 Actor）。在这种情况下，`context.parent`（监督者引用）和`context.path.parent`（Actor 路径中的父节点）不表示同一个 Actor。但是，在监督者中查找子级的名称会在远程节点上找到它，保留逻辑结构，例如发送到未解析的 Actor 引用时。

![actor-path-in-system](https://github.com/guobinhit/akka-guide/blob/master/images/addressing/actor-path-in-system.png)

### 3.5.7. 地址部分用于什么？
当通过网络发送 Actor 引用时，它由其路径表示。因此，路径必须完全编码向底层 Actor 发送消息所需的所有信息。这是通过在路径字符串的地址部分编码协议、主机和端口来实现的。当 Actor 系统从远程节点接收到 Actor 路径时，它检查该路径的地址是否与该 Actor 系统的地址匹配，在这种情况下，它将解析为 Actor 的本地引用。否则，它将由远程 Actor 引用表示。

### 3.5.8. Actor 路径的顶级范围
在路径层次结构的根目录下，存在根目录守护者，在其上可以找到所有其他 Actor；其名称为`"/"`。下一级包括以下内容：

- `"/user"`是所有用户创建的 Actor 的顶级守护者 Actor；使用`ActorSystem.actorOf`创建的 Actor 位于此 Actor 的下面。
- `"/system"`是所有系统创建的 Actor 的顶级守护者 Actor，例如在 Actor 系统开始时通过配置自动部署日志监听器。
- `"/deadletters"`是死信 Actor，即所有发送到已停止或不存在的 Actor 的消息都会重新路由（在尽最大努力的基础上：消息也可能会丢失，即使是在本地 JVM 中）。
- `"/temp"`是所有短期系统创建的 Actor 的守护者 Actor，例如在`ActorRef.ask`的实现中使用的 Actor。
- `"/remote"`是一个人工路径，其下面所有 Actor 的监督者都是远程 Actor 引用。

像这样为 Actor 构建名称空间的需要源于一个中心且非常简单的设计目标：层次结构中的所有内容都是一个 Actor，并且所有 Actor 都以相同的方式工作。因此，你不仅可以查找你创建的 Actor，还可以查找系统守护者并向其发送消息（在本例中，它将尽职尽责地丢弃该消息）。这一强有力的原则意味着不需要记住任何怪癖，它使整个系统更加统一和一致。

如果你想更多地了解 Actor 的顶层（`top-level`）结构，可见「[The Top-Level Supervisors](https://doc.akka.io/docs/akka/current/general/supervision.html#toplevel-supervisors)」。

## 3.6. 位置透明
上一节描述了如何使用 Actor 路径来启用位置透明（`location transparency`）。这个特殊的特性需要一些额外的解释，因为在编程语言、平台和技术的上下文中，相关术语“透明远程处理”的使用方式非常不同。

### 3.6.1. 默认分布
Akka 中的所有内容都设计成在分布式环境中工作：Actor 的所有交互都使用纯消息传递，而所有内容都是异步的。这项工作的目的是确保在单个 JVM 中或在成百上千台机器的集群上运行时，所有功能都可以平等地使用。实现这一点的关键是通过优化从远程到本地，而不是通过泛化从本地到远程。有关第二种方法必然失败的原因的详细讨论，请参阅这篇「[经典](https://doc.akka.io/docs/misc/smli_tr-94-29.pdf)」文章。

### 3.6.2. 打破透明的方法
Akka 的真实情况不必是使用它的应用程序的真实情况，因为分布式执行的设计对可能的情况有一些限制。最明显的一点是，通过线路发送的所有消息都必须是可序列化的。虽然不太明显，但如果要在远程节点上创建 Actor，则包含用作 Actor 工厂（即在`Props`中）的闭包。

另一个后果是，每件事都需要意识到所有的交互都是完全异步的，在计算机网络中，这可能意味着消息到达收件人可能需要几分钟时间（取决于配置）。这还意味着消息丢失的概率要比一个 JVM 中的高很多，在同一个 JVM 中，它接近于零，但仍然是：没有硬保证！

### 3.6.3. 如何使用远程处理？
我们将透明的概念限制在几乎没有用于 Akka 远程处理层的 API：它纯粹是由配置驱动的。只需根据前面几节中概述的原则编写应用程序，然后在配置文件中指定 Actor 子树的远程部署。这样，你的应用程序就可以在不需要触摸代码的情况下进行扩展。API 中唯一允许对远程部署产生编程影响的部分是，`Props`包含一个可以设置为特定`Deploy`实例的字段；这与将等效部署放入配置文件（如果两者都给出，则配置文件获胜）的效果相同。

### 3.6.4. Peer-to-Peer vs. Client-Server
Akka 远程处理是一种以对等（`peer-to-peer`，或者称之为“点对点”）方式连接 Actor 系统的通信模块，是 Akka 集群的基础。远程处理的设计由两个（相关的）设计决策驱动：

 1. 所涉及系统之间的通信是对称的：如果系统 A 可以连接到系统 B，那么系统 B 也必须能够独立地连接到系统 A。
 2. 就连接模式而言，通信系统的角色是对称的：没有只接受连接的系统，也没有只启动连接的系统。

这些决策的结果是不可能安全地创建具有预定义角色的纯“客户端-服务器”设置（违反假设 2）。对于“客户端-服务器”设置，最好使用 HTTP 或 Akka I/O。

- **重要提示**：使用涉及网络地址转换的设置、负载均衡器或 Docker 容器违反假设 1，除非在网络配置中采取其他步骤以允许相关系统之间的对称通信。在这种情况下，可以将 Akka 配置为绑定到不同于用于在 Akka 节点之间建立连接的网络地址。详见「[ Akka behind NAT or in a Docker container](https://doc.akka.io/docs/akka/current/remoting.html#remote-configuration-nat)」。

### 3.6.5. 用路由器扩容标记点

除了能够在集群的不同节点上运行 Actor 系统的不同部分之外，还可以通过将支持并行化的 Actor 子树（例如，搜索引擎并行处理不同的查询）相乘，扩展到更多的核心。然后，克隆可以以不同的方式被路由到，例如循环。实现这一点的唯一必要的是，开发人员需要将某个 Actor 声明为`withRouter`，然后取而代之的是，将创建一个路由器 Actor，该 Actor 将生成所需类型的可配置子级，并以配置的方式路由到这些子级。一旦声明了这样的路由器，就可以从配置文件中自由地覆盖其配置，包括将其与（部分）子级的远程部署混合在一起。在「[路由](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/routing.md)」中可以阅读更多关于此的信息。

----------

**英文原文链接**：[Location Transparency](https://doc.akka.io/docs/akka/current/general/remoting.html).

## 3.7. Akka和Java内存模型

使用Lightbend平台（包括Scala和Akka）的主要好处在于，它简化了编写并发软件的过程。本文讨论了Lightbend平台（尤其是Akka）如何在并发应用程序中处理共享内存。

### 3.7.1. Java内存模型

在Java 5之前，Java内存模型（JMM）定义不正确。当多个线程访问共享内存时，可能会得到各种奇怪的结果，例如：

* 一个线程看不到其他线程写入的值：可见性问题
* 线程观察到其他线程的“不可能”行为，这是由于未按预期的顺序执行指令引起的：指令重新排序问题。

随着Java 5中JSR 133的实现，许多这些问题已得到解决。JMM是基于“先于发生”关系的一组规则，该规则约束何时一个内存访问必须在另一个内存访问之前发生，反之，何时允许它们无序发生。这些规则的两个示例是：

* **监控器锁定规则**：在随后每次获取相同锁定之前，都会释放锁定。
* **易失性变量规则**：在每次后续读取同一易失性变量之前发生易失性变量的写操作

尽管JMM看起来很复杂，但该规范试图在易用性与写入高性能和可伸缩并发数据结构的能力之间找到平衡。

### 3.7.2. Actors 和 Java 内存模型

在Akka的Actors实现中，有两种方式可以让多线程在共享内存上执行动作:

* 如果一个消息被发送给一个Actor(例如另一个Actor)。在大多数情况下，消息是不可变的，但如果消息不是一个正确构造的不可变对象，没有“happens before”规则，接收者可能会看到部分初始化的数据结构，甚至可能凭空看到值(long /double)。
* 如果actor在处理消息时更改了其内部状态，并在稍后处理另一条消息时访问该状态。重要的是要认识到，对于actor模型，您不能保证相同的线程将为不同的消息执行相同的actor。

为了防止actor上的可见性和重新排序问题，Akka保证了以下两个“happens before”规则：

* Actor发送规则:将消息发送给actor的过程发生在同一actor接收消息之前。
* Actor后续处理规则:同一Actor在处理下一条消息之前处理一条消息。

>**请注意**
>用外行人的话说，这意味着当actor处理下一条消息时，actor内部字段的更改是可见的。因此，actor中的字段不需要是volatile或等效的。

这两个规则仅适用于相同的actor实例，如果使用不同的actor则无效

### 3.7.3. Futures和JAVA内存

未来的完成“happens before”对其注册的任何回调调用被执行之前。

我们建议不要关闭非final字段(Java中的final和Scala中的val)，如果你选择关闭非final字段，它们必须被标记为volatile，以便字段的当前值对回调可见。

如果关闭引用，还必须确保引用的实例是线程安全的。我们强烈建议远离使用锁的对象，因为它会带来性能问题，在最坏的情况下，还会导致死锁。

### 3.7.4. Actors 和共享可变状态

因为Akka运行在JVM上，所以仍然需要遵守一些规则。

最重要的是，你不能关闭内部 Actor 状态，并暴露给其他线程:

```java
class MyActor extends AbstractBehavior<MyActor.Command> {

  interface Command {}

  class Message implements Command {
    public final ActorRef<Object> otherActor;

    public Message(ActorRef<Object> replyTo) {
      this.otherActor = replyTo;
    }
  }

  class UpdateState implements Command {
    public final String newState;

    public UpdateState(String newState) {
      this.newState = newState;
    }
  }

  private String state = "";
  private Set<String> mySet = new HashSet<>();

  public MyActor(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(Message.class, this::onMessage)
        .onMessage(UpdateState.class, this::onUpdateState)
        .build();
  }

  private Behavior<Command> onMessage(Message message) {
    // Very bad: shared mutable object allows
    // the other actor to mutate your own state,
    // or worse, you might get weird race conditions
    message.otherActor.tell(mySet);

    // Example of incorrect approach
    // Very bad: shared mutable state will cause your
    // application to break in weird ways
    CompletableFuture.runAsync(
        () -> {
          state = "This will race";
        });

    // Example of incorrect approach
    // Very bad: shared mutable state will cause your
    // application to break in weird ways
    expensiveCalculation()
        .whenComplete(
            (result, failure) -> {
              if (result != null) state = "new state: " + result;
            });

    // Example of correct approach
    // Turn the future result into a message that is sent to
    // self when future completes
    CompletableFuture<String> futureResult = expensiveCalculation();
    getContext()
        .pipeToSelf(
            futureResult,
            (result, failure) -> {
              if (result != null) return new UpdateState(result);
              else throw new RuntimeException(failure);
            });

    // Another example of incorrect approach
    // mutating actor state from ask future callback
    CompletionStage<String> response =
        AskPattern.ask(
            message.otherActor,
            Query::new,
            Duration.ofSeconds(3),
            getContext().getSystem().scheduler());
    response.whenComplete(
        (result, failure) -> {
          if (result != null) state = "new state: " + result;
        });

    // use context.ask instead, turns the completion
    // into a message sent to self
    getContext()
        .ask(
            String.class,
            message.otherActor,
            Duration.ofSeconds(3),
            Query::new,
            (result, failure) -> {
              if (result != null) return new UpdateState(result);
              else throw new RuntimeException(failure);
            });
    return this;
  }

  private Behavior<Command> onUpdateState(UpdateState command) {
    // safe as long as `newState` is immutable, if it is mutable we'd need to
    // make a defensive copy
    this.state = command.newState;
    return this;
  }
}
```

消息应该是不可变的，这是为了避免共享的可变状态陷阱。


## 3.8. 消息传递可靠性

Akka 帮助你构建可靠的应用程序，这些应用程序可以在一台机器中使用多个处理器核心（`scaling up`，纵向扩展）或分布在计算机网络中（`scaling out`，横向扩展）。实现这一点的关键抽象是，代码单元 Actor 之间的所有交互都是通过消息传递进行的，这就是为什么 Actor 之间传递消息的精确语义应该有自己的章节。

为了给下面的讨论提供一些上下文，请考虑跨多个网络主机的应用程序。无论是发送到本地 JVM 上的 Actor 还是发送到远程 Actor，通信的基本机制都是相同的，但是在传递延迟（可能还取决于网络链路的带宽和消息大小）和可靠性方面会有明显的差异。在远程消息发送的情况下，涉及到更多的步骤，这意味着更多的步骤可能出错。另一个方面是本地发送将在同一个 JVM 中传递对消息的引用，而对发送的底层对象没有任何限制，而远程传输将限制消息的大小。

因此假设 Actor 之间的远程通信是安全的，这个一个悲观的赌注。它意味着只依赖于那些总是有保证的属性，这些属性将在下面详细讨论。这在 Actor 的实现中有一些开销。如果你愿意牺牲完整的位置透明性，例如在一组紧密合作的 Actor 的情况下，你可以将他们始终放在同一个 JVM 上，并在消息传递上享受更严格的保证。下文将进一步讨论这种权衡的细节。

作为补充部分，我们对如何在内置的基础上构建更强的可靠性给出了一些建议。本章最后讨论了“死信办公室（`Dead Letter Office`）”的作用。

### 3.8.1. 一般规则

这些是发送消息的规则（即，`tell`或`!`方法，也是`ask`模式的基础）：

- 至多一次传递（`at-most-once delivery`），即没有保证的传递
- 每个`sender–receiver`对（`pair`）的消息排序

第一个规则通常也存在于其他 Actor 的实现中，而第二个规则则特定于 Akka。

#### 3.8.1.1. 讨论：“至多一次”是什么意思？

在描述传递机制的语义时，有三个基本类别：

- 至多一次传递（`at-most-once delivery`）意味着对于传递给该机制的每个消息，该消息要么传递一次，要么根本不传递；更随意地说，它意味着消息可能会丢失。
- 至少一次传递（`at-least-once delivery`）意味着对于传递给该机制的每个消息，可能会多次尝试传递它，从而至少一次成功；同样，在更随意的情况下，这意味着消息可能重复，但不会丢失。
- 精确一次传递（`exactly-once delivery`）意味着对于传递给该机制的每个消息，只向接收者传递一次；消息既不能丢失也不能重复。

第一种是最廉价和高效的，而且拥有最低的实现开销，因为它可以在发送端或传输机制中以不保持状态的情况下以“即发即弃（`fire-and-forget`）”的方式完成。第二种需要重试以应对传输损失，这意味着在发送端保持状态，在接收端具有确认机制。第三种是最昂贵的，因此性能最差，因为除了第二种之外，它还要求状态保持在接收端，以便过滤出重复的传递。

#### 3.8.1.2. 讨论：为什么不保证传递？

问题的核心在于这个保证到底意味着什么：

1. 消息在网络上发送？
2. 消息是由另一个主机接收的？
3. 消息被放入目标 Actor 的邮箱？
4. 目标 Actor 正在开始处理消息？
5. 目标 Actor 是否成功处理了消息？

其中每一个都有不同的挑战和成本，很明显，在某些条件下，任何邮件传递库都将无法遵守；例如，考虑可配置的邮箱类型以及绑定邮箱如何与第三点交互，甚至第五点考虑决定“成功”部分的意义。

同样的道理是，「[没有人需要可靠的消息传递](https://www.infoq.com/articles/no-reliable-messaging)」。发送方了解交互是否成功的唯一有意义的方法是接收业务的确认消息，这不是 Akka 可以自己完成的，我们既不编写“按我的意思做”的框架，也不希望这样做。

Akka 采用分布式计算，并通过消息传递使通信的易出错性变得明确，因此它不会试图撒谎并模拟泄漏的抽象。这是一个在 Erlang 成功使用的模型，需要用户围绕它设计自己的应用程序。你可以在「[Erlang 文档](http://erlang.org/faq/academic.html)」的第 10.9 节和第 10.10 节中了解更多关于这种方法的信息，Akka 将密切关注它。

关于这个问题的另一个角度是，只提供基本的保证，那些不需要更高可靠性的用例不需要支付它们的实现成本；总是可以在基本用例之上添加更高的可靠性，但是不可能为了获得更多的性能而主动地删除可靠性。

#### 3.8.1.3. 讨论：消息排序

更具体的规则是，对于给定的一对 Actor，直接从第一个 Actor 发送到第二个 Actor 的消息不会被无序接收。该词直接强调，该保证仅适用于与`tell`运算符一起发送到最终目的地时，而不适用于使用中介或其他消息分发功能时，除非另有说明。

保证说明如下：

- Actor `A1`向`A2`发送消息`M1`、`M2`、`M3`
- Actor `A3`向`A2`发送消息`M4`、`M5`、`M6`

这意味着：

1. 如果`M1`被接收，则必须在`M2`和`M3`之前接收。
2. 如果`M2`被接收，必须在`M3`之前接收。
3. 如果`M4`被接收，则必须在`M5`和`M6`之前接收。
4. 如果`M5`被接收，则必须在`M6`之前接收。
5. `A2`可以看到`A1`的消息与`A3`的消息交织在一起。
6. 由于没有保证的传递，任何信息都可能被丢弃，即不能到达`A2`。

在此，需要注意的是，Akka 的保证适用于邮件进入收件人邮箱的顺序。如果邮箱实现不遵循`FIFO`顺序（例如`PriorityMailbox`），那么 Actor 的处理顺序可能会偏离排队顺序。

请注意，此规则不可传递：

- Actor `A`将消息`M1`发送给 Actor `C`
- Actor `A`将消息`M2`发送给 Actor `B`
- Actor `B`将消息`M2`转发给 Actor `C`
- Actor `C`可以接受任何顺序的`M1`和`M2`

因果传递排序（`Causal transitive ordering`）意味着`M2`在`M1`之前从未在 Actor `C`收到过，尽管其中任何一个都可能丢失。当`A`、`B`和`C`驻留在不同的网络主机上时，由于不同的消息传递延迟，可能会违反此顺序，具体请参阅下面的详细信息。

- **注释**：Actor 创建被视为从父级发送到子级的消息，其语义与上面讨论的消息相同。以这种初始创建消息重新排序的方式向 Actor 发送消息意味着消息可能不会到达，因为 Actor 还不存在。消息可能来得太早的一个例子是，创建一个远程部署的 Actor `R1`，将其引用发送到另一个远程 Actor `R2`，并让`R2`向`R1`发送消息。定义良好的排序示例是父级创建 Actor 并立即向其发送消息。

#### 3.8.1.4. 通信故障

请注意，上面讨论的排序保证仅适用于 Actor 之间的用户消息。Actor 的子级的失败是通过特定的系统消息进行通信的，这些消息不是相对于普通用户消息进行排序的。特别地：

- 子 Actor `C`将消息`M`发送到其父 Actor `P`
- 子 Actor 因错误`F`导致失败
- 父 Actor `P`可能按`M`、`F`或`F`、`M`的顺序接收这两个事件

这样做的原因是内部系统消息有自己的邮箱，因此用户和系统消息的排队调用顺序不能保证其出列时间的顺序。

### 3.8.2. 在 JVM（本地）消息发送的规则
#### 3.8.2.1. 小心你对这部分的操作！

不建议依赖本节中更强的可靠性，因为它会将你的应用程序绑定到仅本地（`local-only`）部署：为了适合在计算机集群上运行，可能必须对应用程序进行不同的设计，而不是仅使用某些 Actor 本地的某些消息交换模式。我们的信条是“设计一次，以任何你想要的方式部署”，为了实现这一点，你应该只依赖于「[一般规则](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/message-delivery-reliability.md#%E4%B8%80%E8%88%AC%E8%A7%84%E5%88%99)」。

#### 3.8.2.2. 本地消息发送的可靠性

Akka 测试套件依赖于在本地上下文中不丢失消息（对于非错误条件测试也适用于远程部署），这意味着我们确实尽了最大努力保持测试的稳定性。但是，本地`tell`操作可能会失败，原因与在 JVM 上进行常规方法调用相同：

- `StackOverflowError`
- `OutOfMemoryError`
- 其它`VirtualMachineError`

此外，本地发送可能会以 Akka 特定的方式失败：

- 如果邮箱不接受邮件（例如，完全`BoundedMailbox`）
- 如果接收 Actor 在处理消息时失败或已终止

虽然第一个问题是配置问题，但第二个问题值得考虑：如果在处理过程中出现异常，则消息的发送者不会得到反馈，而是将该通知发送给监督者。对于外部观察者来说，这通常与丢失的消息没有区别。

#### 3.8.2.3. 本地消息发送顺序

假设严格的`FIFO`邮箱，在某些条件下，消除了消息排序保证的非传递性（`non-transitivity`）的上述警告。正如你将要注意到的，这些都是非常微妙的，而且将来的性能优化甚至有可能使整个段落（`paragraph`）失效。可能的非详尽的指示清单是：

- 在接收到顶级 Actor 的第一个回复之前，存在一个保护内部临时队列的锁，而这个锁是不公平的；这意味着，根据低级线程调度，来自不同发送方的排队请求在 Actor 的构造过程中到达，可以重新排序。因为在 JVM 上不存在完全公平的锁，所以这是不可修复的。
- 同样的机制在`Router`的构建过程中使用，更精确地说是路由的`ActorRef`，因此对于部署了路由器的 Actor 来说，同样的问题也存在。
- 如上所述，在排队过程中涉及锁的任何地方都会出现问题，这也可能适用于自定义邮箱。

虽然此列表我们已经仔细考虑过了，但仍然可能存在其他的我们没有想到的问题。

#### 3.8.2.4. 本地顺序与网络顺序有什么关系？

对于给定的一对 Actor，直接从第一个 Actor 发送到第二个 Actor 的消息将不会被无序接收，这一规则适用于使用基于 TCP 的 Akka 远程传输协议通过网络发送的消息。

如前一节所述，本地消息在特定条件下发送服从传递因果排序。由于不同的邮件传递延迟，可能会违反此顺序。例如：

- `node-1`上的 Actor `A`向`node-3`上的 Actor `C`发送消息`M1`
- `node-1`上的 Actor `A`然后向`node-2`上的 Actor `B`发送消息`M2`
- `node-2`上的 Actor `B`将消息`M2`转发给`node-3`上的 Actor `C`
- Actor `C`可以接受任何顺序的`M1`和`M2`

`M1`到`node-3`的“传输”时间可能比`M2`通过`node-2`到`node-3`的“传输”时间要长。

### 3.8.3. 高级抽象

基于 Akka 核心中的一个小而一致的工具集，Akka 还提供了强大的、更高级的抽象。

#### 3.8.3.1. 消息模式

如上所述，对可靠传递需求的直接回答是一个明确的`ACK-RETRY`协议。以最简单的形式，这需要

- 识别单个消息以将消息与确认关联的方法
- 一种重试机制，如果不及时确认，将重新发送消息
- 接收者检测和丢弃重复数据的一种方法

第三个是必要的，因为消息也不能保证到达。Akka 持久性模块的“至少一次传递”支持具有业务级确认的`ACK-RETRY`协议。通过跟踪通过"至少一次传递"发送的消息的标识符，可以检测到重复的消息。实现第三部分的另一种方法是使消息处理在业务逻辑级别上是等量的。

#### 3.8.3.2. 事件源

事件源（和分片）是大型网站扩展到数十亿用户的原因，其思想非常简单：当一个组件（思考 Actor）处理一个命令时，它将生成一个表示命令效果的事件列表。除了应用于组件的状态之外，还存储这些事件。这个方案的好处在于，事件只会被附加到存储中，不会发生任何变化；这样可以完美地复制和扩展这个事件流（`event stream`）的使用者（即，其他组件可能会使用事件流作为在不同区域复制组件状态或对更改作出反应的手段）。如果组件的状态由于机器故障或被推出缓存而丢失，则可以通过重放事件流（通常使用快照来加快进程）来重建。Akka 持久化支持「[事件源](https://github.com/guobinhit/akka-guide/blob/master/articles/actors/persistence.md#%E4%BA%8B%E4%BB%B6%E6%BA%90)」。

#### 3.8.3.3. 带明确确认的邮箱

通过实现自定义邮箱类型，可以在接收 Actor 端重试消息处理，以处理临时故障。此模式在本地通信上下文中最有用，因为在本地通信上下文中，传递保证在其他方面足以满足应用程序的需求。

请注意，对于「[在 JVM（本地）消息发送的规则](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/message-delivery-reliability.md#%E5%9C%A8-jvm%E6%9C%AC%E5%9C%B0%E6%B6%88%E6%81%AF%E5%8F%91%E9%80%81%E7%9A%84%E8%A7%84%E5%88%99)」的警告确实适用。

### 3.8.4. 死信

无法传递（并且可以确定）的消息将传递给称为`/deadLetters`的虚拟 Actor。这种传递是在尽最大努力的基础上进行的；它甚至可能在本地 JVM 中失败（例如，在 Actor 终止期间）。通过不可靠的网络传输发送的消息将丢失，而不会显示为死信。

#### 3.8.4.1. 应该用死信做什么？

此工具的主要用途是调试，特别是当 Actor 发送的邮件不一致时（通常检查死信会告诉你发送者或收件者在某个地方设置错误）。为了有助于实现这一目的，最好避免在可能的情况下发送死信，也就是说，使用合适的死信记录器不时的运行应用程序，并清除日志输出。与所有其他方法一样，这个练习需要明智地应用常识：很可能避免发送到终止的 Actor 会使发送者的代码比调试输出的清晰性更差。

死信服务在传递保证方面遵循与所有其他消息发送相同的规则，因此不能用于实现保证传递。

#### 3.8.4.2. 如何收到死信？

Actor 可以订阅事件流上的类`akka.actor.DeadLetter`，请参阅「[事件流](https://github.com/guobinhit/akka-guide/blob/master/articles/index-utilities/event-bus.md#%E4%BA%8B%E4%BB%B6%E6%B5%81)」了解如何执行该操作。然后，订阅的 Actor 将收到（本地）系统中从那时起发布的所有死信。死信不会在网络上传播，如果要在一个位置收集死信，则必须为每个网络节点订阅一个 Actor，然后手动转发它们。还要考虑在该节点上生成死信，它可以确定发送操作失败，对于远程发送，死信可以是本地系统（如果无法建立网络连接）或远程系统（如果你要发送到的 Actor 在该时间点不存在）。

#### 3.8.4.3. 通常不令人担忧的死信

每当一个 Actor 不因自己的决定而终止时，它发送给自己的一些消息就有可能丢失。在通常是良性的复杂关闭场景中，有一种情况很容易发生：看到`akka.dispatch.Terminate`消息丢失意味着给出了两个终止请求，但只有一个可以成功。同样，你可能会看到`akka.actor.Terminated`来自子 Actor 的消息，而如果父级 Actor 在父级终止时仍在监视子 Actor，则会阻止一系列以死信形式出现的 Actor。


----------

**英文原文链接**：[Message Delivery Reliability](https://doc.akka.io/docs/akka/current/general/message-delivery-reliability.html).


## 3.9. 配置
你可以在不定义任何配置的情况下开始使用 Akka，因为提供了合理的默认值。稍后，你可能需要修改设置以更改默认行为或适应特定的运行时环境。你可以修改的典型设置示例：

- 日志级别和日志记录器后端
- 启用远程处理
- 消息序列化程序
- 路由器的定义
- 调度员调整

Akka 使用「[Typesafe Config Library](https://github.com/lightbend/config)」，这对于配置你自己的应用程序或使用或不使用 Akka 构建的库也是一个不错的选择。这个库是用 Java 实现的，没有外部依赖关系；你应该看看它的文档，特别是关于「[ConfigFactory](https://lightbend.github.io/config/latest/api/com/typesafe/config/ConfigFactory.html)」。

- **警告**：如果你使用来自`2.9.x`系列的 Scala REPL 的 Akka，并且没有向`ActorSystem`提供自己的`ClassLoader`，那么需要使用`-Yrepl-sync`启动 REPL，以解决 REPLs 提供的上下文类加载器中的缺陷。

### 3.9.1. 从哪里读取配置？
Akka 的所有配置（`configuration`）都保存在`ActorSystem`实例中，或者换句话说，从外部看，`ActorSystem`是配置信息的唯一使用者。在构造 Actor 系统时，可以传入`Config`对象，也可以不传入，其中第二种情况等同于传递`ConfigFactory.load()`，使用正确的类加载器。这大致意味着默认值是解析类路径根目录下的所有`application.conf`、`application.json`和`application.properties`文件。有关详细信息，请参阅上述文档。然后，Actor 系统合并在类路径根目录下找到的所有`reference.conf`资源，以形成最终的配置，以供内部使用。

```java
appConfig.withFallback(ConfigFactory.defaultReference(classLoader))
```
其原理是代码从不包含默认值，而是依赖于它们在相关库提供的`reference.conf`中的存在。

作为系统属性给出的覆盖具有最高优先级，请参阅「[HOCON](https://github.com/lightbend/config/blob/master/HOCON.md)」规范（靠近底部）。另外值得注意的是，默认为应用程序的应用程序配置可以使用`config.resource`属性覆盖，还有更多内容，请参阅「[Config](https://github.com/lightbend/config/blob/master/README.md)」文档。

- **注释**：如果你正在编写 Akka 应用程序，请将你的配置保存在类路径根目录下的`application.conf`中。如果你正在编写基于 Akka 的库，请将其配置保存在 JAR 文件根目录下的`reference.conf`中。

### 3.9.2. 使用 JarJar、OneJar、Assembly 或任何  jar-bundler 时

- **警告**：Akka 的配置方法很大程度上依赖于每个`module/jar`都有自己的`reference.conf`文件的概念，所有这些都将由配��发现并加载。不幸的是，这也意味着如果你将多个 Jar 放入或合并到同一个 Jar 中，那么你还需要合并所有`reference.conf`。否则，所有默认值将丢失，Akka ��不起作用。

如果使用 Maven 打包应用程序，还可以使用「[Apache Maven Shade Plugin](http://maven.apache.org/plugins/maven-shade-plugin/)」的「[Resource Transformers](http://maven.apache.org/plugins/maven-shade-plugin/examples/resource-transformers.html#AppendingTransformer)」，其支持将构建类路径上的所有`reference.conf`合并为一个。

插件配置可能如下所示：

```xml
<plugin>
 <groupId>org.apache.maven.plugins</groupId>
 <artifactId>maven-shade-plugin</artifactId>
 <version>1.5</version>
 <executions>
  <execution>
   <phase>package</phase>
   <goals>
    <goal>shade</goal>
   </goals>
   <configuration>
    <shadedArtifactAttached>true</shadedArtifactAttached>
    <shadedClassifierName>allinone</shadedClassifierName>
    <artifactSet>
     <includes>
      <include>*:*</include>
     </includes>
    </artifactSet>
    <transformers>
      <transformer
       implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
       <resource>reference.conf</resource>
      </transformer>
      <transformer
       implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
       <manifestEntries>
        <Main-Class>akka.Main</Main-Class>
       </manifestEntries>
      </transformer>
    </transformers>
   </configuration>
  </execution>
 </executions>
</plugin>
```
### 3.9.3. 自定义 application.conf
一个自定义的`application.conf`配置可能如下所示：

```
## In this file you can override any option defined in the reference files.
## Copy in parts of the reference files and modify as you please.

akka {

  ## Loggers to register at boot time (akka.event.Logging$DefaultLogger logs
  ## to STDOUT)
  loggers = ["akka.event.slf4j.Slf4jLogger"]

  ## Log level used by the configured loggers (see "loggers") as soon
  ## as they have been started; before that, see "stdout-loglevel"
  ## Options: OFF, ERROR, WARNING, INFO, DEBUG
  loglevel = "DEBUG"

  ## Log level for the very basic logger activated during ActorSystem startup.
  ## This logger prints the log messages to stdout (System.out).
  ## Options: OFF, ERROR, WARNING, INFO, DEBUG
  stdout-loglevel = "DEBUG"

  ## Filter of log events that is used by the LoggingAdapter before
  ## publishing log events to the eventStream.
  logging-filter = "akka.event.slf4j.Slf4jLoggingFilter"

  actor {
    provider = "cluster"

    default-dispatcher {
      ## Throughput for default Dispatcher, set to 1 for as fair as possible
      throughput = 10
    }
  }

  remote {
    ## The port clients should connect to. Default is 2552.
    netty.tcp.port = 4711
  }
}
```
### 3.9.4. 包含文件
有时，包含另一个配置文件可能很有用，例如，如果你有一个`application.conf`，具有所有与环境无关的设置，然后覆盖特定环境的某些设置。

使用`-Dconfig.resource=/dev.conf`指定系统属性将加载`dev.conf`文件，其中包括`application.conf`

- **dev.conf**

```
include "application"

akka {
  loglevel = "DEBUG"
}
```
在 HOCON 规范中解释了更高级的包含和替换机制。

### 3.9.5. 配置日志记录
如果系统或配置属性`akka.log-config-on-start`设置为`on`，那么当 Actor 系统启动时，将在`INFO`级别记录完整配置。当你不确定使用了什么配置时，这很有用。

如果有疑问，你可以在使用配置对象构建 Actor 系统之前或之后检查它们：

```
Welcome to Scala 2.12 (Java HotSpot(TM) 64-Bit Server VM, Java 1.8.0).
Type in expressions to have them evaluated.
Type :help for more information.

scala> import com.typesafe.config._
import com.typesafe.config._

scala> ConfigFactory.parseString("a.b=12")
res0: com.typesafe.config.Config = Config(SimpleConfigObject({"a" : {"b" : 12}}))

scala> res0.root.render
res1: java.lang.String =
{
    ## String: 1
    "a" : {
        ## String: 1
        "b" : 12
    }
}
```

每个项目前面的注释给出了有关设置起点（文件和行号）的详细信息以及可能出现的注释，例如在参考配置中。合并引用和 Actor 系统的设置如下所示：

```java
final ActorSystem system = ActorSystem.create();
System.out.println(system.settings());
// this is a shortcut for system.settings().config().root().render()
```
### 3.9.6. 关于类加载器的一句话
在配置文件的几个地方，可以指定由 Akka 实例化的某个对象的完全限定类名。这是使用 Java 反射完成的，Java 反射又使用类加载器。在应用程序容器或 OSGi 包等具有挑战性的环境中获得正确的方法并不总是很简单的，Akka 的当前方法是，每个`ActorSystem`实现存储当前线程的上下文类加载器（如果可用，否则只存储其自己的加载器，如`this.getClass.getClassLoader`）并将其用于所有反射访问。这意味着将 Akka 放在引导类路径上会从奇怪的地方产生`NullPointerException`：这是不支持的。

### 3.9.7. 应用程序特定设置
配置也可用于特定于应用程序的设置。一个好的做法是将这些设置放在「[扩展](https://github.com/guobinhit/akka-guide/blob/master/articles/index-utilities/extending-akka.md#%E5%BA%94%E7%94%A8%E7%A8%8B%E5%BA%8F%E7%89%B9%E5%AE%9A%E8%AE%BE%E7%BD%AE)」中。

#### 3.9.7.1. 配置多个 ActorSystem
如果你有多个`ActorSystem`（或者你正在编写一个库，并且有一个`ActorSystem`可能与应用程序的`ActorSystem`分离），那么你可能需要分离每个系统的配置。

考虑到`ConfigFactory.load()`从整个类路径中合并所有具有匹配名称的资源，利用该功能区分配置层次结构中的 Actor 系统是最容易：

```
myapp1 {
  akka.loglevel = "WARNING"
  my.own.setting = 43
}
myapp2 {
  akka.loglevel = "ERROR"
  app2.setting = "appname"
}
my.own.setting = 42
my.other.setting = "hello"
```

```java
val config = ConfigFactory.load()
val app1 = ActorSystem("MyApp1", config.getConfig("myapp1").withFallback(config))
val app2 = ActorSystem("MyApp2",
  config.getConfig("myapp2").withOnlyPath("akka").withFallback(config))
```

这两个示例演示了“提升子树（`lift-a-subtree`）”技巧的不同变化：在第一种情况下，从 Actor 系统中访问的配置是

```
akka.loglevel = "WARNING"
my.own.setting = 43
my.other.setting = "hello"
// plus myapp1 and myapp2 subtrees
```
在第二种情况下，只提升“akka”子树，结果如下

```
akka.loglevel = "ERROR"
my.own.setting = 42
my.other.setting = "hello"
// plus myapp1 and myapp2 subtrees
```
- **注释**：配置库非常强大，说明其所有功能超出了本文的范围。尤其不包括如何将其他配置文件包含在其他文件中（参见「[包含文件](https://github.com/guobinhit/akka-guide/blob/master/articles/general-concepts/configuration.md#%E5%8C%85%E5%90%AB%E6%96%87%E4%BB%B6)」中的一个小示例）以及通过路径替换复制配置树的部分。

在实例化`ActorSystem`时，还可以通过其他方式以编程方式指定和分析配置。

```java
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
val customConf = ConfigFactory.parseString("""
  akka.actor.deployment {
    /my-service {
      router = round-robin-pool
      nr-of-instances = 3
    }
  }
  """)
// ConfigFactory.load sandwiches customConfig between default reference
// config and default overrides, and then resolves it.
val system = ActorSystem("MySystem", ConfigFactory.load(customConf))
```

### 3.9.8. 从自定义位置读取配置
你可以在代码或使用系统属性中替换或补充`application.conf`。

如果你使用的是`ConfigFactory.load()`（默认情况下 Akka 会这样做），那么可以通过定义`-Dconfig.resource=whatever`、`-Dconfig.file=whatever`或`-Dconfig.url=whatever`来替换`application.conf`。

在用`-Dconfig.resource`和`friends`指定的替换文件中，如果你仍然想使用`application.{conf,json,properties}`，可以使用`include "application"`。在`include "application"`之前指定的设置将被包含的文件覆盖，而在`include "application"`之后指定的设置将覆盖包含的文件。

在代码中，有许多自定义选项。

`ConfigFactory.load()`有几个重载；这些重载允许你指定一些夹在系统属性（重写）和默认值（来自`reference.conf`）之间的内容，替换常规`application.{conf,json,properties}`并替换` -Dconfig.file`和`friends`。

`ConfigFactory.load()`的最简单变体采用资源基名称（`resource basename`），而不是应用程序；然后将使用`myname.conf`、`myname.json`和`myname.properties`而不是`application.{conf,json,properties}`。

最灵活的变体采用`Config`对象，你可以使用`ConfigFactory`中的任何方法加载该对象。例如，你可以使用`ConfigFactory.parseString()`在代码中放置配置字符串，也可以制作映射和`ConfigFactory.parseMap()`，或者加载文件。

你还可以将自定义配置与常规配置结合起来，这可能看起来像：

```java
// make a Config with just your special setting
Config myConfig = ConfigFactory.parseString("something=somethingElse");
// load the normal config stack (system props,
// then application.conf, then reference.conf)
Config regularConfig = ConfigFactory.load();
// override regular stack with myConfig
Config combined = myConfig.withFallback(regularConfig);
// put the result in between the overrides
// (system props) and defaults again
Config complete = ConfigFactory.load(combined);
// create ActorSystem
ActorSystem system = ActorSystem.create("myname", complete);
```
使用`Config`对象时，请记住蛋糕中有三个“层”：

- `ConfigFactory.defaultOverrides()`（系统属性）
- 应用程序的设置
- `ConfigFactory.defaultReference()`（`reference.conf`）

通常的目标是定制中间层，而不让其他两层单独使用。

- `ConfigFactory.load()`加载整个堆栈
- `ConfigFactory.load()`的重载允许你指定不同的中间层
- `ConfigFactory.parse()`变体加载单个文件或资源

要堆叠两层，请使用`override.withFallback(fallback)`；尝试将系统属性（`defaultOverrides()`）保持在顶部，并将`reference.conf`（`defaultReference()`）保持在底部。

请记住，你通常可以在`application.conf`中添加另一个`include`语句，而不是编写代码。`application.conf`顶部的`include`将被`application.conf`的其余部分覆盖，而底部的`include`将覆盖前面的内容。

### 3.9.9. Actor 部署配置
特定 Actor 的部署设置可以在配置的`akka.actor.deployment`部分中定义。在部署部分，可以定义调度程序、邮箱、路由设置和远程部署等内容。这些功能的配置在详细介绍相应主题的章节中进行了描述。示例如下：

```
akka.actor.deployment {

  ## '/user/actorA/actorB' is a remote deployed actor
  /actorA/actorB {
    remote = "akka.tcp://sampleActorSystem@127.0.0.1:2553"
  }
  
  ## all direct children of '/user/actorC' have a dedicated dispatcher 
  "/actorC/*" {
    dispatcher = my-dispatcher
  }

  ## all descendants of '/user/actorC' (direct children, and their children recursively)
  ## have a dedicated dispatcher
  "/actorC/**" {
    dispatcher = my-dispatcher
  }
  
  ## '/user/actorD/actorE' has a special priority mailbox
  /actorD/actorE {
    mailbox = prio-mailbox
  }
  
  ## '/user/actorF/actorG/actorH' is a random pool
  /actorF/actorG/actorH {
    router = random-pool
    nr-of-instances = 5
  }
}

my-dispatcher {
  fork-join-executor.parallelism-min = 10
  fork-join-executor.parallelism-max = 10
}
prio-mailbox {
  mailbox-type = "a.b.MyPrioMailbox"
}
```
- **注释**：特定 Actor 的部署部分由 Actor 相对于`/user`的路径标识。

可以使用星号作为 Actor 路径部分的通配符匹配，因此可以指定：`/*/sampleActor`，它将匹配层次结构中该级别上的所有`sampleActor`。此外，请注意：

- 可以在最后一个位置使用通配符来匹配特定级别的所有 Actor：`/someparent/*`
- 可以在最后一个位置使用双通配符以递归方式匹配所有子 Actor 及其子参 Actor：`/someparent/**`
- 非通配符匹配总是比通配符具有更高的匹配优先级，并且单个通配符匹配比双通配符具有更高的优先级，因此：`/foo/bar`被认为比`/foo/*`更具体，后者被认为比`/foo/**`更具体，仅使用最高优先级匹配。
- 通配符不能用于部分匹配，如`/foo*/bar`、`/f*o/bar`等。
- 双通配符只能放在最后一个位置。

### 3.9.10. 参考配置列表
每个 Akka 模块都有一个带有默认值的参考配置文件。

- [akka-actor](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-actor)
- [akka-agent](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-agent)
- [akka-camel](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-camel)
- [akka-cluster](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-cluster)
- [akka-multi-node-testkit](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-multi-node-testkit)
- [akka-persistence](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-persistence)
- [akka-remote](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-remote)
- [akka-remote (artery)](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-remote-artery-)
- [akka-testkit](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-testkit)
- [akka-cluster-metrics](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-cluster-metrics)
- [akka-cluster-tools](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-cluster-tools)
- [akka-cluster-sharding](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-cluster-sharding)
- [akka-distributed-data](https://doc.akka.io/docs/akka/current/general/configuration.html#akka-distributed-data)


----------

**英文原文链接**：[Configuration](https://doc.akka.io/docs/akka/current/general/configuration.html).


# 4. Actors

>核心Akka库是Akka-actor-typed，但是actors是跨Akka库使用的，提供了一致的、集成的模型，使您不必单独解决并发或分布式系统设计中出现的挑战。从表面上看，actor是一种采用封装(OOP的支柱之一)的编程范式，它达到了极限。与对象不同，actor不仅封装了它们的状态，而且封装了它们的执行。与Actor的通信不是通过方法调用，而是通过传递消息。虽然这种差异看起来很小，但它实际上使我们在并发性和远程通信方面摆脱了OOP的限制。如果您觉得这个描述过于深奥，还不能完全理解的话，请不要担心，在下一章我们将详细解释actor。目前，重要的一点是，这是一个在基础级别处理并发和分发的模型，而不是通过临时修补尝试将这些特性引入OOP。

> Actor 解决的挑战包括以下几个方面:
> * 如何构建和设计高性能并发应用程序。
> * 如何在多线程环境中处理错误。
> * 如何保护我的项目不受并发陷阱的影响。

## 4.1. Actors 基础

### 4.1.1. Actor Maven依赖库

```xml
<properties>
  <akka.version>2.6.8</akka.version>
  <scala.binary.version>2.13</scala.binary.version>
</properties>
<dependency>
  <groupId>com.typesafe.akka</groupId>
  <artifactId>akka-actor-typed_${scala.binary.version}</artifactId>
  <version>${akka.version}</version>
</dependency>
```

### 4.1.2. Actor 简介

使用Akka使您不必为actor系统创建基础设施，也不必编写控制基本行为所需的底层代码。为了理解这一点，让我们看看您在代码中创建的Actor与Akka在内部为您创建和管理的Actor之间的关系，Actor生命周期和失败处理。

### 4.1.3. Actor 层次结构

在Akka中Actor永远属于父Actor。您可以通过调用ActorContext.spawn()来创建一个actor。创建者Actor成为新创建的子Actor的父Actor。您可能会问，您创建的第一个actor的父元素是谁?

如下所示，所有Actors都有一个共同的父Actor，即用户监护人，它是在启动ActorSystem时定义和创建的。正如我们在《快速入门指南》中提到的，actor的创建将返回一个引用，该引用是一个有效的URL。例如，如果我们从用户guardian中使用`context.spawn(someBehavior, "someActor")`创建一个名为someActor的actor，其引用将包括路径`/user/someActor`。

![20200802224604](https://liulv.work/images/img/20200802224604.png)

实际上，在启动第一个Actor之前，Akka已经在系统中创建了两个Actor。这些内置Actor的名称包含监护人(guardian)。监控Actor包括:

* / 所谓的根监护者（guardian）- 它是系统中所有角色的父级，也是系统本身终止时最后一个停止的角色。

* /system 系统的监护者 - Akka或构建在Akka之上的其他库可以在系统名称空间中创建Actor。

* /user 用户的监护者 - 这是您为启动应用程序中的所有其他Actor而提供的顶级Actor。

查看actor层次结构的最简单方法是打印ActorRef实例。在这个小实验中，我们创建一个actor，打印它的引用，创建这个actor的子元素，然后打印子元素的引用。我们从Hello World项目开始，如果您还没有下载它，请从Lightbend技术中心下载Quickstart项目。

在Hello World项目中，导航到`com.tcfuture.akka.actor.architecture`示例包，并为下面代码片段中的每个类创建一个Java文件，并复制各自的内容。保存文件并运行`com.tcfuture.akka.actor.example.hello.PrintMyActorRefActor.ActorHierarchyExperiments`从构建工具或IDE中观察输出。

```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

class PrintMyActorRefActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(PrintMyActorRefActor::new);
  }

  private PrintMyActorRefActor(ActorContext<String> context) {
    super(context);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("printit", this::printIt).build();
  }

  private Behavior<String> printIt() {
    ActorRef<String> secondRef = getContext().spawn(Behaviors.empty(), "second-actor");
    System.out.println("Second: " + secondRef);
    return this;
  }
}

class Main extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(Main::new);
  }

  private Main(ActorContext<String> context) {
    super(context);
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("start", this::start).build();
  }

  private Behavior<String> start() {
    ActorRef<String> firstRef = getContext().spawn(PrintMyActorRefActor.create(), "first-actor");

    System.out.println("First: " + firstRef);
    firstRef.tell("printit");
    return Behaviors.same();
  }
}

public class ActorHierarchyExperiments {
  public static void main(String[] args) {
    ActorRef<String> testSystem = ActorSystem.create(Main.create(), "testSystem");
    testSystem.tell("start");
  }
}
```

请注意消息要求第一个actor执行其工作的方式。我们使用父节点的引用发送消息:`firstRef.tell("printit", ActorRef.noSender())`。当代码执行时，输出包括对第一个actor的引用以及它作为printit大小写的一部分创建的子元素。您的输出应该类似于以下内容:

```log
First: Actor[akka://testSystem/user/first-actor#1053618476]
Second: Actor[akka://testSystem/user/first-actor/second-actor#-1544706041]
```

输出打印如下：

![20200802230207](https://liulv.work/images/img/20200802230207.png)

注意引用的结构:

* 两个路径都从`akka://testSystem/`开始。因为所有的actor引用都是有效的url，所以akka://是协议字段的值。

* 接下来，就像在万维网上一样，URL标识系统。在本例中，系统被命名为testSystem，但它可以是任何其他名称。如果启用了多个系统之间的远程通信，URL的这一部分将包含主机名，以便其他系统可以在网络上找到它。

* 因为第二个Actor的引用包含路径`/first-actor/`，所以它将其标识为第一个Actor的子元素。

* actor引用的最后一部分#1053618476或#-1544706041是一个在大多数情况下可以忽略的惟一标识符。

现在您已经了解了actor层次结构的样子，您可能想知道:为什么我们需要这个层次结构?它是用来做什么的?

层次结构的一个重要角色是安全地管理Actor的生命周期。接下来让我们考虑这个问题，看看这些知识如何帮助我们编写更好的代码。

### 4.1.4. Actor 生命周期

Actor在创建时出现，然后在用户请求时停止。当一个Actor被停止时，它的所有子Actor也会被递归地停止。此行为极大地简化了资源清理工作，并有助于避免资源泄漏，比如由于打开套接字和文件而导致的泄漏。事实上，在处理低级多线程代码时，一个经常被忽视的困难是各种并发资源的生命周期管理。

为了停止一个Actor，推荐的模式是在actor内部返回Behaviors.stopped()来停止它自己，通常作为对一些用户定义的停止消息的响应，或者当actor完成它的工作时。从父节点调用context.stop(childRef)来停止子节点从技术上讲是可行的，但是不可能通过这种方式停止任意的(非子节点)节点。

Akka actor API公开了一些生命周期信号，例如PostStop是在actor停止之后发送的。在此之后不处理任何消息。

让我们在一个简单的实验中使用PostStop生命周期信号来观察停止Actor时的行为。首先，在您的项目中添加以下2个actor类:

```java
class StartStopActor1 extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(StartStopActor1::new);
  }

  private StartStopActor1(ActorContext<String> context) {
    super(context);
    System.out.println("first started");

    context.spawn(StartStopActor2.create(), "second");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
        .onMessageEquals("stop", Behaviors::stopped)
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<String> onPostStop() {
    System.out.println("first stopped");
    return this;
  }
}

class StartStopActor2 extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(StartStopActor2::new);
  }

  private StartStopActor2(ActorContext<String> context) {
    super(context);
    System.out.println("second started");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onSignal(PostStop.class, signal -> onPostStop()).build();
  }

  private Behavior<String> onPostStop() {
    System.out.println("second stopped");
    return this;
  }
}
```

创建一个像上面那样的“main”类启动actor，然后发送一个“stop”消息:

```java
ActorRef<String> first = context.spawn(StartStopActor1.create(), "first");
first.tell("stop");
```

运行结果如下：

```
first started
second started
second stopped
first stopped
```

![20200803002422](https://cdn.jsdelivr.net/gh/v4liulv/PicPed/img/20200803002422.png)

当我们先停止actor时，它会先停止它的child actor，然后再停止它自己。这种顺序是严格的，子节点的所有后停止信号都在父节点的后停止信号处理之前处理。

### 4.1.5. Actor 故障处理

父Actor和子Actor在整个生命周期中都是相互联系的。每当一个Actor失败时(抛出一个异常或一个未处理的异常从接收中冒出)，失败信息就会传播到监督策略，然后由其决定如何处理由Actor引起的异常。监控策略通常由父actor在生成子actor时定义。这样，父母就成了孩子的监督者。默认的管理策略是阻止孩子。如果你不定义策略，所有的失败都会导致停止。

让我们通过一个简单的实验来观察重启监管策略。添加以下类到你的项目，就像你做的前面的:

```java
class SupervisingActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(SupervisingActor::new);
  }

  private final ActorRef<String> child;

  private SupervisingActor(ActorContext<String> context) {
    super(context);
    child =
        context.spawn(
            Behaviors.supervise(SupervisedActor.create()).onFailure(SupervisorStrategy.restart()),
            "supervised-actor");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder().onMessageEquals("failChild", this::onFailChild).build();
  }

  private Behavior<String> onFailChild() {
    child.tell("fail");
    return this;
  }
}

class SupervisedActor extends AbstractBehavior<String> {

  static Behavior<String> create() {
    return Behaviors.setup(SupervisedActor::new);
  }

  private SupervisedActor(ActorContext<String> context) {
    super(context);
    System.out.println("supervised actor started");
  }

  @Override
  public Receive<String> createReceive() {
    return newReceiveBuilder()
        .onMessageEquals("fail", this::fail)
        .onSignal(PreRestart.class, signal -> preRestart())
        .onSignal(PostStop.class, signal -> postStop())
        .build();
  }

  private Behavior<String> fail() {
    System.out.println("supervised actor fails now");
    throw new RuntimeException("I failed!");
  }

  private Behavior<String> preRestart() {
    System.out.println("second will be restarted");
    return this;
  }

  private Behavior<String> postStop() {
    System.out.println("second stopped");
    return this;
  }
}
```

并运行：

```java
ActorRef<String> supervisingActor =
    context.spawn(SupervisingActor.create(), "supervising-actor");
supervisingActor.tell("failChild");
```

您应该会看到类似如下的输出:

```
First: Actor[akka://ActorSystem/user/supervising-actor#637280321]
supervised actor started
supervised actor fails now
second will be restarted
[2020-08-03 00:29:08,938] [ERROR] [akka.actor.typed.Behavior$] [ActorSystem-akka.actor.default-dispatcher-3] [akka://ActorSystem/user/supervising-actor/supervised-actor] - Supervisor RestartSupervisor saw failure: I failed!
java.lang.RuntimeException: I failed!
	at com.tcfuture.akka.actor.architecture.SupervisedActor.fail(FailureHandling.java:103)
	at akka.actor.typed.javadsl.ReceiveBuilder$$anon$2.apply(ReceiveBuilder.scala:88)
	at akka.actor.typed.javadsl.ReceiveBuilder$$anon$2.apply(ReceiveBuilder.scala:86)
	at akka.actor.typed.javadsl.BuiltReceive.receive(ReceiveBuilder.scala:213)
	at akka.actor.typed.javadsl.BuiltReceive.receiveMessage(ReceiveBuilder.scala:204)
	at akka.actor.typed.javadsl.Receive.receive(Receive.scala:53)
	at akka.actor.typed.javadsl.AbstractBehavior.receive(AbstractBehavior.scala:64)
	at akka.actor.typed.Behavior$.interpret(Behavior.scala:274)
	at akka.actor.typed.Behavior$.interpretMessage(Behavior.scala:230)
	at akka.actor.typed.internal.InterceptorImpl$$anon$2.apply(InterceptorImpl.scala:57)
	at akka.actor.typed.internal.RestartSupervisor.aroundReceive(Supervision.scala:263)
	at akka.actor.typed.internal.InterceptorImpl.receive(InterceptorImpl.scala:85)
	at akka.actor.typed.Behavior$.interpret(Behavior.scala:274)
	at akka.actor.typed.Behavior$.interpretMessage(Behavior.scala:230)
	at akka.actor.typed.internal.adapter.ActorAdapter.handleMessage(ActorAdapter.scala:129)
	at akka.actor.typed.internal.adapter.ActorAdapter.aroundReceive(ActorAdapter.scala:106)
	at akka.actor.ActorCell.receiveMessage(ActorCell.scala:577)
	at akka.actor.ActorCell.invoke(ActorCell.scala:547)
	at akka.dispatch.Mailbox.processMailbox(Mailbox.scala:270)
	at akka.dispatch.Mailbox.run(Mailbox.scala:231)
	at akka.dispatch.Mailbox.exec(Mailbox.scala:243)
	at java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:289)
	at java.util.concurrent.ForkJoinPool$WorkQueue.runTask(ForkJoinPool.java:1056)
	at java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1692)
	at java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:157)
supervised actor started
```

我们看到，在失败后，被监督的Actor被停止并立即重新启动。我们还会看到一个日志条目报告所处理的异常，在本例中是我们的测试异常。在本例中，我们还使用了PreRestart信号，该信号在重启之前被处理。


## 4.2. 介绍 Actors

### 4.2.1. Akka Actors 

Actor[模型](https://en.wikipedia.org/wiki/Actor_model)为编写并发和分布式系统提供了更高层次的抽象。它减轻了开发人员必须处理显式锁和线程管理的负担，使编写正确的并发和并行系统变得更加容易。actor是Carl Hewitt在1973年的论文中定义的，但由于Erlang语言的使用而得到推广，例如在Ericsson中就成功地用于构建高度并发和可靠的电信系统。Akka的actor的API借用了Erlang的一些语法。

### 4.2.2. 第一个示例

在入门示例中[HelloWordAkka](#22-HelloWordAkka)已经存在完整的示例。

如果你是Akka的新成员，你可能想从[入门指南](https://doc.akka.io/docs/akka/current/typed/guide/introduction.html)开始，然后回来这里学习更多。我们也推荐观看[Akka的简短介绍视频](https://akka.io/blog/news/2019/12/03/akka-typed-actor-intro-video?_ga=2.110322695.388717210.1596435693-1352056857.1596435693)。

熟悉Actor的基本的、外部的和内部的生态系统是很有帮助的，看看您可以根据需要利用和定制什么，查看 [Actor System](#32-Actor系统)和 [Actor引用、路径和地址](#35-Actor引用、路径和地址)。


正如在[Actor Systems](#32-Actor系统) 中所讨论的，Actor是关于在独立的计算单元之间发送消息的，但是这看起来像什么呢?

在以下所有这些导入都是假设的:

```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
```

有了这些，我们就可以定义第一个Actor，它会说hello!

![20200805105658](https://liulv.work/images/img/20200805105658.png)

```java
public class HelloWorld extends AbstractBehavior<HelloWorld.Greet> {

  public static final class Greet {
    public final String whom;
    public final ActorRef<Greeted> replyTo;

    public Greet(String whom, ActorRef<Greeted> replyTo) {
      this.whom = whom;
      this.replyTo = replyTo;
    }
  }

  public static final class Greeted {
    public final String whom;
    public final ActorRef<Greet> from;

    public Greeted(String whom, ActorRef<Greet> from) {
      this.whom = whom;
      this.from = from;
    }
  }

  public static Behavior<Greet> create() {
    return Behaviors.setup(HelloWorld::new);
  }

  private HelloWorld(ActorContext<Greet> context) {
    super(context);
  }

  @Override
  public Receive<Greet> createReceive() {
    return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
  }

  private Behavior<Greet> onGreet(Greet command) {
    getContext().getLog().info("Hello {}!", command.whom);
    command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
    return this;
  }
}
```

这一小段代码定义了两种消息类型，一种用于命令Actor向某人打招呼，另一种用于Actor确认它已经这样做了。Greet类型不仅包含要打招呼的对象的信息，它还包含消息发送方提供的ActorRef，以便HelloWorld Actor可以发送回确认消息。

在接收行为工厂的帮助下，Actor的行为被定义为Greeter 。处理下一条消息会导致可能与此消息不同的新行为。状态通过返回一个持有新的不可变状态的新行为来更新。在这种情况下，我们不需要更新任何状态，所以我们返回this，这意味着下一个行为“与当前行为相同”。

此行为处理的消息类型被声明为类Greet。通常，Actor 处理不止一种特定的消息类型，其中所有的消息类型都直接或间接实现一个公共接口。

在最后一行，我们看到HelloWorld Actor 向另一个 Actor发送消息，这是使用tell方法完成的。它是一个不阻塞调用者线程的异步操作。

由于 replyTo 地址被声明为 ActorRef<Greeted> 类型，编译器将只允许我们发送这种类型的消息，其他用法将是编译器错误。

Actor 接受的消息类型与所有应答类型一起定义了该 Actor 所说的协议;在本例中，它是一个简单的请求-应答协议，但 Actor 可以在需要时建模任意复杂的协议。协议与在 HelloWorld 类中实现它的行为绑定在一起。

正如卡尔·休伊特所说，一个 Actors 不是 Actors ——没有人可以交谈会很孤独。我们需要另一个与 Greeter 互动的 Actors 。让我们制作一个HelloWorldBot来接收来自Greeter 的回复，并发送一些额外的问候消息，然后收集这些回复，直到达到给定的最大数量的消息为止。

![20200805111011](https://liulv.work/images/img/20200805111011.png)

```java
public class HelloWorldBot extends AbstractBehavior<HelloWorld.Greeted> {

  public static Behavior<HelloWorld.Greeted> create(int max) {
    return Behaviors.setup(context -> new HelloWorldBot(context, max));
  }

  private final int max;
  private int greetingCounter;

  private HelloWorldBot(ActorContext<HelloWorld.Greeted> context, int max) {
    super(context);
    this.max = max;
  }

  @Override
  public Receive<HelloWorld.Greeted> createReceive() {
    return newReceiveBuilder().onMessage(HelloWorld.Greeted.class, this::onGreeted).build();
  }

  private Behavior<HelloWorld.Greeted> onGreeted(HelloWorld.Greeted message) {
    greetingCounter++;
    getContext().getLog().info("Greeting {} for {}", greetingCounter, message.whom);
    if (greetingCounter == max) {
      return Behaviors.stopped();
    } else {
      message.from.tell(new HelloWorld.Greet(message.whom, getContext().getSelf()));
      return this;
    }
  }
}
```

请注意该 Actor 是如何使用实例变量管理计数器的。由于actor实例一次只处理一条消息，因此不需要像synchronized或AtomicInteger这样的并发保护。

第三个Actors生成了Greeter和HelloWorldBot，并开始它们之间的交互。

```java
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {

  public static class SayHello {
    public final String name;

    public SayHello(String name) {
      this.name = name;
    }
  }

  public static Behavior<SayHello> create() {
    return Behaviors.setup(HelloWorldMain::new);
  }

  private final ActorRef<HelloWorld.Greet> greeter;

  private HelloWorldMain(ActorContext<SayHello> context) {
    super(context);
    greeter = context.spawn(HelloWorld.create(), "greeter");
  }

  @Override
  public Receive<SayHello> createReceive() {
    return newReceiveBuilder().onMessage(SayHello.class, this::onStart).build();
  }

  private Behavior<SayHello> onStart(SayHello command) {
    ActorRef<HelloWorld.Greeted> replyTo =
        getContext().spawn(HelloWorldBot.create(3), command.name);
    greeter.tell(new HelloWorld.Greet(command.name, replyTo));
    return this;
  }
}
```

现在我们想要尝试这个 Actor，所以我们必须启动一个 ActorSystem 来托管它:

```java
final ActorSystem<HelloWorldMain.SayHello> system =
    ActorSystem.create(HelloWorldMain.create(), "hello");

system.tell(new HelloWorldMain.SayHello("World"));
system.tell(new HelloWorldMain.SayHello("Akka"));
```

我们从定义的HelloWorldMain行为启动一个ActorSystem，并发送两个SayHello消息，这将启动两个单独的HelloWorldBot Actor和单个Greeter Actor之间的交互。

应用程序通常由单个ActorSystem组成，每个JVM运行多个Actor。

控制台输出可能是这样的:

```log
[2020-08-05 11:40:43,073] [INFO] [akka.event.slf4j.Slf4jLogger] [hello-akka.actor.default-dispatcher-3] [] - Slf4jLogger started
[2020-08-05 11:40:43,219] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello World!
[2020-08-05 11:40:43,221] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello Akka!
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-6] [akka://hello/user/World] - Greeting 1 for World
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-3] [akka://hello/user/Akka] - Greeting 1 for Akka
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello World!
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello Akka!
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-6] [akka://hello/user/World] - Greeting 2 for World
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello World!
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-3] [akka://hello/user/Akka] - Greeting 2 for Akka
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-6] [akka://hello/user/World] - Greeting 3 for World
[2020-08-05 11:40:43,222] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorld] [hello-akka.actor.default-dispatcher-5] [akka://hello/user/greeter] - Hello Akka!
[2020-08-05 11:40:43,223] [INFO] [com.tcfuture.akka.actor.example.helloword.nw.HelloWorldBot] [hello-akka.actor.default-dispatcher-3] [akka://hello/user/Akka] - Greeting 3 for Akka
```

您还需要添加[日志依赖](https://doc.akka.io/docs/akka/current/typed/logging.html)项，以便在运行时查看输出。

### 4.2.3. 复杂的例子

本例子更现实，演示了一些重要的模式:

* 使用接口和实现该接口的类来表示 Acotor 可以接收的多个消息
* 通过使用子 actors 处理会话
* 通过改变行为来处理状态
* 使用多个 actors 以类型安全的方式表示协议的不同部分

![20200805114627](https://liulv.work/images/img/20200805114627.png)

#### 4.2.3.1. 功能介绍

首先，我们将以函数样式显示此示例，然后以[面向对象样式](https://doc.akka.io/docs/akka/current/typed/actors.html#object-oriented-style)显示相同的示例。选择使用哪种风格是一个品味问题，两种风格都可以混合使用，这取决于哪种风格对特定的Actor来说是最好的。在[样式指南](https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style)中提供了选择的注意事项。


考虑一个运行聊天室的 Actor:客户端 Actor可以通过发送一条包含他们的屏幕名的消息来连接，然后他们可以发布消息。聊天室 Actor将把所有发布的消息发送给所有当前连接的客户端 Actor。协议定义可以如下所示:

```java
static interface RoomCommand {}

public static final class GetSession implements RoomCommand {
  public final String screenName;
  public final ActorRef<SessionEvent> replyTo;

  public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
    this.screenName = screenName;
    this.replyTo = replyTo;
  }
}

interface SessionEvent {}

public static final class SessionGranted implements SessionEvent {
  public final ActorRef<PostMessage> handle;

  public SessionGranted(ActorRef<PostMessage> handle) {
    this.handle = handle;
  }
}

public static final class SessionDenied implements SessionEvent {
  public final String reason;

  public SessionDenied(String reason) {
    this.reason = reason;
  }
}

public static final class MessagePosted implements SessionEvent {
  public final String screenName;
  public final String message;

  public MessagePosted(String screenName, String message) {
    this.screenName = screenName;
    this.message = message;
  }
}

interface SessionCommand {}

public static final class PostMessage implements SessionCommand {
  public final String message;

  public PostMessage(String message) {
    this.message = message;
  }
}

private static final class NotifyClient implements SessionCommand {
  final MessagePosted message;

  NotifyClient(MessagePosted message) {
    this.message = message;
  }
}
```

最初，客户端 Actor 只能访问允许他们进行第一步的 ActorRef<GetSession>。一旦建立了客户端的会话，它就会得到一个包含句柄的SessionGranted消息，该句柄用于解锁下一个协议步骤，即发布消息。需要将PostMessage命令发送到这个表示已添加到聊天室的会话的特定地址。会话的另一个方面是客户端通过replyTo参数显示了它自己的地址，以便后续的messagepost事件可以发送给它。

这说明了actor如何表达Java对象上的方法调用以外的内容。声明的消息类型及其内容描述了一个完整的协议，该协议可以涉及多个Actor，并且可以通过多个步骤进行演化。下面是聊天室协议的实现:

```java
public class ChatRoom {
  private static final class PublishSessionMessage implements RoomCommand {
    public final String screenName;
    public final String message;

    public PublishSessionMessage(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  public static Behavior<RoomCommand> create() {
    return Behaviors.setup(
        ctx -> new ChatRoom(ctx).chatRoom(new ArrayList<ActorRef<SessionCommand>>()));
  }

  private final ActorContext<RoomCommand> context;

  private ChatRoom(ActorContext<RoomCommand> context) {
    this.context = context;
  }

  private Behavior<RoomCommand> chatRoom(List<ActorRef<SessionCommand>> sessions) {
    return Behaviors.receive(RoomCommand.class)
        .onMessage(GetSession.class, getSession -> onGetSession(sessions, getSession))
        .onMessage(PublishSessionMessage.class, pub -> onPublishSessionMessage(sessions, pub))
        .build();
  }

  private Behavior<RoomCommand> onGetSession(
      List<ActorRef<SessionCommand>> sessions, GetSession getSession)
      throws UnsupportedEncodingException {
    ActorRef<SessionEvent> client = getSession.replyTo;
    ActorRef<SessionCommand> ses =
        context.spawn(
            Session.create(context.getSelf(), getSession.screenName, client),
            URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
    // narrow to only expose PostMessage
    client.tell(new SessionGranted(ses.narrow()));
    List<ActorRef<SessionCommand>> newSessions = new ArrayList<>(sessions);
    newSessions.add(ses);
    return chatRoom(newSessions);
  }

  private Behavior<RoomCommand> onPublishSessionMessage(
      List<ActorRef<SessionCommand>> sessions, PublishSessionMessage pub) {
    NotifyClient notification =
        new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
    sessions.forEach(s -> s.tell(notification));
    return Behaviors.same();
  }

  static class Session {
    static Behavior<ChatRoom.SessionCommand> create(
        ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
      return Behaviors.receive(ChatRoom.SessionCommand.class)
          .onMessage(PostMessage.class, post -> onPostMessage(room, screenName, post))
          .onMessage(NotifyClient.class, notification -> onNotifyClient(client, notification))
          .build();
    }

    private static Behavior<SessionCommand> onPostMessage(
        ActorRef<RoomCommand> room, String screenName, PostMessage post) {
      // from client, publish to others via the room
      room.tell(new PublishSessionMessage(screenName, post.message));
      return Behaviors.same();
    }

    private static Behavior<SessionCommand> onNotifyClient(
        ActorRef<SessionEvent> client, NotifyClient notification) {
      // published from the room
      client.tell(notification.message);
      return Behaviors.same();
    }
  }
}
```

通过改变行为而不是使用任何变量来管理状态。

当一个新的GetSession命令进来时，我们将该客户端添加到返回行为中的列表中。然后，我们还需要创建会话的ActorRef，它将用于发布消息。在本例中，我们希望创建一个非常简单的Actor，它将PostMessage命令重新打包到PublishSessionMessage命令中，该命令还包含屏幕名。

我们在这里声明的行为可以处理RoomCommand的两种子类型。GetSession已经解释过了，来自会话Actor的PublishSessionMessage命令将触发包含的聊天室消息向所有连接的客户端传播。但是我们不想给PublishSessionMessage命令发送到任意客户的能力,我们保留权利内部会话Actor我们create-otherwise客户可能会带来完全不同的屏幕名称(想象一下GetSession协议进一步包括身份验证信息安全)。

如果我们不关心保护会话和屏幕名之间的通信，那么我们可以更改协议，这样PostMessage被删除，所有客户端只获得ActorRef<PublishSessionMessage>来发送。在这种情况下，不需要会话actor，我们可以使用context.getSelf()。在这种情况下，类型检查是有效的，因为ActorRef<T>在其类型参数中是相反的，这意味着我们可以在需要ActorRef<PublishSessionMessage>的地方使用ActorRef<RoomCommand>——这是有意义的，因为前者会比后者使用更多的语言。反之则会出现问题，因此在ActorRef<RoomCommand>是必需的地方传递ActorRef<PublishSessionMessage>将导致类型错误。

#### 4.2.3.2. 尝试

为了看到这个聊天室在行动，我们需要写一个客户端 Actors，可以使用它:

```java
public class Gabbler {
  public static Behavior<ChatRoom.SessionEvent> create() {
    return Behaviors.setup(ctx -> new Gabbler(ctx).behavior());
  }

  private final ActorContext<ChatRoom.SessionEvent> context;

  private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
    this.context = context;
  }

  private Behavior<ChatRoom.SessionEvent> behavior() {
    return Behaviors.receive(ChatRoom.SessionEvent.class)
        .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
        .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
        .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
        .build();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
    context.getLog().info("cannot start chat room session: {}", message.reason);
    return Behaviors.stopped();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
    message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
    return Behaviors.same();
  }

  private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
    context
        .getLog()
        .info("message has been posted by '{}': {}", message.screenName, message.message);
    return Behaviors.stopped();
  }
}
```

通过这种行为，我们可以创建一个Actor，它将接受聊天室会话、发布消息、等待它的发布，然后终止。最后一步需要改变行为的能力，我们需要从正常的运行行为转换到终止状态。这就是为什么在这里我们不返回相同的值，而返回另一个特殊的停止值。

现在要尝试一下，我们必须同时启动聊天室和gabbler当然我们是在Actor系统中进行的。既然只能有一个用户监护人，我们可以从聊天室的gabbler(这不是我们想要的——它使逻辑复杂化了)或者从聊天室的gabbler(这是荒谬的)或者我们从第三个角色开始他们两个——我们唯一明智的选择:

```java
public class Main {
  public static Behavior<Void> create() {
    return Behaviors.setup(
        context -> {
          ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
          ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
          context.watch(gabbler);
          chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

          return Behaviors.receive(Void.class)
              .onSignal(Terminated.class, sig -> Behaviors.stopped())
              .build();
        });
  }

  public static void main(String[] args) {
    ActorSystem.create(Main.create(), "ChatRoomDemo");
  }
}
```

在良好的传统中，我们称主 Actor 为它是什么，它直接对应于传统Java应用程序中的主方法。这个Actor将自动执行它的工作，我们不需要从外部发送消息，所以我们将它声明为Void类型。Actor不仅接收外部消息，还会收到某些系统事件的通知，即所谓的信号。为了访问它们，我们选择使用receive behavior decorator来实现这个特定的操作。对于信号(Signal的子类)将调用提供的onSignal函数，对于用户消息将调用onMessage函数。

这个特殊的主要Actor是使用behavior创建的。设置，它就像行为的工厂。与行为相反，行为实例的创建被推迟到Actor启动时。在actor运行之前立即创建行为实例的接收。设置中的工厂函数作为参数传递给ActorContext，例如，它可以用于生成子actor。这个主Actor创建聊天室和gabbler，并启动它们之间的会话，当gabbler结束时，我们将收到由于调用了context而终止的事件。观看。这允许我们关闭Actor系统:当主Actor终止时，就不再需要做什么了。

因此，在创建具有主 Actor 行为的 Actor 系统之后，我们可以让主方法返回，ActorSystem将继续运行，JVM将保持活动状态，直到根Actor停止。

#### 4.2.3.3. 面向对象风格

上面的示例使用了函数式编程风格，在这种风格中，您将一个函数传递给工厂，然后由工厂构造一个行为，对于有状态的Actor，这意味着将不可变的状态作为参数传递，并在需要对更改的状态进行操作时切换到新的行为。另一种表达方式是更面向对象的风格，其中定义了actor行为的具体类，可变状态作为字段保存在其中。

选择使用哪种风格是一个品味问题，两种风格都可以混合使用，这取决于哪种风格对特定的Actor来说是最好的。在[样式指南](https://doc.akka.io/docs/akka/current/typed/style-guide.html#functional-versus-object-oriented-style)中提供了选择的注意事项。

##### 4.2.3.3.1. AbstractBehavior API

定义基于actor行为的类从扩展akka.actor.type.javads.AbstractBehavior<T>开始，其中T是行为将接受的消息类型。

让我们重复上面使用AbstractBehavior实现的更复杂示例中的聊天室示例。与actor交互的协议看起来是一样的:

```java
static interface RoomCommand {}

public static final class GetSession implements RoomCommand {
  public final String screenName;
  public final ActorRef<SessionEvent> replyTo;

  public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
    this.screenName = screenName;
    this.replyTo = replyTo;
  }
}

static interface SessionEvent {}

public static final class SessionGranted implements SessionEvent {
  public final ActorRef<PostMessage> handle;

  public SessionGranted(ActorRef<PostMessage> handle) {
    this.handle = handle;
  }
}

public static final class SessionDenied implements SessionEvent {
  public final String reason;

  public SessionDenied(String reason) {
    this.reason = reason;
  }
}

public static final class MessagePosted implements SessionEvent {
  public final String screenName;
  public final String message;

  public MessagePosted(String screenName, String message) {
    this.screenName = screenName;
    this.message = message;
  }
}

static interface SessionCommand {}

public static final class PostMessage implements SessionCommand {
  public final String message;

  public PostMessage(String message) {
    this.message = message;
  }
}

private static final class NotifyClient implements SessionCommand {
  final MessagePosted message;

  NotifyClient(MessagePosted message) {
    this.message = message;
  }
}
```

最初，客户端Actor只能访问允许他们进行第一步的ActorRef<GetSession>。一旦建立了客户端的会话，它就会得到一个包含句柄的SessionGranted消息，该句柄用于解锁下一个协议步骤，即发布消息。需要将PostMessage命令发送到这个表示已添加到聊天室的会话的特定地址。会话的另一个方面是客户端通过replyTo参数显示了它自己的地址，以便后续的messagepost事件可以发送给它。

这说明了actor如何表达Java对象上的方法调用以外的内容。声明的消息类型及其内容描述了一个完整的协议，该协议可以涉及多个Actor，并且可以通过多个步骤进行演化。下面是聊天室协议的AbstractBehavior实现:

```java
public class ChatRoom {
  private static final class PublishSessionMessage implements RoomCommand {
    public final String screenName;
    public final String message;

    public PublishSessionMessage(String screenName, String message) {
      this.screenName = screenName;
      this.message = message;
    }
  }

  public static Behavior<RoomCommand> create() {
    return Behaviors.setup(ChatRoomBehavior::new);
  }

  public static class ChatRoomBehavior extends AbstractBehavior<RoomCommand> {
    final List<ActorRef<SessionCommand>> sessions = new ArrayList<>();

    private ChatRoomBehavior(ActorContext<RoomCommand> context) {
      super(context);
    }

    @Override
    public Receive<RoomCommand> createReceive() {
      ReceiveBuilder<RoomCommand> builder = newReceiveBuilder();

      builder.onMessage(GetSession.class, this::onGetSession);
      builder.onMessage(PublishSessionMessage.class, this::onPublishSessionMessage);

      return builder.build();
    }

    private Behavior<RoomCommand> onGetSession(GetSession getSession)
        throws UnsupportedEncodingException {
      ActorRef<SessionEvent> client = getSession.replyTo;
      ActorRef<SessionCommand> ses =
          getContext()
              .spawn(
                  SessionBehavior.create(getContext().getSelf(), getSession.screenName, client),
                  URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
      // narrow to only expose PostMessage
      client.tell(new SessionGranted(ses.narrow()));
      sessions.add(ses);
      return this;
    }

    private Behavior<RoomCommand> onPublishSessionMessage(PublishSessionMessage pub) {
      NotifyClient notification =
          new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
      sessions.forEach(s -> s.tell(notification));
      return this;
    }
  }

  static class SessionBehavior extends AbstractBehavior<ChatRoom.SessionCommand> {
    private final ActorRef<RoomCommand> room;
    private final String screenName;
    private final ActorRef<SessionEvent> client;

    public static Behavior<ChatRoom.SessionCommand> create(
        ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
      return Behaviors.setup(context -> new SessionBehavior(context, room, screenName, client));
    }

    private SessionBehavior(
        ActorContext<ChatRoom.SessionCommand> context,
        ActorRef<RoomCommand> room,
        String screenName,
        ActorRef<SessionEvent> client) {
      super(context);
      this.room = room;
      this.screenName = screenName;
      this.client = client;
    }

    @Override
    public Receive<SessionCommand> createReceive() {
      return newReceiveBuilder()
          .onMessage(PostMessage.class, this::onPostMessage)
          .onMessage(NotifyClient.class, this::onNotifyClient)
          .build();
    }

    private Behavior<SessionCommand> onPostMessage(PostMessage post) {
      // from client, publish to others via the room
      room.tell(new PublishSessionMessage(screenName, post.message));
      return Behaviors.same();
    }

    private Behavior<SessionCommand> onNotifyClient(NotifyClient notification) {
      // published from the room
      client.tell(notification.message);
      return Behaviors.same();
    }
  }
}
```

状态是通过类中的字段管理的，就像常规的面向对象类一样。由于状态是可变的，所以我们不会从消息逻辑返回不同的行为，但是可以返回AbstractBehavior实例本身(this)作为处理传入的下一个消息的行为。我们还可以返回Behavior。以同样的方式达到同样的目的。

在这个示例中，我们创建了单独的语句来创建行为构建器，但是它也会从每一步返回构建器本身，因此也可以使用更流畅的行为定义样式。您应该选择的内容取决于Actor接受的消息集的大小。

也可以返回一个新的不同的AbstractBehavior,例如代表一个不同的国家在一个有限状态机(FSM),或使用一个功能行为的工厂与功能结合面向对象风格的不同部分的生命周期相同的演员的行为。

当新的GetSession命令传入时，我们将该客户端添加到当前会话列表中。然后，我们还需要创建会话的ActorRef，它将用于发布消息。在本例中，我们希望创建一个非常简单的Actor，它将PostMessage命令重新打包到PublishSessionMessage命令中，该命令还包含屏幕名。

为了实现为会话生成子元素的逻辑，我们需要访问ActorContext。这是在创建行为时作为构造函数参数注入的，请注意我们是如何将AbstractBehavior和behavior结合在一起的。在创建工厂方法中执行此操作。

我们在这里声明的行为可以处理RoomCommand的两种子类型。GetSession已经解释过了，来自会话Actor的PublishSessionMessage命令将触发包含的聊天室消息向所有连接的客户端传播。但是我们不想给PublishSessionMessage命令发送到任意客户的能力,我们保留权利内部会话Actor我们create-otherwise客户可能会带来完全不同的屏幕名称(想象一下GetSession协议进一步包括身份验证信息安全)。

如果我们不关心保护会话和屏幕名之间的通信，那么我们可以更改协议，这样PostMessage被删除，所有客户端只获得ActorRef<PublishSessionMessage>来发送。在这种情况下，不需要会话actor，我们可以使用context.getSelf()。在这种情况下，类型检查是有效的，因为ActorRef<T>在其类型参数中是相反的，这意味着我们可以在需要ActorRef<PublishSessionMessage>的地方使用ActorRef<RoomCommand>——这是有意义的，因为前者会比后者使用更多的语言。反之则会出现问题，因此在ActorRef<RoomCommand>是必需的地方传递ActorRef<PublishSessionMessage>将导致类型错误。

##### 4.2.3.3.2. 尝试

为了看到这个聊天室在行动，我们需要写一个客户端演员，可以使用它:

```java
public class Gabbler extends AbstractBehavior<ChatRoom.SessionEvent> {
  public static Behavior<ChatRoom.SessionEvent> create() {
    return Behaviors.setup(Gabbler::new);
  }

  private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
    super(context);
  }

  @Override
  public Receive<ChatRoom.SessionEvent> createReceive() {
    ReceiveBuilder<ChatRoom.SessionEvent> builder = newReceiveBuilder();
    return builder
        .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
        .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
        .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
        .build();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
    getContext().getLog().info("cannot start chat room session: {}", message.reason);
    return Behaviors.stopped();
  }

  private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
    message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
    return Behaviors.same();
  }

  private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
    getContext()
        .getLog()
        .info("message has been posted by '{}': {}", message.screenName, message.message);
    return Behaviors.stopped();
  }
}
```

现在要尝试一下，我们必须同时启动聊天室和gabbler当然我们是在Actor系统中进行的。既然只能有一个用户监护人，我们可以从聊天室的gabbler(这不是我们想要的——它使逻辑复杂化了)或者从聊天室的gabbler(这是荒谬的)或者我们从第三个角色开始他们两个——我们唯一明智的选择:

```java
public class Main {
  public static Behavior<Void> create() {
    return Behaviors.setup(
        context -> {
          ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(ChatRoom.create(), "chatRoom");
          ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(Gabbler.create(), "gabbler");
          context.watch(gabbler);
          chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));

          return Behaviors.receive(Void.class)
              .onSignal(Terminated.class, sig -> Behaviors.stopped())
              .build();
        });
  }

  public static void main(String[] args) {
    ActorSystem.create(Main.create(), "ChatRoomDemo");
  }
}
```

在良好的传统中，我们称主Actor为它是什么，它直接对应于传统Java应用程序中的主方法。这个Actor将自动执行它的工作，我们不需要从外部发送消息，所以我们将它声明为Void类型。Actor不仅接收外部消息，还会收到某些系统事件的通知，即所谓的信号。为了访问它们，我们选择使用receive behavior decorator来实现这个特定的操作。对于信号(Signal的子类)将调用提供的onSignal函数，对于用户消息将调用onMessage函数。

这个特殊的主要Actor是使用behavior创建的。设置，它就像行为的工厂。与行为相反，行为实例的创建被推迟到Actor启动时。在actor运行之前立即创建行为实例的接收。设置中的工厂函数作为参数传递给ActorContext，例如，它可以用于生成子actor。这个主Actor创建聊天室和gabbler，并启动它们之间的会话，当gabbler结束时，我们将收到由于调用了context而终止的事件。观看。这允许我们关闭Actor系统:当主Actor终止时，就不再需要做什么了。

因此，在创建具有主Actor行为的Actor系统之后，我们可以让主方法返回，ActorSystem将继续运行，JVM将保持活动状态，直到根Actor停止。


## 4.3. Actor生命周期

### 4.3.1. 简介

Actor是必须显式启动和停止的有状态资源。

重要的是要注意，当不再引用Actor时，Actor不会自动停止，创建的每个Actor也必须显式地销毁。惟一的简化是，停止父Actor还将递归地停止该父Actor创建的所有子Actor。当ActorSystem关闭时，所有的Actors也会自动停止。

> **请注意**
> ActorSystem是重量级结构，它将分配线程，因此为每个逻辑应用程序创建一个线程。通常每个JVM进程有一个 ActorSystem。

### 4.3.2. 创建Actors

一个actor可以创建或衍生任意数量的子actor，而子actor又可以衍生自己的子actor，从而形成一个actor层次结构。[ActorSystem](https://doc.akka.io/japi/akka/2.6/akka/actor/typed/ActorSystem.html)托管层次结构，并且只能有一个根Actor，即位于ActorSystem层次结构顶端的Actor。子角色的生命周期与父角色相关联——子角色可以停止自己或在任何时候被停止，但它永远无法活过父角色。

#### 4.3.2.1. ActorContext

ActorContext可以被多种目的访问，例如:

* 创建子Acotr和监督
* 观察其他Actor以接收一个终止的(其他Actor)事件，如果被观察的Actor永久停止
* 日志记录
* 创建消息适配器
* 与另一个Actor的请求-响应交互(ask)
* 对self ActorRef的访问

如果一个行为需要使用ActorContext，例如衍生子actor，或者使用context.getSelf()，它可以通过用Behaviors.setup包装构造来获得:  

```java
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {
  public static Behavior<SayHello> create() {
    return Behaviors.setup(HelloWorldMain::new);
  }

  private final ActorRef<HelloWorld.Greet> greeter;

  private HelloWorldMain(ActorContext<SayHello> context) {
    super(context);
    greeter = context.spawn(HelloWorld.create(), "greeter");
  }
}
```  

#### 4.3.2.2. ActorContext线程安全

ActorContext中的许多方法都不是线程安全的

* 不能被来自java.util.concurrent.CompletionStage的线程访问回调。
* 不能在多个actor实例之间共享
* 必须只在普通actor线程中使用消息处理

### 4.3.3. 监护Actor

顶级Actor，也称为用户监护Actor，是与ActorSystem一起创建的。发送到Actor系统的消息被定向到根Actor。根actor是由用来创建ActorSystem的行为定义的，在下面的例子中命名为HelloWorldMain:

```java
final ActorSystem<HelloWorldMain.SayHello> system =
    ActorSystem.create(HelloWorldMain.create(), "hello");

system.tell(new HelloWorldMain.SayHello("World"));
system.tell(new HelloWorldMain.SayHello("Akka"));
```

对于非常简单的应用程序，guardian可能包含实际的应用程序逻辑和处理消息。一旦应用程序处理了不止一个问题，监护人就应该引导应用程序，将各种子系统作为子系统生成，并监视它们的生命周期。

当guardian Actor停止时，将停止ActorSystem。

当ActorSystem.terminate [协调关闭](https://doc.akka.io/docs/akka/current/coordinated-shutdown.html)流程将以特定的顺序停止Actor和服务。

### 4.3.4. 创建子Actor

子actor是通过[ActorContext](https://doc.akka.io/japi/akka/2.6/akka/actor/typed/javadsl/ActorContext.html)的衍生来创建和启动的。在下面的示例中，当根actor启动时，它会生成一个由HelloWorld行为描述的子actor。此外，当根actor接收到一个开始消息时，它会创建一个由行为HelloWorldBot定义的子actor:

```java
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {

  public static class SayHello {
    public final String name;

    public SayHello(String name) {
      this.name = name;
    }
  }

  public static Behavior<SayHello> create() {
    return Behaviors.setup(HelloWorldMain::new);
  }

  private final ActorRef<HelloWorld.Greet> greeter;

  private HelloWorldMain(ActorContext<SayHello> context) {
    super(context);
    greeter = context.spawn(HelloWorld.create(), "greeter");
  }

  @Override
  public Receive<SayHello> createReceive() {
    return newReceiveBuilder().onMessage(SayHello.class, this::onStart).build();
  }

  private Behavior<SayHello> onStart(SayHello command) {
    ActorRef<HelloWorld.Greeted> replyTo =
        getContext().spawn(HelloWorldBot.create(3), command.name);
    greeter.tell(new HelloWorld.Greet(command.name, replyTo));
    return this;
  }
}
```

要在生成actor时指定dispatcher，请使用[DispatcherSelector](https://doc.akka.io/japi/akka/2.6/akka/actor/typed/DispatcherSelector.html)。如果未指定，则Actor将使用默认分派器，有关详细信息，请参阅default分派器。

```java
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {

  // Start message...

  public static Behavior<SayHello> create() {
    return Behaviors.setup(HelloWorldMain::new);
  }

  private final ActorRef<HelloWorld.Greet> greeter;

  private HelloWorldMain(ActorContext<SayHello> context) {
    super(context);

    final String dispatcherPath = "akka.actor.default-blocking-io-dispatcher";
    Props greeterProps = DispatcherSelector.fromConfig(dispatcherPath);
    greeter = getContext().spawn(HelloWorld.create(), "greeter", greeterProps);
  }

  // createReceive ...
}
```

请参考[Actors](#422-)了解上述示例的大致内容。

### 4.3.5. Spawn协议

监护Actor应该负责任务的初始化，并创建应用程序的初始参Actor，但有时您可能希望从监护参Actor的外部派生新的参Actor。例如，为每个HTTP请求创建一个actor。

这在您的行为中并不难实现，但是由于这是一种常见的模式，因此有一个预定义的消息协议和行为实现。它可以作为行为人系统的监护行为人，也可以与行为相结合。设置以启动一些初始任务或Actor。然后可以通过告诉或询问SpawnProtocol从外部启动子actor。衍生到系统的actor引用。使用ask类似于如何ActorSystem。actorOf可以在经典的actor中使用，区别在于返回ActorRef的CompletionStage。

监护人行为可以定义为:

```java
import akka.actor.typed.Behavior;
import akka.actor.typed.SpawnProtocol;
import akka.actor.typed.javadsl.Behaviors;

public abstract class HelloWorldMain {
  private HelloWorldMain() {}

  public static Behavior<SpawnProtocol.Command> create() {
    return Behaviors.setup(
        context -> {
          // Start initial tasks
          // context.spawn(...)

          return SpawnProtocol.create();
        });
  }
}
```

和ActorSystem可以创建与主行为和要求衍生其他Actor:

```java
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.AskPattern;

final ActorSystem<SpawnProtocol.Command> system =
    ActorSystem.create(HelloWorldMain.create(), "hello");
final Duration timeout = Duration.ofSeconds(3);

CompletionStage<ActorRef<HelloWorld.Greet>> greeter =
    AskPattern.ask(
        system,
        replyTo ->
            new SpawnProtocol.Spawn<>(HelloWorld.create(), "greeter", Props.empty(), replyTo),
        timeout,
        system.scheduler());

Behavior<HelloWorld.Greeted> greetedBehavior =
    Behaviors.receive(
        (context, message) -> {
          context.getLog().info("Greeting for {} from {}", message.whom, message.from);
          return Behaviors.stopped();
        });

CompletionStage<ActorRef<HelloWorld.Greeted>> greetedReplyTo =
    AskPattern.ask(
        system,
        replyTo -> new SpawnProtocol.Spawn<>(greetedBehavior, "", Props.empty(), replyTo),
        timeout,
        system.scheduler());

greeter.whenComplete(
    (greeterRef, exc) -> {
      if (exc == null) {
        greetedReplyTo.whenComplete(
            (greetedReplyToRef, exc2) -> {
              if (exc2 == null) {
                greeterRef.tell(new HelloWorld.Greet("Akka", greetedReplyToRef));
              }
            });
      }
    });
```

SpawnProtocol也可以在actor层次结构的其他地方使用。它不必是根守护角色。

在[Actor发现](https://doc.akka.io/docs/akka/current/typed/actor-discovery.html)中描述了查找运行Actor的方法。

### 4.3.6. 停止Actor

一个actor可以通过返回来停止自己Behaviors.stopped作为下一个行为。

通过使用父actor的ActorContext的stop方法，可以强制子actor在处理完当前消息后停止。只有子Actors可以用这种方式停止。

所有的子Actor将在其父Actor被停止时停止。

当一个actor停止时，它会接收到一个PostStop信号，这个信号可以用来清理资源。一个回调函数可以被指定为行为的参数。在优雅停止时处理PostStop信号。这允许在突然停止时应用不同的操作。

下面一个停止actor例子：

```java
import java.util.concurrent.TimeUnit;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.PostStop;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;


public class MasterControlProgram extends AbstractBehavior<MasterControlProgram.Command> {

  interface Command {}

  public static final class SpawnJob implements Command {
    public final String name;

    public SpawnJob(String name) {
      this.name = name;
    }
  }

  public enum GracefulShutdown implements Command {
    INSTANCE
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(MasterControlProgram::new);
  }

  public MasterControlProgram(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(SpawnJob.class, this::onSpawnJob)
        .onMessage(GracefulShutdown.class, message -> onGracefulShutdown())
        .onSignal(PostStop.class, signal -> onPostStop())
        .build();
  }

  private Behavior<Command> onSpawnJob(SpawnJob message) {
    getContext().getSystem().log().info("Spawning job {}!", message.name);
    getContext().spawn(Job.create(message.name), message.name);
    return this;
  }

  private Behavior<Command> onGracefulShutdown() {
    getContext().getSystem().log().info("Initiating graceful shutdown...");

    // perform graceful stop, executing cleanup before final system termination
    // behavior executing cleanup is passed as a parameter to Actor.stopped
    return Behaviors.stopped(() -> getContext().getSystem().log().info("Cleanup!"));
  }

  private Behavior<Command> onPostStop() {
    getContext().getSystem().log().info("Master Control Program stopped");
    return this;
  }
}

public class Job extends AbstractBehavior<Job.Command> {

  interface Command {}

  public static Behavior<Command> create(String name) {
    return Behaviors.setup(context -> new Job(context, name));
  }

  private final String name;

  public Job(ActorContext<Command> context, String name) {
    super(context);
    this.name = name;
  }

  @Override
  public Receive<Job.Command> createReceive() {
    return newReceiveBuilder().onSignal(PostStop.class, postStop -> onPostStop()).build();
  }

  private Behavior<Command> onPostStop() {
    getContext().getSystem().log().info("Worker {} stopped", name);
    return this;
  }
}

final ActorSystem<MasterControlProgram.Command> system =
    ActorSystem.create(MasterControlProgram.create(), "B6700");

system.tell(new MasterControlProgram.SpawnJob("a"));
system.tell(new MasterControlProgram.SpawnJob("b"));

// sleep here to allow time for the new actors to be started
Thread.sleep(100);

system.tell(MasterControlProgram.GracefulShutdown.INSTANCE);

system.getWhenTerminated().toCompletableFuture().get(3, TimeUnit.SECONDS);
```

在从PostStop清理资源时，您还应该考虑对PreRestart信号执行相同的操作，该信号是在actor重新启动时发出的。注意，PostStop不是为重新启动而发出的。


### 4.3.7. Watching Actors

为了在另一个actor终止时得到通知(即永久停止，而不是临时失败并重新启动)，一个actor可以监视另一个actor。被观察Actor的终止(请查看[停止Actor](#436-停止Acto))时，将收到终止信号。

```java
public class MasterControlProgram extends AbstractBehavior<MasterControlProgram.Command> {

  interface Command {}

  public static final class SpawnJob implements Command {
    public final String name;

    public SpawnJob(String name) {
      this.name = name;
    }
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(MasterControlProgram::new);
  }

  public MasterControlProgram(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(SpawnJob.class, this::onSpawnJob)
        .onSignal(Terminated.class, this::onTerminated)
        .build();
  }

  private Behavior<Command> onSpawnJob(SpawnJob message) {
    getContext().getSystem().log().info("Spawning job {}!", message.name);
    ActorRef<Job.Command> job = getContext().spawn(Job.create(message.name), message.name);
    getContext().watch(job);
    return this;
  }

  private Behavior<Command> onTerminated(Terminated terminated) {
    getContext().getSystem().log().info("Job stopped: {}", terminated.getRef().path().name());
    return this;
  }
}
```

监视的另一种方法是watchWith，它允许指定自定义消息而不是终止消息。这通常比使用watch和终止信号更可取，因为可以在消息中包含其他信息，以便稍后接收时使用。

与上面的示例类似，但是在任务完成时使用watchWith并对原始请求者进行应答。

```java
public class MasterControlProgram extends AbstractBehavior<MasterControlProgram.Command> {

  interface Command {}

  public static final class SpawnJob implements Command {
    public final String name;
    public final ActorRef<JobDone> replyToWhenDone;

    public SpawnJob(String name, ActorRef<JobDone> replyToWhenDone) {
      this.name = name;
      this.replyToWhenDone = replyToWhenDone;
    }
  }

  public static final class JobDone {
    public final String name;

    public JobDone(String name) {
      this.name = name;
    }
  }

  private static final class JobTerminated implements Command {
    final String name;
    final ActorRef<JobDone> replyToWhenDone;

    JobTerminated(String name, ActorRef<JobDone> replyToWhenDone) {
      this.name = name;
      this.replyToWhenDone = replyToWhenDone;
    }
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(MasterControlProgram::new);
  }

  public MasterControlProgram(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(SpawnJob.class, this::onSpawnJob)
        .onMessage(JobTerminated.class, this::onJobTerminated)
        .build();
  }

  private Behavior<Command> onSpawnJob(SpawnJob message) {
    getContext().getSystem().log().info("Spawning job {}!", message.name);
    ActorRef<Job.Command> job = getContext().spawn(Job.create(message.name), message.name);
    getContext().watchWith(job, new JobTerminated(message.name, message.replyToWhenDone));
    return this;
  }

  private Behavior<Command> onJobTerminated(JobTerminated terminated) {
    getContext().getSystem().log().info("Job stopped: {}", terminated.name);
    terminated.replyToWhenDone.tell(new JobDone(terminated.name));
    return this;
  }
}
```

请注意replyToWhenDone是如何包含在watchWith消息中，然后在稍后接收终止作业的消息时使用的。

被观察的actor可以是任何ActorRef，它不必是上面示例中的子actor。

应该注意，终止消息的生成与注册和终止发生的顺序无关。特别是，即使被监视的Actor在注册时已经被终止，监视的Actor也会收到终止消息。

多次注册并不一定导致产生多条消息,但没有保证只有一个收到这样的消息:如果终止观看演员的生成和消息排队,和另一个注册这个消息被处理之前完成,然后第二个消息将被排队,因为注册的监控已经终止的演员会导致立即终止消息的一代。

它也可以取消观看另一个演员的动态使用背景。unwatch(目标)。即使终止的消息已经在邮箱中排队，这也可以工作;在调用unwatch后，不会再处理该actor的终止消息。

当被监视的actor位于已从[集群](https://doc.akka.io/docs/akka/current/typed/cluster.html)中删除的节点上时，也会发送终止的消息。


## 4.4. 互动模式

### 4.4.1. 简介

在Akka中与Actor的交互是通过ActorRef<T>完成的，其中T是Actor接受的消息类型，也称为“协议”。这确保了只有正确类型的消息可以发送给actor，而且除了actor本身之外，没有其他人可以访问actor实例的内部。


与Actor的消息交换遵循一些常见的模式，让我们逐一介绍其中的每一个模式。

### 4.4.2. Fire-forget

与actor交互的基本方法是通过actorRef.tell(message)。使用tell发送消息可以从任何线程安全地完成。

Tell是异步的，这意味着该方法立即返回。语句执行后，不能保证接收方已经处理了消息。它还意味着无法知道消息是否被接收、处理是否成功。

Example:

![20200805205635](https://liulv.work/images/img/20200805205635.png)

给定协议和Actor行为:

```java
public class Printer {
  public static class PrintMe {
    public final String message;

    public PrintMe(String message) {
      this.message = message;
    }
  }

  public static Behavior<PrintMe> create() {
    return Behaviors.setup(
        context ->
            Behaviors.receive(PrintMe.class)
                .onMessage(
                    PrintMe.class,
                    printMe -> {
                      context.getLog().info(printMe.message);
                      return Behaviors.same();
                    })
                .build());
  }
}
```

异步发送消息如下：

```java
final ActorSystem<Printer.PrintMe> system =
    ActorSystem.create(Printer.create(), "printer-sample-system");

// note that system is also the ActorRef to the guardian actor
final ActorRef<Printer.PrintMe> ref = system;

// these are all fire and forget
ref.tell(new Printer.PrintMe("message 1"));
ref.tell(new Printer.PrintMe("message 2"));
```

**有用的时候：**

* 确保消息已被处理并不重要
* 没有办法对不成功的交付或处理采取行动
* 我们希望最小化创建的消息数量，以获得更高的吞吐量(发送响应将需要创建两倍于此的消息数量)

**存在问题：**

* 如果消息的流入超过了actor的处理能力，那么收件箱将被填满，在最坏的情况下会导致JVM崩溃，并产生OutOfMemoryError错误
* 如果消息丢失，发送者将不知道

### 4.4.3. 请求-响应

Actor之间的许多交互都需要从接收Actor发回一条或多条响应消息。响应消息可以是查询的结果、接收和处理消息的某种形式的确认或请求订阅的事件。

在Akka中，响应的接收方必须被编码为消息本身中的一个字段，然后接收方可以使用该字段发送(告知)回响应。

Example:

![20200805210650](https://liulv.work/images/img/20200805210650.png)

根据以下协议:

```java
public static class Request {
  public final String query;
  public final ActorRef<Response> replyTo;

  public Request(String query, ActorRef<Response> replyTo) {
    this.query = query;
    this.replyTo = replyTo;
  }
}

public static class Response {
  public final String result;

  public Response(String result) {
    this.result = result;
  }
}
```

发送方将使用自己的ActorRef<Response>，它可以通过ActorContext.getSelf()对replyTo进行访问。

```java
cookieFabric.tell(new CookieFabric.Request("give me cookies", context.getSelf()));
```

在接收方的ActorRef<Response>可以用来发送一个或多个响应回来:

```java
// actor behavior
public static Behavior<Request> create() {
  return Behaviors.receive(Request.class)
      .onMessage(Request.class, CookieFabric::onRequest)
      .build();
}

private static Behavior<Request> onRequest(Request request) {
  // ... process request ...
  request.replyTo.tell(new Response("Here are the cookies for " + request.query));
  return Behaviors.same();
}
```

有用的时候:

订阅将发回许多响应消息的Actor

面临问题：

* Actor很少有来自另一个Actor的响应消息作为其协议的一部分(参见[适应响应](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#adapted-response))
* 很难检测到没有发送或处理消息请求(参见[ask](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#request-response-with-ask-between-two-actors))
* 除非协议已经包含了提供上下文的方法，例如在响应中发送的请求id，否则不可能在不引入新的、单独的Actor的情况下将交互与特定的上下文绑定(参见ask或[per session子Actor](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#per-session-child-actor))。


### 4.4.4. 适应响应

大多数情况下，发送actor不支持，也不应该支持接收另一个actor的响应消息。在这种情况下，我们需要提供正确类型的ActorRef，并将响应消息调整为发送actor可以处理的类型。

Example:

![20200805212210](https://liulv.work/images/img/20200805212210.png)

```java
public class Backend {
  public interface Request {}

  public static class StartTranslationJob implements Request {
    public final int taskId;
    public final URI site;
    public final ActorRef<Response> replyTo;

    public StartTranslationJob(int taskId, URI site, ActorRef<Response> replyTo) {
      this.taskId = taskId;
      this.site = site;
      this.replyTo = replyTo;
    }
  }

  public interface Response {}

  public static class JobStarted implements Response {
    public final int taskId;

    public JobStarted(int taskId) {
      this.taskId = taskId;
    }
  }

  public static class JobProgress implements Response {
    public final int taskId;
    public final double progress;

    public JobProgress(int taskId, double progress) {
      this.taskId = taskId;
      this.progress = progress;
    }
  }

  public static class JobCompleted implements Response {
    public final int taskId;
    public final URI result;

    public JobCompleted(int taskId, URI result) {
      this.taskId = taskId;
      this.result = result;
    }
  }
}

public class Frontend {

  public interface Command {}

  public static class Translate implements Command {
    public final URI site;
    public final ActorRef<URI> replyTo;

    public Translate(URI site, ActorRef<URI> replyTo) {
      this.site = site;
      this.replyTo = replyTo;
    }
  }

  private static class WrappedBackendResponse implements Command {
    final Backend.Response response;

    public WrappedBackendResponse(Backend.Response response) {
      this.response = response;
    }
  }

  public static class Translator extends AbstractBehavior<Command> {
    private final ActorRef<Backend.Request> backend;
    private final ActorRef<Backend.Response> backendResponseAdapter;

    private int taskIdCounter = 0;
    private Map<Integer, ActorRef<URI>> inProgress = new HashMap<>();

    public Translator(ActorContext<Command> context, ActorRef<Backend.Request> backend) {
      super(context);
      this.backend = backend;
      this.backendResponseAdapter =
          context.messageAdapter(Backend.Response.class, WrappedBackendResponse::new);
    }

    @Override
    public Receive<Command> createReceive() {
      return newReceiveBuilder()
          .onMessage(Translate.class, this::onTranslate)
          .onMessage(WrappedBackendResponse.class, this::onWrappedBackendResponse)
          .build();
    }

    private Behavior<Command> onTranslate(Translate cmd) {
      taskIdCounter += 1;
      inProgress.put(taskIdCounter, cmd.replyTo);
      backend.tell(
          new Backend.StartTranslationJob(taskIdCounter, cmd.site, backendResponseAdapter));
      return this;
    }

    private Behavior<Command> onWrappedBackendResponse(WrappedBackendResponse wrapped) {
      Backend.Response response = wrapped.response;
      if (response instanceof Backend.JobStarted) {
        Backend.JobStarted rsp = (Backend.JobStarted) response;
        getContext().getLog().info("Started {}", rsp.taskId);
      } else if (response instanceof Backend.JobProgress) {
        Backend.JobProgress rsp = (Backend.JobProgress) response;
        getContext().getLog().info("Progress {}", rsp.taskId);
      } else if (response instanceof Backend.JobCompleted) {
        Backend.JobCompleted rsp = (Backend.JobCompleted) response;
        getContext().getLog().info("Completed {}", rsp.taskId);
        inProgress.get(rsp.taskId).tell(rsp.result);
        inProgress.remove(rsp.taskId);
      } else {
        return Behaviors.unhandled();
      }

      return this;
    }
  }
}
```

您可以为不同的消息类注册多个消息适配器。每个消息类只能有一个消息适配器，以确保重复注册后适配器的数量不会无限制地增长。这也意味着注册的适配器将替换同一消息类的现有适配器。

如果消息类与给定的类匹配，或者是该类的子类，则将使用消息适配器。已注册的适配器将按照与它们的注册顺序相反的顺序进行尝试，即最后一个先注册。

消息适配器(以及返回的ActorRef)具有与接收actor相同的生命周期。建议在顶级的Behaviors.setup中注册适配器。AbstractBehavior的设置或构造函数，但如果需要，可以稍后注册它们。

适配器函数在接收actor中运行，可以安全地访问它的状态，但是如果它抛出异常，actor将停止。

有用的时候:

* 在不同的actor消息协议之间进行转换
* 订阅将向bac发送许多响应消息的Actor

存在问题：

* 很难检测到没有发送或处理消息请求(参见ask)
* 每个响应消息类型只能进行一种适应，如果注册了新的，旧的就会被替换，例如，如果不同的目标Actor使用相同的响应类型，它们就不能有不同的适应，除非在消息中编码了某种相关性
* 除非协议已经包含了提供上下文的方法，例如在响应中也发送的请求id，否则不可能在不引入新的、单独的Actor的情况下将交互绑定到某个特定的上下文。

### 4.4.5. 两个Actor请求-响应之间使用ask

在请求和响应之间存在1:1映射的交互中，我们可以在ActorContext上使用ask来与另一个Actor交互。

交互有两个步骤，首先我们需要构造传出消息，为此我们需要一个ActorRef<Response>作为传出消息中的接收方。第二步是将成功响应或失败转换为消息，该消息是发送参与者协议的一部分。还请参阅[通用响应包装器](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#generic-response-wrapper)，了解成功或错误的响应。

Example:

![20200805212908](https://liulv.work/images/img/20200805212908.png)

```java
public class Hal extends AbstractBehavior<Hal.Command> {

  public static Behavior<Hal.Command> create() {
    return Behaviors.setup(Hal::new);
  }

  private Hal(ActorContext<Command> context) {
    super(context);
  }

  public interface Command {}

  public static final class OpenThePodBayDoorsPlease implements Command {
    public final ActorRef<HalResponse> respondTo;

    public OpenThePodBayDoorsPlease(ActorRef<HalResponse> respondTo) {
      this.respondTo = respondTo;
    }
  }

  public static final class HalResponse {
    public final String message;

    public HalResponse(String message) {
      this.message = message;
    }
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
        .build();
  }

  private Behavior<Command> onOpenThePodBayDoorsPlease(OpenThePodBayDoorsPlease message) {
    message.respondTo.tell(new HalResponse("I'm sorry, Dave. I'm afraid I can't do that."));
    return this;
  }
}

public class Dave extends AbstractBehavior<Dave.Command> {

  public interface Command {}

  // this is a part of the protocol that is internal to the actor itself
  private static final class AdaptedResponse implements Command {
    public final String message;

    public AdaptedResponse(String message) {
      this.message = message;
    }
  }

  public static Behavior<Command> create(ActorRef<Hal.Command> hal) {
    return Behaviors.setup(context -> new Dave(context, hal));
  }

  private Dave(ActorContext<Command> context, ActorRef<Hal.Command> hal) {
    super(context);

    // asking someone requires a timeout, if the timeout hits without response
    // the ask is failed with a TimeoutException
    final Duration timeout = Duration.ofSeconds(3);

    context.ask(
        Hal.HalResponse.class,
        hal,
        timeout,
        // construct the outgoing message
        (ActorRef<Hal.HalResponse> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
        // adapt the response (or failure to respond)
        (response, throwable) -> {
          if (response != null) {
            return new AdaptedResponse(response.message);
          } else {
            return new AdaptedResponse("Request failed");
          }
        });

    // we can also tie in request context into an interaction, it is safe to look at
    // actor internal state from the transformation function, but remember that it may have
    // changed at the time the response arrives and the transformation is done, best is to
    // use immutable state we have closed over like here.
    final int requestId = 1;
    context.ask(
        Hal.HalResponse.class,
        hal,
        timeout,
        // construct the outgoing message
        (ActorRef<Hal.HalResponse> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
        // adapt the response (or failure to respond)
        (response, throwable) -> {
          if (response != null) {
            return new AdaptedResponse(requestId + ": " + response.message);
          } else {
            return new AdaptedResponse(requestId + ": Request failed");
          }
        });
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        // the adapted message ends up being processed like any other
        // message sent to the actor
        .onMessage(AdaptedResponse.class, this::onAdaptedResponse)
        .build();
  }

  private Behavior<Command> onAdaptedResponse(AdaptedResponse response) {
    getContext().getLog().info("Got response from HAL: {}", response.message);
    return this;
  }
}
```

响应适应函数在接收actor中运行，可以安全地访问它的状态，但是如果它抛出异常，actor将停止。

有用的时候:

* 单响应查询
* 在继续之前，参与者需要知道消息已被处理
* 如果没有产生及时的响应，允许参与者重新发送
* 跟踪未完成的请求，而不是用消息淹没收件人(“backpressure”)
* 上下文应该附加到交互中，但是协议不支持(请求id，响应的是什么查询)

存在问题:

* 对一个请求只能有一个单一的响应(参见[每个会话child Actor](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#per-session-child-actor))
* 当请求超时时，接收方并不知道，可能仍然处理它直到完成，甚至在事后开始处理它
* 为超时寻找一个合适的值，特别是当在接收actor中被链接的ask触发器请求时。您希望有一个短的超时时间来响应请求程序，但同时又不希望出现许多误报。

### 4.4.6. 来着外部Actor的请求-响应ask

有时你需要与Actor的Actor从外部系统交互,这可以用“即发即弃”如上所述或通过另一个版本的要求返回一个CompletionStage <反应>是完成一个成功或失败的响应TimeoutException指定的超时时间内如果没有响应。

为此，我们使用akka.actor.type.javadsl.askpattern.ask。请求发送一条消息给一个参与者并获得一个CompletionState[Response] 返回。

Example:

![20200805213553](https://liulv.work/images/img/20200805213553.png)

```java
public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

  interface Command {}

  public static class GiveMeCookies implements Command {
    public final int count;
    public final ActorRef<Reply> replyTo;

    public GiveMeCookies(int count, ActorRef<Reply> replyTo) {
      this.count = count;
      this.replyTo = replyTo;
    }
  }

  interface Reply {}

  public static class Cookies implements Reply {
    public final int count;

    public Cookies(int count) {
      this.count = count;
    }
  }

  public static class InvalidRequest implements Reply {
    public final String reason;

    public InvalidRequest(String reason) {
      this.reason = reason;
    }
  }

  public static Behavior<Command> create() {
    return Behaviors.setup(CookieFabric::new);
  }

  private CookieFabric(ActorContext<Command> context) {
    super(context);
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder().onMessage(GiveMeCookies.class, this::onGiveMeCookies).build();
  }

  private Behavior<Command> onGiveMeCookies(GiveMeCookies request) {
    if (request.count >= 5) request.replyTo.tell(new InvalidRequest("Too many cookies."));
    else request.replyTo.tell(new Cookies(request.count));

    return this;
  }
}

  public void askAndPrint(
      ActorSystem<Void> system, ActorRef<CookieFabric.Command> cookieFabric) {
    CompletionStage<CookieFabric.Reply> result =
        AskPattern.ask(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
            // asking someone requires a timeout and a scheduler, if the timeout hits without
            // response the ask is failed with a TimeoutException
            Duration.ofSeconds(3),
            system.scheduler());

    result.whenComplete(
        (reply, failure) -> {
          if (reply instanceof CookieFabric.Cookies)
            System.out.println("Yay, " + ((CookieFabric.Cookies) reply).count + " cookies!");
          else if (reply instanceof CookieFabric.InvalidRequest)
            System.out.println(
                "No cookies for me. " + ((CookieFabric.InvalidRequest) reply).reason);
          else System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }
```

注意，验证错误在消息协议中也是显式的。GiveMeCookies请求可以用cookie或InvalidRequest进行响应。请求者必须决定如何处理InvalidRequest应答。有时应该将其视为失败的未来，因此可以将应答映射到请求者端。还请参阅[通用响应包装器](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#generic-response-wrapper)，了解成功或错误的响应。

```java
CompletionStage<CookieFabric.Reply> result =
    AskPattern.ask(
        cookieFabric,
        replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
        Duration.ofSeconds(3),
        system.scheduler());

CompletionStage<CookieFabric.Cookies> cookies =
    result.thenCompose(
        (CookieFabric.Reply reply) -> {
          if (reply instanceof CookieFabric.Cookies) {
            return CompletableFuture.completedFuture((CookieFabric.Cookies) reply);
          } else if (reply instanceof CookieFabric.InvalidRequest) {
            CompletableFuture<CookieFabric.Cookies> failed = new CompletableFuture<>();
            failed.completeExceptionally(
                new IllegalArgumentException(((CookieFabric.InvalidRequest) reply).reason));
            return failed;
          } else {
            throw new IllegalStateException("Unexpected reply: " + reply.getClass());
          }
        });

cookies.whenComplete(
    (cookiesReply, failure) -> {
      if (cookies != null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
      else System.out.println("Boo! didn't get cookies in time. " + failure);
    });
```

有用的时候:

从参与者系统外部查询参与者

存在问题：

* 在返回的CompletionStage上的回调很容易意外地关闭和不安全的可变状态，因为这些回调将在不同的线程上执行
* 对一个请求只能有一个单一的响应(参见[每个会话child Actor](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#per-session-child-actor))
* 当请求超时时，接收方并不知道，可能仍然处理它直到完成，甚至在事后开始处理它

### 4.4.7. 一般反应包装

在许多情况下，响应可以是一个成功的结果，也可以是一个错误(例如，命令无效的验证错误)。必须为每个请求类型定义两个响应类和一个共享超类型，这可能是重复的，特别是在集群上下文中，您还必须确保消息可以序列化以通过网络发送。

为了帮助解决这个问题，一个通用的状态-响应类型包括在Akka: StatusReply中，在任何地方都可以使用ask，还有第二个方法askWithStatus，假设响应是StatusReply，它将打开成功的响应并帮助处理验证错误。Akka包含该类型的预构建序列化器，因此在通常的用例中，集群应用程序只需要为成功的结果提供一个序列化器。

对于成功的应答不包含实际值但更多的是确认的情况，有一个预定义的StatusReply.ack()，其类型为StatusReply<Done>。

错误最好作为描述错误的文本发送，但也可以使用异常附加类型。

示例actor对actor ask:

```java
import akka.pattern.StatusReply;

public class Hal extends AbstractBehavior<Hal.Command> {

  public static Behavior<Hal.Command> create() {
    return Behaviors.setup(Hal::new);
  }

  private Hal(ActorContext<Hal.Command> context) {
    super(context);
  }

  public interface Command {}

  public static final class OpenThePodBayDoorsPlease implements Hal.Command {
    public final ActorRef<StatusReply<String>> respondTo;

    public OpenThePodBayDoorsPlease(ActorRef<StatusReply<String>> respondTo) {
      this.respondTo = respondTo;
    }
  }

  @Override
  public Receive<Hal.Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(Hal.OpenThePodBayDoorsPlease.class, this::onOpenThePodBayDoorsPlease)
        .build();
  }

  private Behavior<Hal.Command> onOpenThePodBayDoorsPlease(
      Hal.OpenThePodBayDoorsPlease message) {
    message.respondTo.tell(StatusReply.error("I'm sorry, Dave. I'm afraid I can't do that."));
    return this;
  }
}

public class Dave extends AbstractBehavior<Dave.Command> {

  public interface Command {}

  // this is a part of the protocol that is internal to the actor itself
  private static final class AdaptedResponse implements Dave.Command {
    public final String message;

    public AdaptedResponse(String message) {
      this.message = message;
    }
  }

  public static Behavior<Dave.Command> create(ActorRef<Hal.Command> hal) {
    return Behaviors.setup(context -> new Dave(context, hal));
  }

  private Dave(ActorContext<Dave.Command> context, ActorRef<Hal.Command> hal) {
    super(context);

    // asking someone requires a timeout, if the timeout hits without response
    // the ask is failed with a TimeoutException
    final Duration timeout = Duration.ofSeconds(3);

    context.askWithStatus(
        String.class,
        hal,
        timeout,
        // construct the outgoing message
        (ActorRef<StatusReply<String>> ref) -> new Hal.OpenThePodBayDoorsPlease(ref),
        // adapt the response (or failure to respond)
        (response, throwable) -> {
          if (response != null) {
            // a ReponseWithStatus.success(m) is unwrapped and passed as response
            return new Dave.AdaptedResponse(response);
          } else {
            // a ResponseWithStatus.error will end up as a StatusReply.ErrorMessage()
            // exception here
            return new Dave.AdaptedResponse("Request failed: " + throwable.getMessage());
          }
        });
  }

  @Override
  public Receive<Dave.Command> createReceive() {
    return newReceiveBuilder()
        // the adapted message ends up being processed like any other
        // message sent to the actor
        .onMessage(Dave.AdaptedResponse.class, this::onAdaptedResponse)
        .build();
  }

  private Behavior<Dave.Command> onAdaptedResponse(Dave.AdaptedResponse response) {
    getContext().getLog().info("Got response from HAL: {}", response.message);
    return this;
  }
}
```

对于消息适配器，验证错误将转化为失败。在本例中，我们将显式地分别处理valdation错误和其他ask失败。

示例从外部ask

```java
public class CookieFabric extends AbstractBehavior<CookieFabric.Command> {

  interface Command {}

  public static class GiveMeCookies implements CookieFabric.Command {
    public final int count;
    public final ActorRef<StatusReply<CookieFabric.Cookies>> replyTo;

    public GiveMeCookies(int count, ActorRef<StatusReply<CookieFabric.Cookies>> replyTo) {
      this.count = count;
      this.replyTo = replyTo;
    }
  }

  public static class Cookies {
    public final int count;

    public Cookies(int count) {
      this.count = count;
    }
  }

  public static Behavior<CookieFabric.Command> create() {
    return Behaviors.setup(CookieFabric::new);
  }

  private CookieFabric(ActorContext<CookieFabric.Command> context) {
    super(context);
  }

  @Override
  public Receive<CookieFabric.Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(CookieFabric.GiveMeCookies.class, this::onGiveMeCookies)
        .build();
  }

  private Behavior<CookieFabric.Command> onGiveMeCookies(CookieFabric.GiveMeCookies request) {
    if (request.count >= 5) request.replyTo.tell(StatusReply.error("Too many cookies."));
    else request.replyTo.tell(StatusReply.success(new CookieFabric.Cookies(request.count)));

    return this;
  }
}

  public void askAndPrint(
      ActorSystem<Void> system, ActorRef<CookieFabric.Command> cookieFabric) {
    CompletionStage<CookieFabric.Cookies> result =
        AskPattern.askWithStatus(
            cookieFabric,
            replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
            // asking someone requires a timeout and a scheduler, if the timeout hits without
            // response the ask is failed with a TimeoutException
            Duration.ofSeconds(3),
            system.scheduler());

    result.whenComplete(
        (reply, failure) -> {
          if (reply != null) System.out.println("Yay, " + reply.count + " cookies!");
          else if (failure instanceof StatusReply.ErrorMessage)
            System.out.println("No cookies for me. " + failure.getMessage());
          else System.out.println("Boo! didn't get cookies in time. " + failure);
        });
  }
```

注意，验证错误在消息协议中也是显式的，但被编码为包装类型，使用StatusReply.error(text)构造:

```java
CompletionStage<CookieFabric.Cookies> cookies =
    AskPattern.askWithStatus(
        cookieFabric,
        replyTo -> new CookieFabric.GiveMeCookies(3, replyTo),
        Duration.ofSeconds(3),
        system.scheduler());

cookies.whenComplete(
    (cookiesReply, failure) -> {
      if (cookies != null) System.out.println("Yay, " + cookiesReply.count + " cookies!");
      else System.out.println("Boo! didn't get cookies in time. " + failure);
    });
```

### 4.4.8. 忽略回复

在某些情况下，参与者对特定请求消息有响应，但您对响应不感兴趣。在这种情况下，您可以传递system.ignoreRef()，将请求-响应转换为“发射-忘记”。

顾名思义，system.ignoreRef()返回一个忽略发送给它的任何消息的ActorRef。

对于与上面的请求响应相同的协议，如果发送方希望忽略应答，它可以为replyTo传递system.ignoreRef()，它可以通过ActorContext.getSystem().ignoreref()访问该应答。

```java
cookieFabric.tell(
    new CookieFabric.Request("don't send cookies back", context.getSystem().ignoreRef()));
```

有用的时候:

发送协议为其定义应答的消息，但您对获取应答不感兴趣

存在问题：

返回的ActorRef会忽略发送给它的所有消息，因此应该谨慎使用。

* 将其随意传递，就像它是一个正常的ActorRef一样，可能会导致破坏角色到角色的交互。
* 当从Actor系统外部执行请求时使用它将导致请求返回的CompletionStage超时，因为它永远不会完成。
* 最后，观看它是合法的，但由于它是一种特殊的类型，它永远不会终止，因此你永远不会从它那里接收到终止信号。

### 4.4.9. 将未来的结果发送给自己

当使用从actor返回CompletionStage的API时，通常您会希望在CompletionStage完成时使用actor中响应的值。为此，ActorContext提供了一个pipeToSelf方法。

Example:

![20200805214503](https://liulv.work/images/img/20200805214503.png)

一个参与者CustomerRepository正在调用CustomerDataAccess上的一个方法，该方法返回一个CompletionStage。

```java
public interface CustomerDataAccess {
  CompletionStage<Done> update(Customer customer);
}

public class Customer {
  public final String id;
  public final long version;
  public final String name;
  public final String address;

  public Customer(String id, long version, String name, String address) {
    this.id = id;
    this.version = version;
    this.name = name;
    this.address = address;
  }
}

public class CustomerRepository extends AbstractBehavior<CustomerRepository.Command> {

  private static final int MAX_OPERATIONS_IN_PROGRESS = 10;

  interface Command {}

  public static class Update implements Command {
    public final Customer customer;
    public final ActorRef<OperationResult> replyTo;

    public Update(Customer customer, ActorRef<OperationResult> replyTo) {
      this.customer = customer;
      this.replyTo = replyTo;
    }
  }

  interface OperationResult {}

  public static class UpdateSuccess implements OperationResult {
    public final String id;

    public UpdateSuccess(String id) {
      this.id = id;
    }
  }

  public static class UpdateFailure implements OperationResult {
    public final String id;
    public final String reason;

    public UpdateFailure(String id, String reason) {
      this.id = id;
      this.reason = reason;
    }
  }

  private static class WrappedUpdateResult implements Command {
    public final OperationResult result;
    public final ActorRef<OperationResult> replyTo;

    private WrappedUpdateResult(OperationResult result, ActorRef<OperationResult> replyTo) {
      this.result = result;
      this.replyTo = replyTo;
    }
  }

  public static Behavior<Command> create(CustomerDataAccess dataAccess) {
    return Behaviors.setup(context -> new CustomerRepository(context, dataAccess));
  }

  private final CustomerDataAccess dataAccess;
  private int operationsInProgress = 0;

  private CustomerRepository(ActorContext<Command> context, CustomerDataAccess dataAccess) {
    super(context);
    this.dataAccess = dataAccess;
  }

  @Override
  public Receive<Command> createReceive() {
    return newReceiveBuilder()
        .onMessage(Update.class, this::onUpdate)
        .onMessage(WrappedUpdateResult.class, this::onUpdateResult)
        .build();
  }

  private Behavior<Command> onUpdate(Update command) {
    if (operationsInProgress == MAX_OPERATIONS_IN_PROGRESS) {
      command.replyTo.tell(
          new UpdateFailure(
              command.customer.id,
              "Max " + MAX_OPERATIONS_IN_PROGRESS + " concurrent operations supported"));
    } else {
      // increase operationsInProgress counter
      operationsInProgress++;
      CompletionStage<Done> futureResult = dataAccess.update(command.customer);
      getContext()
          .pipeToSelf(
              futureResult,
              (ok, exc) -> {
                if (exc == null)
                  return new WrappedUpdateResult(
                      new UpdateSuccess(command.customer.id), command.replyTo);
                else
                  return new WrappedUpdateResult(
                      new UpdateFailure(command.customer.id, exc.getMessage()),
                      command.replyTo);
              });
    }
    return this;
  }

  private Behavior<Command> onUpdateResult(WrappedUpdateResult wrapped) {
    // decrease operationsInProgress counter
    operationsInProgress--;
    // send result to original requestor
    wrapped.replyTo.tell(wrapped.result);
    return this;
  }
}
```

在CompletionStage上使用回调可能很诱人，但是这会带来从外部线程访问不是线程安全的actor的内部状态的风险。例如，上面例子中的numberOfPendingOperations计数器不能从这样的回调访问。因此，最好将结果映射到消息并在接收该消息时执行进一步处理。

有用的时候:
* 访问从参与者返回CompletionStage的api，例如数据库或外部服务
* 当CompletionStage完成时，actor需要继续处理
* 保持上下文与原始请求的关系，并在CompletionStage完成时使用它，例如replyTo actor引用

问题:
为结果添加包装器消息的样板文件。

### 4.4.10. 每个子Actor会话

在某些情况下，只有在从其他参与者收集多个答案之后，才能创建并发回对请求的完整响应。对于这些类型的交互，最好将工作委托给每个“会话”的儿童演员。子节点还可以包含实现重试、超时失败、尾部截断、进度检查等的任意逻辑。

请注意，这本质上就是ask的实现方式，如果您所需要的只是一个带有超时的响应，那么最好使用ask。

子元素是用它执行工作所需的上下文创建的，包括它可以响应的ActorRef。当完整的结果出现时，孩子会以结果做出反应，然后停止。

由于会话参与者的协议不是公共API，而是父参与者的实现细节，因此使用显式协议并调整会话参与者与之交互的参与者的消息可能并不总是有意义的。对于这个用例，可以表示参与者可以接收任何消息(对象)。

Example:

![20200805214912](https://liulv.work/images/img/20200805214912.png)

```java
// dummy data types just for this sample
public class Keys {}

public class Wallet {}

public class KeyCabinet {
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

public class Drawer {

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

public class Home {

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
```

在实际的会话子节点中，您可能还希望包括某种形式的超时(请参阅[将消息调度到self](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#scheduling-messages-to-self))。

有用的时候:
* 在构建结果之前，单个传入请求应该与其他参与者进行多次交互，例如多个结果的聚合
* 您需要处理确认和重试消息，以便至少传递一次

问题:
* 必须对子节点的生命周期进行管理，以避免造成资源泄漏，因此很容易忽略未停止会话参与者的场景
* 它增加了复杂性，因为每个这样的子节点都可以与其他子节点和父节点并发执行

### 4.4.11. 通用响应聚合器






# 5. Remote模块

## 5.1. Remote简介

Remoting使生活在不同计算机上的Actor能够无缝地交换消息。当作为JAR工件分发时，Remoting更像是一个模块而不是一个库。您主要通过配置来启用它，它只有几个api。由于有了Actor模型，远程和本地消息发送看起来完全相同。您在本地系统上使用的模式可以直接转换为远程系统。您很少需要直接使用Remoting，但是它提供了构建集群子系统的基础。

Remoting 解决的挑战包括:

* 如何寻址驻留在远程主机上的actor系统。
* 如何在远程Actor系统上处理单个Actor。
* 如何在网络上将消息转换成字节。
* 如何管理主机之间的低级网络连接(和重连接)，检测崩溃的Actor系统和主机，所有这些都是透明的。
* 如何在同一网络连接上透明地多路传输来自一组不相关的Actor的通信。

## 5.2. Remote Maven库

```xml
<properties>
  <akka.version>2.6.8</akka.version>
  <scala.binary.version>2.13</scala.binary.version>
</properties>
<dependency>
  <groupId>com.typesafe.akka</groupId>
  <artifactId>akka-remote_${scala.binary.version}</artifactId>
  <version>${akka.version}</version>
</dependency>
```








