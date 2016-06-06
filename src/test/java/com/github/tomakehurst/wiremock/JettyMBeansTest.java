package com.github.tomakehurst.wiremock;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;

public class JettyMBeansTest {
	
	/**
	 * The value of this constant must match the port number in the following property in the build.gradle file:
	 * 		systemProperty 'com.sun.management.jmxremote.port', '9998'
	 */
	private static final String JMX_PORT = "9998";
	/**
	 * When run outside of the JUnit test, the following 
	 * all show up loaded in JConsole prefixed with "wiremock."
	 * Why is that?
	 */
	String expectedDomains[] = { 
			"org.eclipse.jetty.io",
	        "org.eclipse.jetty.jmx",
	        "org.eclipse.jetty.server",
	        "org.eclipse.jetty.server.handler",
	        "org.eclipse.jetty.servlet",
	        "org.eclipse.jetty.util.thread" 
	        };

	@Test
	@Ignore("See comments for high maintenance test setup")
	/**
	 * Three things required to run this test:
	 * 1) add the following to the test{} section of build.gradle:
	 * 
	 *    	systemProperty 'com.sun.management.jmxremote',''
     *		systemProperty 'com.sun.management.jmxremote.ssl', 'false'
     *		systemProperty 'com.sun.management.jmxremote.authenticate','false'
     *		systemProperty 'com.sun.management.jmxremote.port','9998'
	 * 
	 * 
	 * 2)  Make sure the @Ignore is commented out, above.
	 * 
	 * 3)  The test needs to be run without any other test, as follows:
	 * 			gradle -Dtest.single=JettyMBeansTest clean test 
	 * 
	 * NOTES:
	 * 
	 * If you run this test and omit the systemProperty stuff in build.gradle, you get this:
	 * <PRE>
 com.github.tomakehurst.wiremock.JettyMBeansTest > canFindJettyMBeans FAILED
    java.io.IOException at JettyMBeansTest.java:67
        Caused by: javax.naming.ServiceUnavailableException at JettyMBeansTest.java:67
            Caused by: java.rmi.ConnectException at JettyMBeansTest.java:67
                Caused by: java.net.ConnectException at JettyMBeansTest.java:67
	 * <PRE>
	 * 
	 * However, those exact same java system properties are NOT required for "java -jar wiremock.jar"
	 * I don't understand it, but that's the way it is.
	 * 
	 * If you include the systemProperty settings with other tests, regardless of whether
	 * you are running this JettyMBeansTest, you get tons of errors like this:
	 * <PRE>
	 Error: Exception thrown by the agent : java.rmi.server.ExportException: Port already in use: 9998; nested exception is:
        java.net.BindException: Address already in use
        </PRE>
	 * 
	 * @throws IOException
	 */
	public void canFindJettyMBeans() throws IOException {
        JMXServiceURL url =
                new JMXServiceURL("service:jmx:rmi:///jndi/rmi://:" + JMX_PORT + "/jmxrmi"); 
            JMXConnector jmxc = JMXConnectorFactory.connect(url, null);

            MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

            String actualDomains[] = mbsc.getDomains();
            
            for (String expectedDomain : expectedDomains) {
            	boolean found = false;
                for(String actualDomain : actualDomains) {
                	if (actualDomain.equals(expectedDomain))
                		found = true;
                }
                assertTrue("Did not found expected JMX domain [" + expectedDomain + "] in list of actual JMX domains [" + Arrays.toString(actualDomains) + "]", found);
            }
		
	}
    @After
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }
	protected static WireMockServer wireMockServer;
	protected static WireMockTestClient testClient;

	@BeforeClass
	public static void setupServer() {


		WireMockConfiguration wireMockConfiguration = new WireMockConfiguration().enableJettyMBeans(true);
		setupServer(wireMockConfiguration);
	}

	@AfterClass
	public static void serverShutdown() {
		wireMockServer.stop();
	}

    public static void setupServer(WireMockConfiguration options) {
        if(options.portNumber() == Options.DEFAULT_PORT) {
			options.dynamicPort();
        }

        wireMockServer = new WireMockServer(options);
        wireMockServer.start();
        testClient = new WireMockTestClient(wireMockServer.port());
        WireMock.configureFor(wireMockServer.port());
    }

	@Before
	public void init() throws InterruptedException {
		WireMock.resetToDefault();
	}
    
	
}
