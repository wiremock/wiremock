package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.AcceptanceTestBase;
import com.github.tomakehurst.wiremock.admin.model.PathParams;
import com.github.tomakehurst.wiremock.admin.tasks.GetHealthCheckTask;
import com.github.tomakehurst.wiremock.common.TimeUtil;
import com.github.tomakehurst.wiremock.core.Admin;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

public class GetHealthCheckTaskTest extends AcceptanceTestBase {
    private Admin mockAdmin = mock(Admin.class);
    private Request mockRequest = mock(Request.class);

    private GetHealthCheckTask getHealthCheckTask = new GetHealthCheckTask();

    /**
     * Validate health check returns version, reposne and upTime in response
     */
    @Test
    public void getHealthCheckTaskTest() {
        ResponseDefinition response = getHealthCheckTask.execute(mockAdmin, mockRequest, PathParams.empty());

        assertThat(response.getStatus(), is(HttpURLConnection.HTTP_OK));
        String responseBody = response.getTextBody();
        assertThat(responseBody.contains("version"), is(true));
        assertThat(responseBody.contains("responseTime"), is(true));
        assertThat(responseBody.contains("upTime"), is(true));
    }

    /**
     * Validate correct version is read from resource file and returned from getVersion() method.
     */
    @Test
    public void getHealthCheckVersionTest() {
        String resourceName = "sampleHealthCheckVersion.txt";
        String version = getHealthCheckTask.getVersion(resourceName);
        assertThat(version, is("1.01.1"));
    }

    /**
     * Validate correct uptime is calculated and returned from getVersion() method.
     */
    @Test
    public void getUpTimeTest() throws Exception{
        long startTime = System.currentTimeMillis();
        TimeUtil.setStartTime(startTime);
        TimeUnit.SECONDS.sleep(1);
        long upTime = getHealthCheckTask.getUpTime();
        assertThat(upTime > 0, is(true));
    }

    /**
     * Validate correct responseTime is calculated and returned from getVersion() method.
     */
    @Test
    public void getResponseTimeTest() throws Exception{
        long startTime = System.currentTimeMillis();
        TimeUnit.SECONDS.sleep(1);
        long endTime = System.currentTimeMillis();
        long responseTime = getHealthCheckTask.getResponseTime(startTime,endTime);
        assertThat(responseTime > 0, is(true));
    }
    //junit Rule for expected exception
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    /**
     * Validate correct exception and error message are returned from getVersion() method.
     */
    @Test
    public void getVersionExceptionTest() throws Exception {
        expectedEx.expect(IllegalStateException.class);
        expectedEx.expectMessage("Failed to load version source file test.txt");
        getHealthCheckTask.getVersion("test.txt");
    }

}
