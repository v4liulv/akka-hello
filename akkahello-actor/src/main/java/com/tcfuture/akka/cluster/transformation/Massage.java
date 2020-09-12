package com.tcfuture.akka.cluster.transformation;

import akka.actor.typed.ActorRef;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.tcfuture.akka.cluster.CborSerializable;
import lombok.AllArgsConstructor;

import java.util.Set;

/**
 * @author liulv
 *
 * 前端消息 容器，前端需要处理的消息实例
 */
public class Massage {

    //消息超类
    interface Event {}

    /**
     * 钩子消息，用于触发重复循环定时处理
     */
     enum Tick implements Event {
        INSTANCE
    }

    /**
     * workers 更新消息
     * 存储Worker.TransformText的actor ref
     */
    @AllArgsConstructor
     static final class WorkersUpdated implements Event {
        //新的TransformText actor
        public final Set<ActorRef<TransformText>> newWorkers;
    }

    /**
     * 转换任务完成了，也可以理解为转换任务
     */
    @AllArgsConstructor
     static final class TransformCompleted implements Event {
        //原文本
        public final String originalText;
        //转换后文本，处理后的结果文本
        public final String transformedText;
    }

    /**
     * 任务失败
     */
    @AllArgsConstructor
     static final class JobFailed implements Event {
        //任务失败原因
        public final String why;
        //需要转换的文本
        public final String text;
    }

    /**
     * 消息类型超类-接口，worker所有的消息都要继承此接口
     */
    interface Command extends CborSerializable {}

    /**
     * worker需要接收的消息
     */
    @AllArgsConstructor
     static final class TransformText implements Command {
        public final String text;
        public final ActorRef<TextTransformed> replyTo;
    }

    /**
     * 转换后的返回的消息，处理后需要发送的消息
     */
     static final class TextTransformed implements Command {
        public final String text;

        //注意必须添加@JsonCreator不然会提示，未能反序列化来自的消息，Failed to deserialize message from
        //不能从对象值反序列化(没有基于委托或属性的创建者)
        @JsonCreator
        public TextTransformed(String text) {
            this.text = text;
        }
    }

}
