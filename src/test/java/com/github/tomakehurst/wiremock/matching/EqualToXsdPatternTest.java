/*
 * Copyright (C) 2011 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.matching;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.common.LocalNotifier;
import java.util.Locale;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EqualToXsdPatternTest {
    private Mockery context;

    @Before
    public void init() {
        context = new Mockery();
        LocalNotifier.set(new ConsoleNotifier(true));
        // We assert English XML parser error messages in this test. So we set our default locale to English to make
        // this test succeed even for users with non-English default locales.
        Locale.setDefault(Locale.ENGLISH);
    }

    @After
    public void cleanup() {
        LocalNotifier.set(null);
    }

    @Test
    public void returnsExactMatchWhenDocumentsAreIdentical() {
        String testXML = "<things>\n" +
            "     <thing characteristic=\"tepid\"/>\n" +
            "     <thing characteristic=\"tedious\"/>\n" +
            "  </things>";
        String testXSD = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<xs:schema attributeFormDefault=\"unqualified\" elementFormDefault=\"qualified\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <xs:element name=\"things\" type=\"thingsType\"/>\n" +
            "  <xs:complexType name=\"thingType\">\n" +
            "    <xs:simpleContent>\n" +
            "      <xs:extension base=\"xs:string\">\n" +
            "        <xs:attribute type=\"xs:string\" name=\"characteristic\" use=\"optional\"/>\n" +
            "      </xs:extension>\n" +
            "    </xs:simpleContent>\n" +
            "  </xs:complexType>\n" +
            "  <xs:complexType name=\"thingsType\">\n" +
            "    <xs:sequence>\n" +
            "      <xs:element type=\"thingType\" name=\"thing\" maxOccurs=\"unbounded\" minOccurs=\"0\"/>\n" +
            "    </xs:sequence>\n" +
            "  </xs:complexType>\n" +
            "</xs:schema>";
        EqualToXsdPattern pattern = new EqualToXsdPattern(testXSD);
        assertTrue(pattern.match(testXML).isExactMatch());
    }
}