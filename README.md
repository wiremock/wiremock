WireMock - a tool for simulating HTTP services
==============================================

WireMock is a tool for building HTTP mocks/stubs/spies. It can be run as a standalone process or called from other Java/JVM code.
A fluent Java client API makes for expressive, concise test cases, while a JSON API enables integration with other languges.


What it's good for
------------------
	
-	Expressive unit tests for HTTP calling code
-	Integrated BDD/ATDD
-	Load testing
-	Fault injection
-	Quick REST API prototyping
 

Quick start with JUnit 4.x
--------------------------

First, add WireMock as a dependency to your project. If you're using Maven, you can do this by adding this to your POM:

	<repositories>
		<repository>
			<id>tomakehurst-mvn-repo-releases</id>
			<name>Tom Akehurst's Maven Release Repo</name>
			<url>https://github.com/tomakehurst/tomakehurst-mvn-repo/raw/master/releases</url>
			<layout>default</layout>
		</repository>
	</repositories>
	
		
...and this to your dependencies:

	<dependency>
		<groupId>com.tomakehurst</groupId>
		<artifactId>wiremock</artifactId>
		<version>1.5</version>
	</dependency>


In your test class, add this:
	
	
	private static WireMockServer wireMockServer;
	
	@BeforeClass
	public static void setupServer() {
		wireMockServer = new WireMockServer();
		wireMockServer.start();
	}
	
	@AfterClass
	public static void serverShutdown() {
		wireMockServer.stop();
	}
	
	@Before
	public void init() {
		//Erases all stub mappings and recorded requests
		WireMock.reset();
	}
	
	
This will start the server on localhost:8080.

You can then write a test case like this:
	
	@Test
	public void exampleTest() {
		givenThat(get(urlEqualTo("/my/resource"))
				.withHeader("Accept", equalTo("text/xml"))
				.willReturn(aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "text/xml")
					.withBody("<response>Some content</response>")));
		
		Result result = myHttpServiceCallingObject.doSomething();
		
		assertTrue(result.wasSuccessFul());
		
		verify(postRequestedFor(urlMatching("/my/resource/[a-z0-9]+"))
				.withBodyMatching(".*<message>1234</message>.*")
				.withHeader("Content-Type", notMatching("application/json")));
	}
	
All the API calls above are static methods on the com.tomakehurst.wiremock.client.WireMock class, so you'll need to add:

	import static com.tomakehurst.wiremock.client.WireMock.aResponse;
	import static com.tomakehurst.wiremock.client.WireMock.equalTo;
	...

### URL matching
For both stub mappings and verifications URLs can be matched exactly
or via a regular expression:
	
	.urlEqualTo("/exact/match")
	.urlMatching("/match/[a-z]{5}")

### Request method matching
HTTP methods currently supported are:

	ANY, GET, POST, PUT, DELETE, HEAD, TRACE, OPTIONS 

### Request header matching
WireMock will ignore headers in the actual request made that are not specified explicitly.
So a mapping or verification with only one header specified will still match a request with three headers
provided they contain the one specified and it matches. 

Request headers can be matched exactly, with a regex or a negative regex:

	.withHeader("Content-Type", equalTo("text/xml"))
	.withHeader("Accept", matching("text/.*"))
	.withHeader("etag", notMatching("abcd2134"))
	
	
### Request body matching
The request body can also be specified as a regex:

	.withBodyMatching(".*Something.*")
	
