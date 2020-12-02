package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.HttpHeaders;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.matching.MockMultipart;
import com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.core.Options.FileIdMethod.*;
import static com.github.tomakehurst.wiremock.http.HttpHeader.httpHeader;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.http.Response.response;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

/***
 * We'll test with a series of requests that are different in some small regard; each request
 * should generate a new hash, different than any hash seen previously.  (requestTest)
 *
 * We'll test a series of different responses in the same manner.  (responseTest)
 *
 * We'll get hashes multiple times, against a new (but created in the same way) list of requests and responses
 * and verify that the hashes produced are consistent.  (consistentHashesTest)
 *
 * In REQUEST_HASH mode, with a constant request, the hash produced is the same as long as the request
 * doesn't change (responseTest), in RESPONSE_HASH mode the hash produced is the same as long as the response
 * doesn't change (requestTest), and in REQUEST_RESPONSE_HASH the hash changes when either changes (requestResponseTest).
 */
public class HashIdGeneratorTest {

    private Mockery context;
    private Request requestForResponseTest;

    @Before
    public void init() {
        context = new Mockery();
        requestForResponseTest = new MockRequestBuilder(context, "Request for response test")
                .withMethod(GET)
                .withUrl("/test/response").build();
    }

    List<Function<MockRequestBuilder, String>> requestBuilders = List.of(
            (builder) -> {
                builder
                        .withMethod(GET)
                        .withUrl("/test/api");
                return "88e04074b5bb2477";
            },
            (builder) -> {
                builder
                        .withMethod(POST);
                return "f0daf32057f5784d";
            },
            (builder) -> {
                builder
                        .withUrl("/test/api2");
                return "d9ccd2a80b7b30bb";
            },
            (builder) -> {
                builder
                        .withBody("k1=v1&k2=v2");
                return "4420e6eb18094702";
            },
            (builder) -> {
                builder
                        .withHeader("Accept-Content", "text/plain");
                return "08484c66aac5d890";
            },
            (builder) -> {
                builder
                        .withHeader("Accept-Content", "text/xml");
                return "e7ef0864f16d9c24";
            },
            (builder) -> {
                builder
                        .withCookie("Color-Scheme", "blue");
                return "ae7f7f1435bdc6e3";
            },
            (builder) -> {
                Collection<Request.Part> parts = new ArrayList<>();
                MockMultipart part = new MockMultipart().name("favorite-cat-poem.txt").body("There was a cat who...");
                part.header("Content-Disposition", "attachment");
                parts.add(part);
                builder.withMultiparts(parts);
                return "54367eb484beb336";
            }
    );

    List<Function<Response.Builder, String>> responseBuilders = List.of(
            (builder) -> {
                builder
                        .status(200);
                return "d5859a17f3b32b3e";
            },
            (builder) -> {
                builder
                        .statusMessage("OK");
                return "9362a550c8dbcb91";
            },
            (builder) -> {
                builder
                        .body("Lorem Ipsum");
                return "dcd68c9f636a0ac8";
            },
            (builder) -> {
                builder
                        .body("Lorem Ipsum -- what the heck is that?");
                return "08262f0ce16902de";
            },
            (builder) -> {
                builder
                        .headers(new HttpHeaders(
                                httpHeader("Content-Type", "text/plain"),
                                httpHeader("Cache-Control", "no-cache")));
                return "5678bb9c965729e6";
            },
            (builder) -> {
                builder
                        .status(404)
                        .statusMessage("Not found")
                        .body("We couldn't find the thing you were looking for.");
                return "7175ce7acd0b75f3";
            }
    );

    static int itemCount = 0;
    static <BUILDER, ITEM_TYPE> Map<ITEM_TYPE, String> build(Function<Integer, BUILDER> builderFactory, Function<BUILDER, ITEM_TYPE> converter, List<Function<BUILDER, String>> builders) {
        Map<ITEM_TYPE, String> items = new HashMap<>();
        for (int countToApply = 1; countToApply <= builders.size(); countToApply++) {
            BUILDER builder = builderFactory.apply(itemCount);
            itemCount++;
            String expectedId = "";
            // Each request/response builds on the one before with one additional change.
            for (int i = 0; i < countToApply; i++) {
                expectedId = builders.get(i).apply(builder);
            }
            items.put(converter.apply(builder), expectedId);
        }
        return items;
    }

