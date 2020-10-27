package com.tcfuture.akka.cluster.statsclusterdy;

import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * @author liulv
 *
 * 构建actor之间的消息传递需要的类型接口和实例对象
 */
public class Message {
    /**
     * Worker 消息超类
     */
    public interface CommandWorker {}

    /**
     * 清除缓存
     */
    enum EvictCache implements CommandWorker {
        INSTANCE
    }

    /**
     * 待处理消息
     */
    @AllArgsConstructor
    static final class Process implements CommandWorker {
        public final String word;
        public final ActorRef<Processed> replyTo;
    }

    /**
     * 处理后回复消息
     */
    @AllArgsConstructor
    static final class Processed implements CommandWorker {
        public final String word;
        public final int length;
    }

    /**
     * Service 消息超类
     */
    interface CommandService {}

    /**
     * Service消息-处理文本
     */
    @AllArgsConstructor
    static final class ProcessText implements CommandService {
        public final String text;
        public final ActorRef<Response> replyTo;
    }

    /**
     * workers 更新消息
     * 存储Worker.TransformText的actor ref
     */
    @AllArgsConstructor
    static final class WorkersUpdated implements CommandService {
        //新的TransformText actor
        public final Set<ActorRef<Process>> newWorkers;
    }

    /**
     * Service消息- 停止
     */
    public enum Stop implements CommandService {
        INSTANCE
    }

    /**
     *  Service消息超类-响应
     */
    interface Response { }

    /**
     * Service 响应消息- 工作结果
     */
    public static final class JobResult implements Response {
        //平均字长
        public final double meanWordLength;

        @JsonCreator
        public JobResult(double meanWordLength) {
            this.meanWordLength = meanWordLength;
        }

        @Override
        public String toString() {
            return "JobResult{" +
                    "meanWordLength=" + meanWordLength +
                    '}';
        }
    }

    /**
     * job失败
     */
    public static final class JobFailed implements Response {
        public final String reason;

        @JsonCreator
        public JobFailed(String reason) {
            this.reason = reason;
        }

        public String toString() {
            return "JobFailed{" +
                    "reason='" + reason + '\'' +
                    '}';
        }
    }

    /**
     * client消息超类
     */
    interface Event {}

    enum Timeout implements Event {
        INSTANCE
    }

    /**
     * 定时消息
     */
    enum Tick implements Event {
        INSTANCE
    }

    /**
     * 计算完成
     */
    @AllArgsConstructor
    static class CalculationComplete implements Event {
        public final int length;
    }

    /**
     *
     */
    static class ServiceResponse implements Event {
        public final Response result;
        public ServiceResponse(Response result) {
            this.result = result;
        }
    }
}