### Verifiying a precise number of requests
An alternate form of the <code>verify</code> call is:

	verify(3, postRequestedFor(urlMatching("/my/resource/[a-z0-9]+")) ...
	

### Running on a different host/port
If you'd prefer a different port, you can do something like this:

	wireMockServer = new WireMockServer(80);
	WireMock.configureFor("localhost", 80);
	
Note: the ability to change host in the second call is to support connection to a standalone instance on another host.

### Prioritising stub mappings
A stub mapping's priority can be set in the following way:

	givenThat(get(urlEqualTo("/some/url")).atPriority(7) ...
	
Priority 1 is treated as most important. For stub mappings with the same priority the most recently inserted will be matched first. 

### Running as a proxy to another service
Stub mappings can be configured to proxy requests to another host and port. This can be useful if you want to run your app against a real service, but
intercept and override certain responses. It can also be used in conjunction with the record feature described in the standalone section for capturing mappings
from a session running against an external service. 

### Fault injection
WireMock stubs can generate various kinds of failure.

	givenThat(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
                
The <code>com.tomakehurst.wiremock.http.Fault</code> enum implements a number of different fault types. 

JSON API
--------

### Registering stub mappings
New stub mappings can be registered on the fly by posting JSON to <code>http://localhost:8080/__admin/mappings/new </code>:

	{ 
		"priority": 3, // Defaults to 5 if not specified. 1 is highest.													
		"request": {									
			"method": "GET",						
			"url": "/my/other/resource", // "url" for exact match, or "urlPattern" for regex
			"headers": {
				"Content-Type": {
					"equalTo": "text/xml"
				},
				"Accept": {
					"matches": "(.*)xml(.*)"
				},
				"Etag": {
					"doesNotMatch": "s0912lksjd(.+)"
				} 
			}
		},										
		"response": {									
			"status": 200,							
			"body": "YES INDEED!",
			"headers": {
				"Content-Type": "text/plain",
				"Cache-Control": "no-cache"
			},
			"fixedDelayMilliseconds": 500,
			"proxyBaseUrl": "http://someotherservice.com/root", // If you use this, exclude all other response attributes
			"fault": "EMPTY_RESPONSE" // If you use this, exclude all other response attributes
		}												
	}
	
In the request portion only the <code>method</code>, and either the <code>url</code> or <code>urlPattern</code> attributes are mandatory.
In the response portion only the <code>status</code> attribute is mandatory unless proxyBaseUrl or fault is specified, in which case all attributes except fixedDelayMilliseconds will be ignored.


### Counting requests matching a pattern
Getting the number of requests that have been made to the server matching a pattern (since startup or last reset) can be achieved
by posting JSON to <code>http://localhost:8080/__admin/requests/count </code>:

	{								
		"method": "POST",						
		"url": "/resource/to/count",
		"headers": {
			"Content-Type": {
				"matches": "(.*)xml(.*)"
			}
		}
	}
	
This will return a response of the form:

	{ "count": 4 }


### Resetting the server
A post to <code>http://localhost:8080/__admin/reset </code> will clear the list of logged requests and all stub mappings.


### Global settings
Global settings can be updated by posting to /__admin/settings. Currently only one property is supported:

	{												
		"fixedDelay": 2000
	}
	
This will add the specified delay in milliseconds to every response.


Running standalone
------------------

### Command line
WireMock can be run in its own process:

	java -jar wiremock-1.5-standalone.jar
	
Or on an alternate port:
	
	java -jar wiremock-1.5-standalone.jar --port 9999
	
### Logging
Verbose logging can be enabled with the <code>--verbose</code> option.

### Recording requests
If WireMock is started with the <code>--record-mappings</code> option all non-admin requests will be captured under the mappings directory, with body content for each mapping captured under __files.

It is recommended that this option is used in tandem with the <code>--proxy-all "http://someotherhost.com/root"</code> parameter, which will create a single stub mapping to proxy all requests to the given URL.  

### Files and mappings directories 
The following directories will be created when you first start WireMock:
	
<code>mappings</code> - Contains stub mappings to load at startup. Any .json file containing a valid stub mapping (as described in the JSON API) placed under here will be loaded.

<code>__files</code> - Contains body content files referenced by mappings with bodyFileName element. Also files under here will be served by the web server directly, even when no mapping refers to them. However, mappings for a given URL will always take precedence. 
		

