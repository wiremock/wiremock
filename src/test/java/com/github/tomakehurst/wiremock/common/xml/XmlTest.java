package com.github.tomakehurst.wiremock.common.xml;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class XmlTest {

    @Test
    public void findsSimpleXmlNodesByXPath() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<things>\n" +
                "    <thing>1</thing>\n" +
                "    <thing>2</thing>\n" +
                "</things>";

        XmlDocument xmlDocument = Xml.parse(xml);

        ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("//things/thing/text()");

        assertThat(nodes.size(), is(2));
        assertThat(nodes.get(0).toString(), is("1"));
        assertThat(nodes.get(1).toString(), is("2"));
    }

    @Test
    public void findsNamespacedXmlNodeByXPath() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<things xmlns:s=\"https://stuff.biz\" id=\"1\">\n" +
                "    <stuff id=\"1\"/>\n" +
                "    <fl:fluff xmlns:fl=\"https://fluff.abc\" id=\"2\" fl:group=\"555\">\n" +
                "        <fl:inner id=\"123\" fl:code=\"D1\">Innards</fl:inner>\n" +
                "        <fl:inner>More Innards</fl:inner>\n" +
                "    </fl:fluff>\n" +
                "</things>";

        XmlDocument xmlDocument = Xml.parse(xml);

        ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("/things/fluff");

        assertThat(nodes.size(), is(1));
        assertThat(nodes.get(0).getAttributes().get("id"), is("2"));
        assertThat(nodes.get(0).getAttributes().get("fl:group"), is("555"));
    }

    @Test
    public void prettyPrintsDocument() {
        String xml = "<one><two><three name='3'/></two></one>";

        XmlDocument xmlDocument = Xml.parse(xml);

        assertThat(xmlDocument.toString(), is("<one>\n" +
                "  <two>\n" +
                "    <three name=\"3\"/>\n" +
                "  </two>\n" +
                "</one>\n"));
    }

    @Test
    public void prettyPrintsNodeAttributeValue() {
        String xml = "<one><two><three name='3'/></two></one>";

        XmlDocument xmlDocument = Xml.parse(xml);
        ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("//three/@name");

        assertThat(nodes.getFirst().toString(), is("3"));
    }

    @Test
    public void prettyPrintsNodeTextValue() {
        String xml = "<one><two>2</two></one>";

        XmlDocument xmlDocument = Xml.parse(xml);
        ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("/one/two/text()");

        assertThat(nodes.getFirst().toString(), is("2"));
    }

    @Test
    public void prettyPrintsNodeXml() {
        String xml = "<one><two><three name=\"3\"/></two></one>";

        XmlDocument xmlDocument = Xml.parse(xml);
        ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("/one/two");

        assertThat(nodes.getFirst().toString(), is("<two>\n" +
                "  <three name=\"3\"/>\n" +
                "</two>"));
    }

    @Test
    public void printsNamespacedXmlWhenPrefixDeclarationNotInScope() {
        String xml = "<?xml version=\"1.0\"?>\n" +
                "<things xmlns:s=\"https://stuff.biz\" id=\"1\">\n" +
                "    <stuff id=\"1\"/>\n" +
                "    <fl:fluff xmlns:fl=\"https://fluff.abc\" id=\"2\">\n" +
                "        <fl:inner id=\"123\" fl:code=\"D1\">Innards</fl:inner>\n" +
                "        <fl:inner>More Innards</fl:inner>\n" +
                "    </fl:fluff>\n" +
                "</things>";

        XmlDocument xmlDocument = Xml.parse(xml);
        ListOrSingle<XmlNode> xmlNodes = xmlDocument.findNodes("/things/fluff/inner[@id=\"123\"]");

        assertThat(xmlNodes.toString(), is("<fl:inner fl:code=\"D1\" id=\"123\">Innards</fl:inner>"));
    }
}
