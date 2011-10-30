WireMock - a toolkit for simulating HTTP services
=================================================

What it's good for
------------------

* Unit/integration testing - write fluent Java specs for stubbed responses and verifications. JSON API for use with other languages.
* Functional/load testing - run as a standalone server and substitute for a real service.
 

Using with JUnit
----------------
First, add WireMock as a dependency to your project. If you're using Maven, you can do this by adding this to your POM:
<pre>
	&lt;repositories&gt;
		&lt;repository&gt;
			&lt;id&gt;tomakehurst-mvn-repo-releases&lt;/id&gt;
			&lt;name&gt;Tom Akehurst's Release Maven Repo&lt;/name&gt;
			&lt;url&gt;https://github.com/tomakehurst/tomakehurst-mvn-repo/raw/master/releases&lt;/url&gt;
			&lt;layout&gt;default&lt;/layout&gt;
		&lt;/repository&gt;
	&lt;/repositories&gt;
</pre>
	
...and this to your dependencies:
<pre>
	<dependency>
		<groupId>com.tomakehurst</groupId>
		<artifactId>wiremock</artifactId>
		<version>1.0</version>
	</dependency>
</pre>




Running standalone
------------------
