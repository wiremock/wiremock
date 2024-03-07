package benchmarks;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.testsupport.WireMockResponse;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 2)
@Fork(1)
@Measurement(iterations = 5)
public class JsonPathMatchingBenchmark {

    public static final String[] TOPICS = { "topic-one", "longer-topic-2", "very-long-topic-3", "four", "five55555555" };

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        private WireMockServer wm;
        private WireMockTestClient client;

        @Setup
        public void setup() {
            wm = new WireMockServer(wireMockConfig().dynamicPort());
            wm.start();
            client = new WireMockTestClient(wm.port());

            for (String topic: TOPICS) {
                wm.stubFor(post("/things")
                        .withRequestBody(matchingJsonPath("$.[?(@.topic == '" + topic + "')]")));
            }
        }

        @TearDown
        public void tearDown() {
            wm.stop();
        }
    }

    @Benchmark
    public boolean matched(BenchmarkState state) {
        final WireMockResponse response = state.client.postJson("/things", String.format(JSON_TEMPLATE, "very-long-topic-3"));
        return response.statusCode() == 200;
    }

    public static void main(String[] args) throws Exception {
        Options opt = new OptionsBuilder()
                .include(JsonPathMatchingBenchmark.class.getSimpleName())
                .warmupIterations(2)
                .forks(1)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }

    static final String JSON_TEMPLATE = "[\n" +
            "  {\"topic\": \"A\", \"name\": \"John Doe\", \"age\": 30, \"occupation\": \"Engineer\", \"city\": \"New York\", \"interests\": [\"hiking\", \"reading\", \"cooking\"], \"email\": \"john.doe@example.com\", \"phone\": \"+1234567890\", \"address\": \"123 Main St\", \"member_since\": \"2020-01-15\"},\n" +
            "  {\"topic\": \"B\", \"name\": \"Alice Smith\", \"age\": 25, \"occupation\": \"Graphic Designer\", \"city\": \"Los Angeles\", \"interests\": [\"painting\", \"yoga\", \"traveling\"], \"email\": \"alice.smith@example.com\", \"phone\": \"+1987654321\", \"address\": \"456 Elm St\", \"member_since\": \"2018-07-20\"},\n" +
            "  {\"topic\": \"C\", \"name\": \"Michael Johnson\", \"age\": 35, \"occupation\": \"Doctor\", \"city\": \"Chicago\", \"interests\": [\"running\", \"photography\", \"music\"], \"email\": \"michael.johnson@example.com\", \"phone\": \"+1122334455\", \"address\": \"789 Oak St\", \"member_since\": \"2016-03-10\"},\n" +
            "  {\"topic\": \"D\", \"name\": \"Emily Brown\", \"age\": 28, \"occupation\": \"Teacher\", \"city\": \"Houston\", \"interests\": [\"gardening\", \"volunteering\", \"movies\"], \"email\": \"emily.brown@example.com\", \"phone\": \"+1443322110\", \"address\": \"101 Pine St\", \"member_since\": \"2019-11-05\"},\n" +
            "  {\"topic\": \"E\", \"name\": \"Christopher Lee\", \"age\": 40, \"occupation\": \"Software Developer\", \"city\": \"San Francisco\", \"interests\": [\"coding\", \"gaming\", \"hiking\"], \"email\": \"chris.lee@example.com\", \"phone\": \"+1555099887\", \"address\": \"234 Maple St\", \"member_since\": \"2015-05-30\"},\n" +
            "  {\"topic\": \"F\", \"name\": \"Jessica Taylor\", \"age\": 32, \"occupation\": \"Marketing Manager\", \"city\": \"Seattle\", \"interests\": [\"writing\", \"biking\", \"cooking\"], \"email\": \"jessica.taylor@example.com\", \"phone\": \"+1662777999\", \"address\": \"567 Cedar St\", \"member_since\": \"2017-09-12\"},\n" +
            "  {\"topic\": \"G\", \"name\": \"Daniel Martinez\", \"age\": 45, \"occupation\": \"Architect\", \"city\": \"Miami\", \"interests\": [\"traveling\", \"drawing\", \"surfing\"], \"email\": \"daniel.martinez@example.com\", \"phone\": \"+1777555666\", \"address\": \"890 Walnut St\", \"member_since\": \"2014-02-25\"},\n" +
            "  {\"topic\": \"H\", \"name\": \"Sarah Wilson\", \"age\": 27, \"occupation\": \"Journalist\", \"city\": \"Boston\", \"interests\": [\"reading\", \"running\", \"photography\"], \"email\": \"sarah.wilson@example.com\", \"phone\": \"+1888444333\", \"address\": \"123 Pineapple St\", \"member_since\": \"2021-02-18\"},\n" +
            "  {\"topic\": \"%s\", \"name\": \"David Thompson\", \"age\": 33, \"occupation\": \"Financial Analyst\", \"city\": \"Atlanta\", \"interests\": [\"investing\", \"basketball\", \"traveling\"], \"email\": \"david.thompson@example.com\", \"phone\": \"+1999777666\", \"address\": \"456 Peach St\", \"member_since\": \"2013-08-09\"},\n" +
            "  {\"topic\": \"J\", \"name\": \"Rachel Garcia\", \"age\": 29, \"occupation\": \"Nurse\", \"city\": \"Dallas\", \"interests\": [\"painting\", \"yoga\", \"movies\"], \"email\": \"rachel.garcia@example.com\", \"phone\": \"+1444999888\", \"address\": \"789 Orange St\", \"member_since\": \"2019-04-27\"},\n" +
            "  {\"topic\": \"K\", \"name\": \"Kevin Nguyen\", \"age\": 31, \"occupation\": \"Entrepreneur\", \"city\": \"Austin\", \"interests\": [\"coding\", \"startups\", \"traveling\"], \"email\": \"kevin.nguyen@example.com\", \"phone\": \"+1222333444\", \"address\": \"101 Lemon St\", \"member_since\": \"2016-11-14\"},\n" +
            "  {\"topic\": \"L\", \"name\": \"Rebecca Kim\", \"age\": 26, \"occupation\": \"Graphic Designer\", \"city\": \"Denver\", \"interests\": [\"hiking\", \"photography\", \"music\"], \"email\": \"rebecca.kim@example.com\", \"phone\": \"+1666888999\", \"address\": \"234 Berry St\", \"member_since\": \"2020-08-03\"},\n" +
            "  {\"topic\": \"M\", \"name\": \"Mark Hernandez\", \"age\": 38, \"occupation\": \"Lawyer\", \"city\": \"Phoenix\", \"interests\": [\"reading\", \"tennis\", \"cooking\"], \"email\": \"mark.hernandez@example.com\", \"phone\": \"+1777666555\", \"address\": \"567 Grape St\", \"member_since\": \"2015-12-21\"},\n" +
            "  {\"topic\": \"N\", \"name\": \"Jennifer White\", \"age\": 34, \"occupation\": \"HR Manager\", \"city\": \"Philadelphia\", \"interests\": [\"painting\", \"running\", \"movies\"], \"email\": \"jennifer.white@example.com\", \"phone\": \"+1444333222\", \"address\": \"890 Cherry St\", \"member_since\": \"2017-06-08\"},\n" +
            "  {\"topic\": \"O\", \"name\": \"Andrew Brown\", \"age\": 37, \"occupation\": \"Sales Manager\", \"city\": \"San Diego\", \"interests\": [\"golfing\", \"traveling\", \"cooking\"], \"email\": \"andrew.brown@example.com\", \"phone\": \"+1555888777\", \"address\": \"123 Plum St\", \"member_since\": \"2016-01-30\"},\n" +
            "  {\"topic\": \"P\", \"name\": \"Lauren Rodriguez\", \"age\": 29, \"occupation\": \"Teacher\", \"city\": \"Orlando\", \"interests\": [\"writing\", \"yoga\", \"movies\"], \"email\": \"lauren.rodriguez@example.com\", \"phone\": \"+1666999888\", \"address\": \"456 Pine St\", \"member_since\": \"2018-10-17\"},\n" +
            "  {\"topic\": \"Q\", \"name\": \"Jonathan Kim\", \"age\": 42, \"occupation\": \"Doctor\", \"city\": \"Portland\", \"interests\": [\"hiking\", \"painting\", \"music\"], \"email\": \"jonathan.kim@example.com\", \"phone\": \"+1222777666\", \"address\": \"789 Olive St\", \"member_since\": \"2014-05-12\"},\n" +
            "  {\"topic\": \"R\", \"name\": \"Michelle Garcia\", \"age\": 30, \"occupation\": \"Software Engineer\", \"city\": \"San Antonio\", \"interests\": [\"coding\", \"gaming\", \"traveling\"], \"email\": \"michelle.garcia@example.com\", \"phone\": \"+1333444555\", \"address\": \"890 Walnut St\", \"member_since\": \"2019-02-14\"}\n" +
            "]\n";
}

/*
Last result:

Benchmark                           Mode  Cnt     Score      Error  Units
JsonPathMatchingBenchmark.matched  thrpt    5  9326.701 ± 1291.559  ops/s
 */
