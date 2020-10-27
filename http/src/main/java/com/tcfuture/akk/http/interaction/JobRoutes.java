package com.tcfuture.akk.http.interaction;

//#route

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.unmarshalling.StringUnmarshallers.LONG;

/**
 * @author liulv
 * @since 1.0.0
 *
 * 定义与先前定义的Actor行为进行通信并处理其所有可能响应的Route.
 */
public class JobRoutes extends AllDirectives {
    private final ActorSystem<?> system;
    private final ActorRef<JobRepository.Command> buildJobRepository;

    public JobRoutes(ActorRef<JobRepository.Command> buildJobRepository, ActorSystem<?> system) {
        this.system = system;
        this.buildJobRepository = buildJobRepository;
    }

    private Route addOrDelete() {
        return concat(
                post(() ->
                        entity(Jackson.unmarshaller(JobRepository.Job.class), job ->
                                onSuccess(add(job), r -> complete("Job added"))
                        )),
                //删除
                delete(() -> onSuccess(deleteAll(), r -> complete("Jobs cleared")))
        );
    }

    /**
     * 根据Job构建AddJob消息请求Ask, Ask响应通过handleKO方法处理后返回
     *
     * @param job Job
     * @return CompletionStage CompletionStage<JobRepository.OK>
     */
    private CompletionStage<JobRepository.OK> add(JobRepository.Job job) {
        return handleKO(AskPattern.ask(
                buildJobRepository,
                replyTo -> new JobRepository.AddJob(job, replyTo),
                Duration.ofSeconds(3),
                system.scheduler()));
    }

    /**
     * 构建ClearJobs消息请求Ask, Ask响应通过handleKO方法处理后返回
     *
     * @return CompletionStage CompletionStage<JobRepository.OK>
     */
    private CompletionStage<JobRepository.OK> deleteAll() {
        return handleKO(AskPattern.ask(
                buildJobRepository,
                JobRepository.ClearJobs::new,
                Duration.ofSeconds(3),
                system.scheduler()));
    }

    /**
     * 构建Ask请求的路由Route
     *
     * @return Route
     */
    public Route jobRoutes() {
        return pathPrefix("jobs", () ->
                        concat(
                        pathEnd(this::addOrDelete), //子路由
                        get(() ->     //jobs/long路由
                                path(LONG, jobId ->
                                        onSuccess(getJob(jobId), jobOption -> {
                                            if (jobOption.isPresent()) {
                                                return complete(StatusCodes.OK, jobOption.get(), Jackson.<JobRepository.Job>marshaller());
                                            } else {
                                                return complete(StatusCodes.NOT_FOUND, "job-" + jobId +
                                                        "不存在");
                                            }
                                        })
                                )
                        )
                )
        );
    }

    /**
     * 根据JobId构建Actor Ask的请求
     *
     * @param jobId Job ID
     * @return CompletionStage<Optional<JobRepository.Job>>
     */
    private CompletionStage<Optional<JobRepository.Job>> getJob(Long jobId) {
        return AskPattern.ask(
                buildJobRepository,
                replyTo -> new JobRepository.GetJobById(jobId, replyTo),
                Duration.ofSeconds(3),
                system.scheduler());
    }

    /**
     *  处理Actor Ask请求的响应，异常处理失败原因或返回CompletionStage<JobRepository.OK>
     *
     * @param stage Actor Ask响应 CompletionStage<JobRepository.Response>
     * @return CompletionStage<JobRepository.OK>
     */
    private CompletionStage<JobRepository.OK> handleKO(CompletionStage<JobRepository.Response> stage) {
        return stage.thenApply(response -> {
            if (response instanceof JobRepository.OK) {
                return (JobRepository.OK)response;
            } else if (response instanceof JobRepository.KO) {
                throw new IllegalStateException(((JobRepository.KO) response).reason);
            } else {
                throw new IllegalStateException("Invalid response");
            }
        });
    }
}