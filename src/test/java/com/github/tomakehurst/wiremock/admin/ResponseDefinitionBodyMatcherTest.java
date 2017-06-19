package com.github.tomakehurst.wiremock.admin;

import com.github.tomakehurst.wiremock.admin.model.ResponseDefinitionBodyMatcher;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder.responseDefinition;
import static junit.framework.TestCase.assertFalse;

public class ResponseDefinitionBodyMatcherTest {
    @Test
    public void doesNotMatchEmptyBody() {
        ResponseDefinition emptyBody = responseDefinition().build();
        ResponseDefinitionBodyMatcher matcher = new ResponseDefinitionBodyMatcher(0, 0);
        assertFalse(matcher.match(emptyBody).isExactMatch());
    }
}