    @Test
    public void requestTest() {
        final IdGenerator generator = new HashIdGenerator(REQUEST_HASH);
        final IdGenerator responseHashGenerator = new HashIdGenerator(RESPONSE_HASH);

        Response response = response()
                .status(200)
                .body("Lorem Ipsum")
                .headers(new HttpHeaders(
                        httpHeader("Content-Type", "text/plain"),
                        httpHeader("Cache-Control", "no-cache")))
                .build();

        Map<Request, String> requests = build(
                (count) -> new MockRequestBuilder(context, "Request #"+ count),
                (builder) -> builder.build(),
                requestBuilders);
        Set<String> uniqueHashes = new HashSet<>();
        Set<String> responseHashes = new HashSet<>();

        for (Map.Entry<Request,String> entry : requests.entrySet()) {
            Request request = entry.getKey();
            final String id = generator.generate(request, response, response.getBody());
            assertThat("Request with expected hash " + entry.getValue() + " produced the wrong hash", id, equalTo(entry.getValue()));

            int sizeBefore = uniqueHashes.size();
            uniqueHashes.add(id);
            int sizeAfter = uniqueHashes.size();
            assertThat("Each request should generate a new hash", sizeAfter, equalTo(sizeBefore + 1));

            final String responseHashId = responseHashGenerator.generate(request, response, response.getBody());
            assertThat(responseHashId, not(equalTo(id)));
            responseHashes.add(responseHashId);

            // The same response is used each time so there should only be one hash returned by hash generator
            // with mode = RESPONSE_HASH.
            assertThat(responseHashes.size(), equalTo(1));
        }
    }

    @Test
    public void responseTest() {
        final IdGenerator generator = new HashIdGenerator(RESPONSE_HASH);
        final IdGenerator requestHashGenerator = new HashIdGenerator(REQUEST_HASH);

        Map<Response, String> responses = build(
                (count) -> response(),
                (builder) -> builder.build(),
                responseBuilders);
        Set<String> uniqueHashes = new HashSet<>();
        Set<String> requestHashes = new HashSet<>();

        for (Map.Entry<Response,String> entry : responses.entrySet()) {
            Response response = entry.getKey();
            final String id = generator.generate(requestForResponseTest, response, response.getBody());
            assertThat("Response with expected hash " + entry.getValue() + " produced the wrong hash", id, equalTo(entry.getValue()));

            int sizeBefore = uniqueHashes.size();
            uniqueHashes.add(id);
            int sizeAfter = uniqueHashes.size();
            assertThat("Each response should generate a new hash", sizeAfter, equalTo(sizeBefore + 1));

            final String requestHashId = requestHashGenerator.generate(requestForResponseTest, response, response.getBody());
            assertThat(requestHashId, not(equalTo(id)));
            requestHashes.add(requestHashId);

            // The same request is used each time so there should only be one hash returned by hash generator
            // with mode = REQUEST_HASH.
            assertThat(requestHashes.size(), equalTo(1));
        }
    }

    @Test
    public void consistentHashesTest() {
        // Run the same tests as before a number of times and validate hashes don't change.
        for (int i = 0; i < 10; i++) {
            requestTest();
            responseTest();
        }
    }

    @Test
    public void requestResponseTest() {
        Map<Request, String> requests = build(
                (count) -> new MockRequestBuilder(context, "Request #"+ count),
                (builder) -> builder.build(),
                requestBuilders);
        Map<Response, String> responses = build(
                (count) -> response(),
                (builder) -> builder.build(),
                responseBuilders);
        Set<String> uniqueHashes = new HashSet<>();

        final IdGenerator generator = new HashIdGenerator(REQUEST_RESPONSE_HASH);

        for (Map.Entry<Request,String> requestEntry : requests.entrySet()) {
            Request request = requestEntry.getKey();
            for (Map.Entry<Response,String> responseEntry : responses.entrySet()) {
                Response response = responseEntry.getKey();
                final String id = generator.generate(request, response, response.getBody());
                int sizeBefore = uniqueHashes.size();
                uniqueHashes.add(id);
                int sizeAfter = uniqueHashes.size();
                assertThat("Each request/response combination should generate a new hash", sizeAfter, equalTo(sizeBefore + 1));
            }
        }
        assertThat(uniqueHashes.size(), equalTo(requests.size() * responses.size()));
    }

}
