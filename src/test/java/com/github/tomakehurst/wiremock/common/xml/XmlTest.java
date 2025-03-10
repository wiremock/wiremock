/*
 * Copyright (C) 2020-2025 Thomas Akehurst
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tomakehurst.wiremock.common.xml;

import static com.github.tomakehurst.wiremock.testsupport.WireMatchers.equalsMultiLine;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import com.github.tomakehurst.wiremock.common.ListOrSingle;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

public class XmlTest {

  @Test
  public void findsSimpleXmlNodesByXPath() {
    String xml =
        "<?xml version=\"1.0\"?>\n"
            + "<things>\n"
            + "    <thing>1</thing>\n"
            + "    <thing>2</thing>\n"
            + "</things>";

    XmlDocument xmlDocument = Xml.parse(xml);

    ListOrSingle<XmlNode> nodes = xmlDocument.findNodes("//things/thing/text()");

    assertThat(nodes.size(), is(2));
    assertThat(nodes.get(0).toString(), is("1"));
    assertThat(nodes.get(1).toString(), is("2"));
  }

  @Test
  public void findsNamespacedXmlNodeByXPath() {
    String xml =
        "<?xml version=\"1.0\"?>\n"
            + "<things xmlns:s=\"https://stuff.biz\" id=\"1\">\n"
            + "    <stuff id=\"1\"/>\n"
            + "    <fl:fluff xmlns:fl=\"https://fluff.abc\" id=\"2\" fl:group=\"555\">\n"
            + "        <fl:inner id=\"123\" fl:code=\"D1\">Innards</fl:inner>\n"
            + "        <fl:inner>More Innards</fl:inner>\n"
            + "    </fl:fluff>\n"
            + "</things>";

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

    assertThat(
        xmlDocument.toString(),
        equalsMultiLine(
            "<one>\n" + "  <two>\n" + "    <three name=\"3\"/>\n" + "  </two>\n" + "</one>\n"));
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

    assertThat(
        nodes.getFirst().toString().trim(),
        equalsMultiLine("<two>\n" + "  <three name=\"3\"/>\n" + "</two>"));
  }

  @Test
  @DisabledForJreRange(
      min = JRE.JAVA_17,
      disabledReason =
          "SaxSource unavailable to parse undeclared namespace prefix, due to "
              + "IllegalAccessException: class com.github.tomakehurst.wiremock.common.xml.XmlNode cannot access class com.sun.org.apache.xalan.internal.xsltc.trax.DOM2SAX (in module java.xml) because module java.xml does not export com.sun.org.apache.xalan.internal.xsltc.trax to unnamed module @2892dae4")
  public void printsNamespacedXmlWhenPrefixDeclarationNotInScope() {
    String xml =
        "<?xml version=\"1.0\"?>\n"
            + "<things xmlns:s=\"https://stuff.biz\" id=\"1\">\n"
            + "    <stuff id=\"1\"/>\n"
            + "    <fl:fluff xmlns:fl=\"https://fluff.abc\" id=\"2\">\n"
            + "        <fl:inner id=\"123\" fl:code=\"D1\">Innards</fl:inner>\n"
            + "        <fl:inner>More Innards</fl:inner>\n"
            + "    </fl:fluff>\n"
            + "</things>";

    XmlDocument xmlDocument = Xml.parse(xml);
    ListOrSingle<XmlNode> xmlNodes = xmlDocument.findNodes("/things/fluff/inner[@id=\"123\"]");

    assertThat(xmlNodes.toString(), is("<fl:inner fl:code=\"D1\" id=\"123\">Innards</fl:inner>"));
  }

  @Test
  void canReadConcurrently() {
    String xml =
        "<?xml version=\"1.0\"?>\n"
            + "<things xmlns:s=\"https://stuff.biz\" id=\"1\">\n"
            + "    <stuff id=\"1\"/>\n"
            + "    <fl:fluff xmlns:fl=\"https://fluff.abc\" id=\"2\">\n"
            + "        <fl:inner id=\"123\" fl:code=\"D1\">Innards</fl:inner>\n"
            + "        <fl:inner>More Innards</fl:inner>\n"
            + "    </fl:fluff>\n"
            + "</things>";
    int iterations = 10000;
    IntStream.range(0, iterations)
        .parallel()
        .mapToObj(i -> Xml.read(xml))
        .forEach(document -> assertThat(document, notNullValue()));
  }
}
