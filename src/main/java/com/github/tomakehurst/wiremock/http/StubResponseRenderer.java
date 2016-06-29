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
package com.github.tomakehurst.wiremock.http;

import com.github.tomakehurst.wiremock.common.BinaryFile;
import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.extension.ResponseTransformer;
import com.github.tomakehurst.wiremock.global.GlobalSettingsHolder;
import com.google.common.base.Optional;

import java.util.concurrent.TimeUnit;
import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockApp.FILES_ROOT;

import static com.github.tomakehurst.wiremock.http.Response.response;

public class StubResponseRenderer implements ResponseRenderer {
	
	private final FileSource fileSource;
	private final GlobalSettingsHolder globalSettingsHolder;
	private final ProxyResponseRenderer proxyResponseRenderer;
	private final List<ResponseTransformer> responseTransformers;

    public StubResponseRenderer(FileSource fileSource,
								GlobalSettingsHolder globalSettingsHolder,
								ProxyResponseRenderer proxyResponseRenderer,
								List<ResponseTransformer> responseTransformers) {
        this.fileSource = fileSource;
        this.globalSettingsHolder = globalSettingsHolder;
        this.proxyResponseRenderer = proxyResponseRenderer;
		this.responseTransformers = responseTransformers;
	}

	@Override
	public Response render(ResponseDefinition responseDefinition) {
		if (!responseDefinition.wasConfigured()) {
			return Response.notConfigured();
		}

		Response response = buildResponse(responseDefinition);
		return applyTransformations(responseDefinition.getOriginalRequest(), responseDefinition, response, responseTransformers);
	}

	private Response buildResponse(ResponseDefinition responseDefinition) {
		addDelayIfSpecifiedGloballyOrIn(responseDefinition);
		addRandomDelayIfSpecifiedGloballyOrIn(responseDefinition);

		if (responseDefinition.isProxyResponse()) {
			return proxyResponseRenderer.render(responseDefinition);
		} else {
			return renderDirectly(responseDefinition);
		}
	}

	private Response applyTransformations(Request request,
										  ResponseDefinition responseDefinition,
										  Response response,
										  List<ResponseTransformer> transformers) {
		if (transformers.isEmpty()) {
			return response;
		}

		ResponseTransformer transformer = transformers.get(0);
		Response newResponse =
				transformer.applyGlobally() || responseDefinition.hasTransformer(transformer) ?
						transformer.transform(request, response, fileSource.child(FILES_ROOT), responseDefinition.getTransformerParameters()) :
						response;

		return applyTransformations(request, responseDefinition, newResponse, transformers.subList(1, transformers.size()));
	}

	private Response renderDirectly(ResponseDefinition responseDefinition) {
        Response.Builder responseBuilder = response()
                .status(responseDefinition.getStatus())
				.statusMessage(responseDefinition.getStatusMessage())
                .headers(responseDefinition.getHeaders())
                .fault(responseDefinition.getFault());

		if (responseDefinition.specifiesBodyFile()) {
			BinaryFile bodyFile = fileSource.getBinaryFileNamed(responseDefinition.getBodyFileName());
            responseBuilder.body(bodyFile.readContents());
		} else if (responseDefinition.specifiesBodyContent()) {
            if(responseDefinition.specifiesBinaryBodyContent()) {
                responseBuilder.body(responseDefinition.getByteBody());
            } else {
                responseBuilder.body(responseDefinition.getByteBody());
            }
		}

        return responseBuilder.build();
	}
	
    private void addDelayIfSpecifiedGloballyOrIn(ResponseDefinition response) {
    	Optional<Integer> optionalDelay = getDelayFromResponseOrGlobalSetting(response);
        if (optionalDelay.isPresent()) {
	        try {
	            Thread.sleep(optionalDelay.get());
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    }
    }
    
    private Optional<Integer> getDelayFromResponseOrGlobalSetting(ResponseDefinition response) {
    	Integer delay = response.getFixedDelayMilliseconds() != null ?
    			response.getFixedDelayMilliseconds() :
    			globalSettingsHolder.get().getFixedDelay();
    	
    	return Optional.fromNullable(delay);
    }

    private void addRandomDelayIfSpecifiedGloballyOrIn(ResponseDefinition response) {
		if (response.getDelayDistribution() != null) {
			addRandomDelayIn(response.getDelayDistribution());
		} else {
			addRandomDelayIn(globalSettingsHolder.get().getDelayDistribution());
		}
    }

	private void addRandomDelayIn(DelayDistribution delayDistribution) {
		if (delayDistribution == null) return;

		long delay = delayDistribution.sampleMillis();
		try {
           TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
           Thread.currentThread().interrupt();
        }
	}
}
