package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.tomakehurst.wiremock.admin.ConversionsTest.getTestServeEvent;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class LimitAndSinceDatePaginatorTest {

    @Test
    void filterRequestsTest() {
        QueryParameter exclude = new QueryParameter("exclude", List.of("health_check", "favicon.ico"));
        QueryParameter include = new QueryParameter("include", List.of("foo", "bar"));
        Predicate<ServeEvent> serveEventPredicate = Conversions.toPredicate(exclude, include);

        List<ServeEvent> serveEventList = new ArrayList<>();
        serveEventList.add(getTestServeEvent("/health_check"));
        serveEventList.add(getTestServeEvent("/health_check"));
        serveEventList.add(getTestServeEvent("/favicon.ico"));
        serveEventList.add(getTestServeEvent("/foo"));
        serveEventList.add(getTestServeEvent("/foo"));
        serveEventList.add(getTestServeEvent("/bar"));
        serveEventList.add(getTestServeEvent("/baz"));

        LimitAndSinceDatePaginator limitAndSinceDatePaginator = new LimitAndSinceDatePaginator(
                serveEventList,
                10,
                null,
                serveEventPredicate
        );

        assertEquals(3, limitAndSinceDatePaginator.getTotal());
        List<String> expectedUrls = List.of("/foo", "/foo", "/bar");
        List<String> actualUrls = limitAndSinceDatePaginator.select().stream()
                .map(ServeEvent::getRequest)
                .map(Request::getUrl)
                .toList();
        assertEquals(expectedUrls, actualUrls);
    }
}
