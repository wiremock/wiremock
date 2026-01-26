/*
 * Copyright (C) 2025-2026 Thomas Akehurst
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
package com.github.tomakehurst.wiremock.extension.responsetemplating;

import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.common.entity.EntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.StringEntityDefinition;
import com.github.tomakehurst.wiremock.common.entity.TextEntityDefinition;
import com.github.tomakehurst.wiremock.extension.MessageActionTransformer;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.message.Message;
import com.github.tomakehurst.wiremock.message.MessageAction;
import com.github.tomakehurst.wiremock.message.MessageActionContext;
import com.github.tomakehurst.wiremock.message.MessageChannel;
import com.github.tomakehurst.wiremock.message.MessageDefinition;
import com.github.tomakehurst.wiremock.message.RequestInitiatedMessageChannel;
import com.github.tomakehurst.wiremock.message.SendMessageAction;
import java.util.HashMap;
import java.util.Map;

public class MessageTemplateTransformer implements MessageActionTransformer {

  public static final String NAME = "message-template";

  private final TemplateEngine templateEngine;

  public MessageTemplateTransformer(TemplateEngine templateEngine) {
    this.templateEngine = templateEngine;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean applyGlobally() {
    return true;
  }

  @Override
  public MessageAction transform(MessageAction action, MessageActionContext context) {
    if (!(action instanceof SendMessageAction sendAction)) {
      return action;
    }

    EntityDefinition body = sendAction.getBody();
    if (body == null) {
      return action;
    }

    String bodyContent = extractBodyContent(body);
    if (bodyContent == null || bodyContent.isEmpty()) {
      return action;
    }

    Map<String, Object> model = buildModel(context, sendAction);
    HandlebarsOptimizedTemplate template = templateEngine.getTemplate(bodyContent, bodyContent);
    String transformedBody = template.apply(model);

    return rebuildAction(sendAction, transformedBody);
  }

  private String extractBodyContent(EntityDefinition body) {
    if (body instanceof StringEntityDefinition stringDef) {
      return stringDef.getValue();
    }
    if (body instanceof TextEntityDefinition textDef) {
      Object data = textDef.getData();
      if (data instanceof String) {
        return (String) data;
      }
      if (data != null) {
        return Json.write(data);
      }
    }
    return null;
  }

  private Map<String, Object> buildModel(MessageActionContext context, SendMessageAction action) {
    Map<String, Object> model = new HashMap<>();

    if (context.isTriggeredByMessage()) {
      Message incomingMessage = context.getIncomingMessage();
      if (incomingMessage != null) {
        model.put("message", new MessageTemplateModel(incomingMessage));
      }
      MessageChannel channel = context.getOriginatingChannel();
      if (channel instanceof RequestInitiatedMessageChannel) {
        Request initiatingRequest =
            ((RequestInitiatedMessageChannel) channel).getInitiatingRequest();
        if (initiatingRequest != null) {
          model.putAll(templateEngine.buildModelForRequest(initiatingRequest));
        }
      }
    } else if (context.isTriggeredByHttp()) {
      model.putAll(templateEngine.buildModelForRequest(context.getHttpServeEvent()));
    }

    Parameters params = action.getTransformerParameters();
    if (params != null && !params.isEmpty()) {
      model.put("parameters", params);
    }

    return model;
  }

  private SendMessageAction rebuildAction(SendMessageAction original, String newBody) {
    return new SendMessageAction(
        new MessageDefinition(new StringEntityDefinition(newBody)),
        original.getChannelTarget(),
        original.getTransformers(),
        original.getTransformerParameters());
  }

  public static class MessageTemplateModel {
    private final String body;

    public MessageTemplateModel(Message message) {
      this.body = message != null ? message.getBodyAsString() : null;
    }

    public String getBody() {
      return body;
    }
  }
}
