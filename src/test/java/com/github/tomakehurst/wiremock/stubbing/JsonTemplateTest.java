package com.github.tomakehurst.wiremock.stubbing;

import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.http.RequestMethod.*;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;



import static com.github.tomakehurst.wiremock.testsupport.MockRequestBuilder.aRequest;

/**
 * Created with IntelliJ IDEA.
 * User: shwet.shashank
 * Date: 04/02/14
 * Time: 1:41 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(JMock.class)
public class JsonTemplateTest {
    private Mockery context;
    private InMemoryStubMappings stubMappings;

    @Before
    public void init(){
        context = new Mockery();
        stubMappings = new InMemoryStubMappings();
    }

    @After
    public void clean(){

    }

    //@Test
    public void getTemplateVariableFromRequestBody(){
        ResponseDefinition responseDefinition = new ResponseDefinition(201,
                "{.section requestBody}{.meta-left}\\\"foo\\\":\\\"{foo}\\\"{.meta-right}{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.POST, "/some/resource1"),
                responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.POST).
                withUrl("/some/resource1").withBody("{\"foo\":\"bar\"}").
                build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("{foo:bar}"));
    }

    @Test
    public void getTemplateListVariableFromRequestBody(){
        ResponseDefinition responseDefinition = new ResponseDefinition(201,
                "{.section requestBody}{.meta-left}\"foo\":\"{foo}\",\"list\":[{.repeated section list}{.meta-left}\"name\":\"{nick}\",\"age\":{age}{.meta-right}{.alternates with},{.end}]{.meta-right}{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.POST,"/some/resource"),
                responseDefinition));
        Request request = aRequest(context).
                withMethod(RequestMethod.POST).
                withBody("{\"foo\":\"bar\",\"list\":[{\"nick\":\"bob1\",\"age\":20},{\"nick\":\"bob2\",\"age\":22}]}").
                withUrl("/some/resource").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("{\"foo\":\"bar\",\"list\":[{\"name\":\"bob1\",\"age\":20},{\"name\":\"bob2\",\"age\":22}]}"));
    }

    @Test
    public void getTemplateVariableFromUrl(){
        ResponseDefinition responseDefinition = new ResponseDefinition(200,
                "{.section urlParameters}{.meta-left}foo:{urlParameter1}{.meta-right}{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.GET,"/some/bar"), responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.GET).withUrl("/some/bar").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("{foo:bar}"));
    }

    @Test
    public void  getTemplateListVariableFromUrl(){
        ResponseDefinition responseDefinition = new ResponseDefinition(200,
                "{.section urlParameters}[{.repeated section urlParameters1}{.meta-left}foo:{@}{.meta-right}{.alternates with},{.end}]{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.GET,"/some/bar1,bar2,bar3"), responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.GET).withUrl("/some/bar1,bar2,bar3").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("[{foo:bar1},{foo:bar2},{foo:bar3}]"));
    }

    @Test
    public void getTemplateVariableFromQueryParam(){
        ResponseDefinition responseDefinition = new ResponseDefinition(200,
                "{.section queryParameters}{.meta-left}foo:{foo}{.meta-right}{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.GET,"/some?foo=bar&foo1=bar1"),
                responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.GET).withUrl("/some?foo=bar&foo1=bar1").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("{foo:bar}"));
    }

    @Test
    public void getTemplateListVariableFromQueryParam(){
        ResponseDefinition responseDefinition = new ResponseDefinition(200,
               "{.section queryParameters}[{.repeated section foos}{@}{.alternates with},{.end}]{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.GET,"/some?foo=bar,bar1,bar2"), responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.GET).withUrl("/some?foo=bar,bar1,bar2").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("[bar,bar1,bar2]"));
    }

    @Test
    public void getTemplateListVariableFromHeaders(){
        ResponseDefinition responseDefinition = new ResponseDefinition(200,
                "{.section requestHeaders}[{.repeated section h1}{@}{.alternates with},{.end}]{.end}");
        responseDefinition.setIsDynamicResponse(true);
        stubMappings.addMapping(new StubMapping(new RequestPattern(RequestMethod.GET,"/some/resource"), responseDefinition));
        Request request = aRequest(context).withMethod(RequestMethod.GET).withUrl("/some/resource").
                withHeader("h1","v1").withHeader("h1","v2").build();
        ResponseDefinition response = stubMappings.serveFor(request);
        assertThat(response.getBody(), is("[v1,v2]"));
    }

}
