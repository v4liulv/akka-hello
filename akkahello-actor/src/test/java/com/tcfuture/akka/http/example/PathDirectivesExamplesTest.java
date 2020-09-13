package com.tcfuture.akka.http.example;

import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.testkit.JUnitRouteTest;
import org.junit.Test;

import static akka.http.javadsl.unmarshalling.StringUnmarshallers.LONG;

/**
 * @author liulv
 */
public class PathDirectivesExamplesTest extends JUnitRouteTest {

    /**
     * pathEnd测试
     */
    @Test
    public void testPathEnd() {
        //#path-end
        final Route route =
                concat(
                        pathPrefix("foo", () ->
                                concat(
                                        pathEnd(() -> complete("/foo")),
                                        //path("bar", () -> complete("/foo/bar"))
                                        get(() ->
                                                path(LONG,
                                                        jobId -> complete("sout" + jobId)))
                                )
                        )
                );

        // tests:
        //testRoute(route).run(HttpRequest.GET("/foo")).assertEntity("/foo");
        //testRoute(route).run(HttpRequest.GET("/foo/")).assertStatusCode(StatusCodes.NOT_FOUND);
        testRoute(route).run(HttpRequest.GET("/foo/123")).assertEntity("sout123");
        //#path-end
    }
}
