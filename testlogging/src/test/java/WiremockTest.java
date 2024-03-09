import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

public class WiremockTest {

    @Rule
    public WireMockRule wiremock = new WireMockRule();

    StringPrintStream stdOutCapture;
    PrintStream stdOut;

    @Before
    public void captureStdOut() {
        stdOut = System.out;
        stdOutCapture = new StringPrintStream();
        System.setOut(stdOutCapture);
    }

    @After
    public void revertStdOut() {
        System.setOut(stdOut);
    }


    @Test
    public void useWireMock() throws IOException {
        stubFor(get(urlMatching("/blah")).willReturn(aResponse().withStatus(200).withBody("body")));
        URL uri = new URL("http://localhost:8080/blah");
        InputStream content = uri.openConnection().getInputStream();

        final String retrievedBody = getStringFromInputStream(content);
        assertEquals("body", retrievedBody);
        assertThat(stdOutCapture.toString(), containsString("LOGBACK INFO  w.o.e.j.s.h.ContextHandler.__admin - RequestHandlerClass from context returned com.github.tomakehurst.wiremock.http.AdminRequestHandler"));
    }

    private static class StringPrintStream extends PrintStream {
        public StringPrintStream() {
            super(new ByteArrayOutputStream());
        }

        @Override
        public String toString() {
            return out.toString();
        }
    }
}
