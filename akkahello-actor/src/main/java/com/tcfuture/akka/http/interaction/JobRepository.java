package com.tcfuture.akka.http.interaction;


import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author liulv
 * @since 1.0.0
 *
 * 首先，让我们开始定义，它将充当构建作业信息的存储库。
 * 对于我们的示例，这不是严格需要的，而仅仅是让一个实际的actor与之交互：Behavior
 */
public class JobRepository extends AbstractBehavior<JobRepository.Command> {

    /**
     * Job json对象
     */
    @AllArgsConstructor
    @JsonFormat
    @ToString
    public static final class Job {
        @JsonProperty("id")
        final Long id;
        @JsonProperty("project-name")
        final String projectName;
        @JsonProperty("status")
        final String status;
        @JsonProperty("duration")
        final Long duration;

        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Job(@JsonProperty("id") Long id, @JsonProperty("project-name") String projectName,
                   @JsonProperty("duration") Long duration) {
            this(id, projectName, "Success", duration);
        }
    }

    // 成功和失败响应
    interface Response {}

    /**
     * 成功响应OK
     */
    public static final class OK implements Response {
        private static OK INSTANCE = new OK();

        private OK() {}

        public static OK getInstance() {
            return INSTANCE;
        }
    }

    /**
     * 失败响应
     */
    @AllArgsConstructor
    public static final class KO implements Response {
        //失败原因
        final String reason;
    }

    // 可以发送到此行为的所有可能的消息接口
    interface Command {}

    /**
     * Add job 消息实例
     */
    @AllArgsConstructor
    public static final class AddJob implements Command {
        final Job job;
        final ActorRef<Response> replyTo;
    }

    /**
     * get job by id 消息实例
     */
    @AllArgsConstructor
    public static final class GetJobById implements Command {
        final Long id;
        final ActorRef<Optional<Job>> replyTo;
    }

    /**
     * 创建 job 实例
     */
    @AllArgsConstructor
    public static final class ClearJobs implements Command {
        final ActorRef<Response> replyTo;
    }

    /**
     * 创建 Behavior
     * @return Behavior<Command>
     */
    public static Behavior<Command> create() {
        return create(new HashMap<Long, Job>());
    }

    /**
     * 创建Behavior 方法
     * @param jobs Map<Long, Job>
     * @return Behavior<Command>
     */
    public static Behavior<Command> create(Map<Long, Job> jobs) {
        return Behaviors.setup(context -> new JobRepository(context, jobs));
    }

    /**
     * 所有的job: <jobId, Job>
     */
    private Map<Long, Job> jobs;

    /**
     * Actor 构造函数
     *
     * @param context ActorContext<Command>
     * @param jobs job的map集合
     */
    private JobRepository(ActorContext<Command> context, Map<Long, Job> jobs) {
        super(context);
        this.jobs = jobs;
    }

    // 该接收处理所有可能的传入消息并将状态job保留在actor中
    @Override
    public Receive<Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AddJob.class, this::addJob)
                .onMessage(GetJobById.class, this::getJobById)
                .onMessage(ClearJobs.class, this::clearJobs)
                .build();
    }

    /**
     * 处理AddJob消息， 如果job已经存在job集合中，响应job已经存在
     *
     * @param msg AddJob
     * @return
     */
    private Behavior<Command> addJob(AddJob msg) {
        long jobId = msg.job.id;
        getContext().getLog().info("actor 添加job {}", msg.job.toString());
        if (jobs.containsKey(jobId))
            msg.replyTo.tell(new KO("Job-" + jobId + " 已经存在"));
        else {
            jobs.put(jobId, msg.job);
            msg.replyTo.tell(OK.getInstance());
        }
        return Behaviors.same();
    }

    private Behavior<Command> getJobById(GetJobById msg) {
        getContext().getLog().info("actor 根据id-{}查询job ", msg.id);
        if (jobs.containsKey(msg.id)) {
            msg.replyTo.tell(Optional.of(jobs.get(msg.id)));
        } else {
            msg.replyTo.tell(Optional.empty());
        }
        return Behaviors.same();
    }

    private Behavior<Command> clearJobs(ClearJobs msg) {
        msg.replyTo.tell(OK.getInstance());
        jobs.clear();
        return Behaviors.same();
    }
}
