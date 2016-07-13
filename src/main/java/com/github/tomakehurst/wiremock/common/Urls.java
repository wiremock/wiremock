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
package com.github.tomakehurst.wiremock.common;

import com.github.tomakehurst.wiremock.http.FormParameter;
import com.github.tomakehurst.wiremock.http.QueryParameter;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.common.Exceptions.throwUnchecked;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;

import org.apache.commons.collections4.keyvalue.DefaultMapEntry;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class Urls {

	public static Map<String, FormParameter> splitForm(String form) {
		if (form == null || form.length() == 0) {
			return Collections.emptyMap();
		}

		final Multimap<String, String> entries = HashMultimap.create();
		for (final NameValuePair nvp : URLEncodedUtils.parse(form, Charset.forName("UTF-8"))) {
			entries.put(nvp.getName(), nvp.getValue() == null ? "" : nvp.getValue());
		}

		final Map<String, FormParameter> params = newHashMap();
		for (final String key : entries.keySet()) {
			final Collection<String> values = entries.get(key);
			params.put(key, FormParameter.formParam(key, values.toArray(new String[values.size()])));
		}
		return params;
	}

    public static Map<String, QueryParameter> splitQuery(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }

        Iterable<String> pairs = Splitter.on('&').split(query);
        ImmutableListMultimap.Builder<String, String> builder = ImmutableListMultimap.builder();
        for (String queryElement: pairs) {
            int firstEqualsIndex = queryElement.indexOf('=');
            if (firstEqualsIndex == -1) {
                builder.putAll(queryElement, "");
            } else {
                String key = queryElement.substring(0, firstEqualsIndex);
                String value = decode(queryElement.substring(firstEqualsIndex + 1));
                builder.putAll(key, value);
            }
        }

        return Maps.transformEntries(builder.build().asMap(), new Maps.EntryTransformer<String, Collection<String>, QueryParameter>() {
            public QueryParameter transformEntry(String key, Collection<String> values) {
                return new QueryParameter(key, ImmutableList.copyOf(values));
            }
        });
    }

    public static Map<String, QueryParameter> splitQuery(URI uri) {
        if (uri == null) {
            return Collections.emptyMap();
        }

        return splitQuery(uri.getQuery());
    }

    public static String decode(String encoded) {
        try {
            return URLDecoder.decode(encoded, "utf-8");
        } catch (UnsupportedEncodingException e) {
            return throwUnchecked(e, String.class);
        }
    }
}
