package com.github.tomakehurst.wiremock.common;

import com.google.common.collect.ImmutableMap;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import java.io.StringReader;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlTest {

    @Test
    public void extractsNamespacesFromXPathAndDocument() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<t:things xmlns:t='https://things.biz' id='$1'>\n" +
                "    <stuff id='1'/>\n" +
                "    <fl:fluff xmlns:fl='https://fluff.abc' id='2'>\n" +
                "        <fl:inner id='666'>Wrong innards</fl:inner>\n" +
                "        <fl:inner id='123'>Innards</fl:inner>\n" +
                "    </fl:fluff>\n" +
                "</t:things>";

        DocumentBuilder documentBuilder = Xml.newDocumentBuilderFactory().newDocumentBuilder();
        documentBuilder.setErrorHandler(new SilentErrorHandler());
        Document document = XMLUnit.buildDocument(documentBuilder, new StringReader(xml));

        String xpathExpression = "/t:things/fl:fluff/fl:inner[@id='123']";
        Map<String, String> namespaces = Xml.extractNamespaces(xpathExpression, document);

        assertThat(namespaces.get("t"), is("https://things.biz"));
        assertThat(namespaces.get("fl"), is("https://fluff.abc"));
    }

    @Test
    public void onlyExtractsNamespacesFromWhenPresentInBothXPathAndDocument() throws Exception {
        String xml = "<?xml version='1.0'?>\n" +
                "<t:things xmlns:t='https://things.biz' id='$1'>\n" +
                "    <stuff id='1'/>\n" +
                "    <fl:fluff xmlns:fl='https://fluff.abc' id='2'>\n" +
                "        <fl:inner id='666'>Wrong innards</fl:inner>\n" +
                "        <fl:inner id='123'>Innards</fl:inner>\n" +
                "    </fl:fluff>\n" +
                "</t:things>";

        DocumentBuilder documentBuilder = Xml.newDocumentBuilderFactory().newDocumentBuilder();
        documentBuilder.setErrorHandler(new SilentErrorHandler());
        Document document = XMLUnit.buildDocument(documentBuilder, new StringReader(xml));

        String xpathExpression = "//fl:inner";
        Map<String, String> namespaces = Xml.extractNamespaces(xpathExpression, document);

        assertThat(namespaces.size(), is(1));
        assertThat(namespaces.get("fl"), is("https://fluff.abc"));
    }

    @Test
    public void findsNodesByXPathWithAutoDetectedNamespaces() throws Exception {
        String xml = "<t:thing xmlns:t='http://things' xmlns:s='http://subthings' xmlns='https://example.com/mynamespace'><s:subThing>The stuff</s:subThing></t:thing>";

        NodeList nodes = Xml.findNodesByXPath(xml, "//s:subThing/text()", null);

        assertThat(nodes.getLength(), is(1));
        assertThat(nodes.item(0).getTextContent(), is("The stuff"));
    }

    @Test
    public void findsNodesByXPathWithSpecifiedNamespaces() throws Exception {
        String xml = "<t:thing xmlns:t='http://things' xmlns:s='http://subthings' xmlns='https://example.com/mynamespace'><s:subThing>The stuff</s:subThing></t:thing>";

        NodeList nodes = Xml.findNodesByXPath(xml, "//sub:subThing/text()", ImmutableMap.of("sub", "http://subthings"));

        assertThat(nodes.getLength(), is(1));
        assertThat(nodes.item(0).getTextContent(), is("The stuff"));
    }
}
