package com.tcfuture.akka.actor.example.iot;

import akka.actor.typed.ActorSystem;

/**
 * @author liulv
 *
 * 用例所需的主要功能，在一个完整的监控家庭温度的物联网系统中，将设备传感器连接到我们的系统的步骤可能如下:
 * 1. 家中的传感器设备通过某种协议进行连接。
 * 2. 管理网络连接的组件接受该连接。
 * 3. 传感器提供它的组和设备ID，以便向系统的设备管理器组件注册。
 * 4. 设备管理器（device manager）组件通过查找或创建负责保持传感器状态的参与者来处理注册。
 * 5. actor用一个确认进行响应，公开它的ActorRef。
 * 6. 网络组件现在使用ActorRef在传感器和设备参与者之间进行通信，而不需要通过设备管理器。
 *
 * 1. 2 步不在示例范围，我们将开始处理步骤3-6，并创建一种方法，让传感器与我们的系统注册，并与actor通信。但首先，我们有另一个架构决策——我们应该
 * 使用多少个角色级别来表示设备组和设备传感器?
 *
 *Akka程序员面临的主要设计挑战之一是为actor选择最佳的粒度。在实践中，根据actor之间交互的特征，通常有几种有效的方法来组织一个系统。例如，在我们
 * 的用例中，可以让单个actor维护所有组和设备——可能使用散列映射。为每个组设置一个actor来跟踪同一home中的所有设备的状态也是合理的。
 *
 * 一般来说，选择更大的粒度。引入比所需更多的细粒度actor会导致比解决的问题更多的问题。
 * 在系统需要时添加更细的粒度:
 * 1. 更高的并发性。
 * 2. 有许多状态的actor之间的复杂对话。我们将在下一章中看到一个很好的例子。
 * 3. 充分说明划分为较小的actor是有意义的。
 * 4. 多个不相关的责任。使用单独的参与者允许个体失败和恢复，而对其他人的影响很小
 */
public class IotMain {
    public static void main(String[] args) {
        // Create ActorSystem and top level supervisor
        ActorSystem.create(IotSupervisor.create(), "iot-system");
    }
}
