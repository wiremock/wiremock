WireMock - a tool for simulating HTTP services
==============================================


Key Features
------------
	
-	HTTP response stubbing, matchable on URL, header and body content patterns
-	Request verification
-	Runs in unit tests, as a standalone process or as a WAR app
-	Configurable via a fluent Java API, JSON files and JSON over HTTP
-	Record/playback of stubs
-	Fault injection
-	Per-request conditional proxying
-   Browser proxying for request inspection and replacement
-	Stateful behaviour simulation
-	Configurable response delays
 

Using with JUnit 4.x
--------------------

First, add WireMock as a dependency to your project:

	<dependency>
		<groupId>com.github.tomakehurst</groupId>
		<artifactId>wiremock</artifactId>
		<version>1.23</version>
		
		<!-- Include this if you have dependency conflicts for Guava, Jetty, Jackson or Apache HTTP Client -->
		<classifier>standalone</classifier>
	</dependency>

Note to anyone who's used previous versions: WireMock is in Maven Central now, so it's no longer necessary to add my repo to your POM.

In your test class, add this:

	@Rule
	public WireMockRule wireMockRule = new WireMockRule(8089); // No-args constructor defaults to port 8080
	

You can then write a test case like this:
	
	@Test
	public void exampleTest() {
		stubFor(get(urlEqualTo("/my/resource"))
				.withHeader("Accept", equalTo("text/xml"))
				.willReturn(aResponse()
					.withStatus(200)
					.withHeader("Content-Type", "text/xml")
					.withBody("<response>Some content</response>")));
		
		Result result = myHttpServiceCallingObject.doSomething();
		
		assertTrue(result.wasSuccessFul());
		
		verify(postRequestedFor(urlMatching("/my/resource/[a-z0-9]+"))
				.withRequestBody(matching(".*<message>1234</message>.*"))
				.withHeader("Content-Type", notMatching("application/json")));
	}
	
