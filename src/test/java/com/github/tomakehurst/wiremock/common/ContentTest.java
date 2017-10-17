package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.content.Content;
import com.github.tomakehurst.wiremock.http.content.Text;
import com.google.common.io.BaseEncoding;
import org.junit.Test;

import static com.google.common.base.Charsets.ISO_8859_1;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ContentTest {

    @Test
    public void binaryContentFromArray() {
        byte[] bytes = {1, 2, 3};
        Content content = Content.fromBytes(bytes);

        assertThat(content.isBinary(), is(true));
        assertThat(content.getData(), is(bytes));
        assertThat(content.getAsString(), is("AQID"));
    }

    @Test
    public void binaryContentFromBase64() {
        byte[] bytes = {1, 2, 3};
        String base64 = BaseEncoding.base64().encode(bytes);
        Content content = Content.fromBase64(base64);

        assertThat(content.isBinary(), is(true));
        assertThat(content.getData(), is(bytes));
        assertThat(content.getAsString(), is(base64));
    }

    @Test
    public void textContentFromString() {
        String s = "Text things";

        Content content = Content.fromString(s);

        assertThat(content.isBinary(), is(false));
        assertThat(content.getData(), is(s.getBytes(UTF_8)));
        assertThat(content.getAsString(), is(s));

        Text text = (Text) content;
        assertThat(text.getCharset(), is(UTF_8));
    }

    @Test
    public void textContentFromStringWithCharacterEncoding() {
        String s = "Text things";

        Content content = Content.fromString(s, ISO_8859_1);

        assertThat(content.isBinary(), is(false));
        assertThat(content.getData(), is(s.getBytes(ISO_8859_1)));
        assertThat(content.getAsString(), is(s));

        Text text = (Text) content;
        assertThat(text.getCharset(), is(ISO_8859_1));
    }

    @Test
    public void jsonContentFromStringAndMimeType() {
        String json = "{ \"things\": \"json\" }";

        Content content = Content.fromString(json, Content.Type.JSON);

        assertThat(content.isBinary(), is(false));
        assertThat(content.getAsString(), is(
            "{\n" +
            "  \"things\" : \"json\"\n" +
            "}"));

        Text text = (Text) content;
        assertThat(text.getCharset(), is(UTF_8));
        assertThat(text.getContentType(), is(Content.Type.JSON));
    }

    @Test
    public void xmlContentFromStringAndMimeType() {
        String json = "<things><thing id=\"1\">Stuff</thing></things>";

        Content content = Content.fromString(json, Content.Type.XML);

        assertThat(content.isBinary(), is(false));
        assertThat(content.getAsString(), is(
            "<things>\n" +
                "  <thing id=\"1\">Stuff</thing>\n" +
                "</things>\n"));

        Text text = (Text) content;
        assertThat(text.getCharset(), is(UTF_8));
        assertThat(text.getContentType(), is(Content.Type.XML));
    }




}
