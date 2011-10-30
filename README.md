WireMock - a toolkit for simulating HTTP services
=================================================

WireMock is an HTTP server that can be configured with pre-canned responses, and that records requests for later verification.
It can be run as a standalone process or called from Java, and is configurable via JSON and Java APIs.

What it's good for
------------------

* Unit/integration testing
* Functional/load testing
* Prototyping
 

Quick start with JUnit 4.x
--------------------------

First, add WireMock as a dependency to your project. If you're using Maven, you can do this by adding this to your POM:

	<repositories>
		<repository>
			<id>tomakehurst-mvn-repo-releases</id>
			<name>Tom Akehurst's Release Maven Repo</name>
			<url>https://github.com/tomakehurst/tomakehurst-mvn-repo/raw/master/releases</url>
			<layout>default</layout>
		</repository>
	</repositories>
	
		
...and this to your dependencies:

	<dependency>
		<groupId>com.tomakehurst</groupId>
		<artifactId>wiremock</artifactId>
		<version>1.0</version>
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
		//Erases all request/response mappings and recorded requests
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
For both request/response mappings and verifications URLs can be matched exactly
or via a regular expression:
	
	.urlEqualTo("/exact/match")
	.urlMatching("/match/[a-z]{5}")
	

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


JSON API
--------

### Registereing mappings
New mappings can be registered on the fly by posting JSON to <code>http://localhost:8080/__admin/mappings/new </code>:

	{ 													
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
			}
		}												
	}
	
In the request portion only the <code>method</code>, and either the <code>url</code> or <code>urlPattern</code> attributes are mandatory.
In the response portion only the <code>status</code> attribute is mandatory.


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
A post to <code>http://localhost:8080/__admin/reset </code> will clear the list of logged requests and all mappings.


Running standalone
------------------
WireMock can be run in its own process:

	java -jar wiremock-1.0.jar
	
Or on an alternate port:
	
	java -jar wiremock-1.0.jar 9999
	
A directory called <code>mappings</code> will be created under the current directory when you first start WireMock.
Placing .json files containing mappings (in the format described above) in here and restarting will cause them to be loaded on startup.

