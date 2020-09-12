package com.tcfuture.akka.actor.interactionpatterns.future;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.concurrent.CompletionStage;

/**
 * @author liulv
 *
 * 当使用从actor返回CompletionStage的API时，通常您会希望在CompletionStage完成时使用actor中响应的值。为此，
 * ActorContext提供了一个pipeToSelf方法。
 *
 * 一个ActorCustomerRepository正在调用CustomerDataAccess上的一个方法，该方法返回一个CompletionStage。
 *
 * 1. update --> CustomerRepository
 * 2. CustomerRepository --update--> CustomerDataAccess
 * 3. CustomerDataAccess --future result--> []
 * 4. [] --pip result--> CustomerRepository
 * 5. CustomerRepository --OperationResult-->
 *
 * 在CompletionStage上使用回调可能很诱人，但是这会带来从外部线程访问不是线程安全的actor的内部状态的风险。例如，
 * 子中的numberOfPendingOperations计数器不能从这样的回调访问。因此，最好将结果映射到消息并在接收该消息时执行进一步处理。
 *
 * 使用场景：
 * 访问从Actor返回CompletionStage的api，例如数据库或外部服务
 * 当CompletionStage完成时，actor需要继续处理
 * 保持上下文与原始请求的关系，并在CompletionStage完成时使用它，例如replyTo actor引用
 */
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
            /**
             * 核心代码 Done类型，ActorContext.pipeToSelf
             */
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
