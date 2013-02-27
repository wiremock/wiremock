import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.junit.Assert.assertEquals;

public class WiremockTest {

    @Rule
    public WireMockRule wiremock = new WireMockRule();

    @Test
    public void useWireMock() throws IOException {
        stubFor(get(urlMatching("/blah")).willReturn(aResponse().withStatus(200).withBody("body")));
        URL uri = new URL("http://localhost:8080/blah");
        InputStream content = uri.openConnection().getInputStream();
        final String retrievedBody = IOUtils.toString(content);
        assertEquals("body", retrievedBody);
    }
}