You can also declare mappings in a more BDDish manner if you prefer:
	
	givenThat(get(....
	
The above @Rule will restart the WireMock server before each test method.
If you want your tests to run slightly faster the following code will keep WireMock running for the entire test class:  
	
	@Rule
    public static WireMockStaticRule wireMockRule = new WireMockStaticRule();
	
	@AfterClass
	public static void serverShutdown() {
		wireMockRule.stopServer();
	}
	
All the API calls above are static methods on the com.github.tomakehurst.wiremock.client.WireMock class, so you'll need to add:

	import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
	import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
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

Headers with multiple values are now supported.

Request headers can be matched exactly, with a regex or a negative regex:

	.withHeader("Content-Type", equalTo("text/xml"))
	.withHeader("Accept", matching("text/.*"))
	.withHeader("etag", notMatching("abcd2134"))
	.withHeader("etag", containing("abcd2134"))
	
	
### Request body matching
Multiple match conditions can be specified for a request's body contents, in a similar fashion to headers: 

	.withRequestBody(equalTo("Something"))
	.withRequestBody(matching(".*Something.*"))
	.withRequestBody(notMatching(".*Another thing.*"))
	.withRequestBody(containing("Some text"))
	
Every condition specified must be satisfied for the mapping to be selected (i.e. they're used with an AND operator)
	
### Verifiying a precise number of requests
An alternate form of the <code>verify</code> call is:

	verify(3, postRequestedFor(urlMatching("/my/resource/[a-z0-9]+")) ...

### Fetching requests matching a pattern

    findAll(putRequestedFor(urlMatching("/find/these/.*"))) ...

### Running on a different host/port
If you'd prefer a different port, you can do something like this:

	wireMockServer = new WireMockServer(80);
	WireMock.configureFor("localhost", 80);

If you've deployed WireMock as a WAR somewhere and it's not at app server's root context:

	WireMock.configureFor("somehost", 8086, "/wiremock"); 

### Prioritising stub mappings
A stub mapping's priority can be set in the following way:

	stubFor(get(urlEqualTo("/some/url")).atPriority(7) ...
	
Priority 1 is treated as most important. For stub mappings with the same priority the most recently inserted will be matched first. 

### Running as a proxy to another service
Stub mappings can be configured to proxy requests to another host and port. This can be useful if you want to run your app against a real service, but
intercept and override certain responses. It can also be used in conjunction with the record feature described in the standalone section for capturing mappings
from a session running against an external service. 

### Fault injection
WireMock stubs can generate various kinds of failure.

	stubFor(get(urlEqualTo("/malformed/response")).willReturn(
                aResponse()
                .withFault(Fault.MALFORMED_RESPONSE_CHUNK)));
                
The <code>com.github.tomakehurst.wiremock.http.Fault</code> enum implements a number of different fault types. 


### Binary Content

In some scenarios you may require the WireMock stub to return binary content.  This can be done by passing a byte[] array
to the **withBody(byte[] ..)** method:

    byte[] binaryContent = new byte[]{65,66,67}
    stubFor(get(urlEqualTo("/my/resource"))
    				.withHeader("Accept", equalTo("text/xml"))
    				.willReturn(aResponse()
    					.withStatus(200)
    					.withHeader("Content-Type", "text/xml")
    					.withBody(binaryContent)));

The **withBodyFile(String file)** method is also capable of handling binary files.  For instance, you may wish for your
mocked endpoint to return a jpg or some other binary content.

Scenarios - Simulating Stateful Behaviour
-----------------------------------------
Sometimes it is desirable to have different responses returned for the same request at different points in time. For instance given a to-do list resource,
I might wish to simulate adding an item to an existing list and seeing the list updated e.g.
	
	GET /organiser/todo-items (returns 1 item)
	POST /organiser/todo-items (with 1 new item)
	GET /organiser/todo-items (now returns 2 items)

A scenario is essentially a simple state machine, with a name and a state represented as a string (similar in concept to JMock's states feature). A stub mapping
can be associated with a scenario, can depend on a particular state in order to be served, and can modify the scenario's state when served.

The behaviour described in the example above could be implemented like this:

	stubFor(get(urlEqualTo("/organiser/todo"))
			.inScenario("ToDoList")
			.whenScenarioStateIs(Scenario.STARTED)
			.willReturn(aResponse().withBody("<item>Buy milk</item>")));
		
	stubFor(put(urlEqualTo("/organiser/todo"))
			.inScenario("ToDoList")
			.whenScenarioStateIs(Scenario.STARTED)
			.willSetStateTo("First Item Added")
			.willReturn(aResponse().withStatus(204)));
	
	stubFor(get(urlEqualTo("/organiser/todo"))
			.inScenario("ToDoList")
			.whenScenarioStateIs("First Item Added")
			.willReturn(aResponse().withBody("<item>Buy milk</item><item>Pay newspaper bill</item>")));
			
To return all registered scenarios back to the initial state you can call

	WireMock.resetAllScenarios();


JSON API
--------

### Registering stub mappings
New stub mappings can be registered on the fly by posting JSON to <code>http://localhost:8080/__admin/mappings/new </code>:

	{ 
		"priority": 3, // Defaults to 5 if not specified. 1 is highest.		
		"scenarioName": "ToDoList", // See scenario section above for details of this and the two following fields
		"requiredScenarioState": "Started",
		"newScenarioState": "First Item Added",											
		"request": {									
			"method": "GET",						
			"url": "/my/other/resource", // use one of url or urlPattern
			"urlPattern": "/my/resource?startDate=.*&endDate=.*",
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
			},
			"bodyPatterns": [
				{ "equalTo": "<content>blah</content>" },
				{ "contains": "blah" },
				{ "matches": "<content>[a-z]+</content>" },
				{ "doesNotMatch": ".*blab.*" }
			]
		},										
		"response": {									
			"status": 200,	// Required						
			"body": "YES INDEED!", // Specify this OR bodyFileName OR base64Body (for binary content)
			"base64Body" : "WUVTIElOREVFRCE=", // binary content.  see: javax.xml.bind.DatatypeConverter.printBase64Binary(byte[] bin)
			"bodyFileName": "path/to/mybodyfile.json", // Relative to __files
			"headers": {
				"Content-Type": "text/plain",
				"Cache-Control": "no-cache",
				"X-My-Header": [ "value-1", "value-2" ]
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


### Getting details of requests made to WireMock
Details of requests recorded by WireMock since the last reset (or startup) can be retrieved by POSTing a JSON document of the same
form used for counting to <code>http://localhost:8080/__admin/requests/find</code>.

The response is of the form:

    {
      "requests": [
        {
          "url": "/my/url",
          "absoluteUrl": "http://mydomain.com/my/url",
          "method": "GET",
          "headers": {
            "Accept-Language": "en-us,en;q=0.5",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:9.0) Gecko/20100101 Firefox/9.0",
            "Accept": "image/png,image/*;q=0.8,*/*;q=0.5"
          },
          "body": "",
          "browserProxyRequest": true,
          "loggedDate": 1339083581823,
          "loggedDateString": "2012-06-07 16:39:41"
        },
        {
          "url": "/my/other/url",
          "absoluteUrl": "http://my.other.domain.com/my/other/url",
          "method": "POST",
          "headers": {
            "Accept": "text/plain",
            "Content-Type": "text/plain"
          },
          "body": "My text",
          "browserProxyRequest": false,
          "loggedDate": 1339083581823,
          "loggedDateString": "2012-06-07 16:39:41"
        }
      ]
    }

### Resetting the server
A post to <code>http://localhost:8080/__admin/reset </code> will clear the list of logged requests and all stub mappings.


### Resetting the state of all scenarios
A post to <code>http://localhost:8080/__admin/scenarios/reset </code> will return all scenarios' state to Started.


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

	java -jar wiremock-1.23-standalone.jar
	
Or on an alternate port:
	
	java -jar wiremock-1.23-standalone.jar --port 9999
	
### Logging
Verbose logging can be enabled with the <code>--verbose</code> option.

### Recording requests
If WireMock is started with the <code>--record-mappings</code> option non-admin requests will be captured under the mappings directory, with body content for each mapping captured under __files.
A stub for a particular request will only be captured once within the current session (to avoid duplicates) and only for requests whose response was proxied. In practice this means that you can run wiremock as a proxy
to your client application with recording turned on, and it will only record mappings for requests it doesn't already recognise.  

You can use this option with the <code>--proxy-all="http://someotherhost.com/root"</code> parameter, which will proxy all requests to the given URL.  

### Files and mappings directories 
The following directories will be created when you first start WireMock:
	
<code>mappings</code> - Contains stub mappings to load at startup. Any .json file containing a valid stub mapping (as described in the JSON API) placed under here will be loaded.

<code>__files</code> - Contains body content files referenced by mappings with bodyFileName element. Also files under here will be served by the web server directly, even when no mapping refers to them. However, mappings for a given URL will always take precedence. 

### Configuring as a browser proxy
Starting WireMock with the --enable-browser-proxying parameter will cause it to detect when a proxy request is being made from a web browser,
and forward it as a standard proxy would unless a matching stub mapping is found. This is useful in two ways: intercepting and modifying specific
request/responses, and examining/verifying requests made during an interaction with a website.

The port number to use in your browser proxy settings is the one used when starting WireMock.

Deploying as a WAR
------------------

WireMock can be built into a WAR file and deployed to other servlet containers. See the <code>sample-war</code> sub-project under WireMock's source for an example of how to do this.

Note: currently the default serving of files from <code>__files</code> when there is no mapping present won't work in this mode, so you'll need to make your own provisions if you still need this.
Also fault responses won't work as they depend specifically on framework classes in Jetty.

