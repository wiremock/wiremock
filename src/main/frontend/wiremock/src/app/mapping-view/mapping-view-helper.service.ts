import {StubMapping} from '../wiremock/model/stub-mapping';
import {UtilService} from '../services/util.service';
import {Message, MessageService, MessageType} from '../message/message.service';

export class MappingViewHelperService {

  private static COPY_FAILURE: string = "Was not able to copy. Details in log";

  constructor() {
  }

  static helperAddDelay(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.fixedDelayMilliseconds)) {
      mapping.response.fixedDelayMilliseconds = 2000;
      return mapping;
    }
  }

  static helperAddPriority(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isUndefined(mapping.priority)) {
      mapping.priority = 1;
      return mapping;
    }
  }

  static helperAddHeaderRequest(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.request) && UtilService.isUndefined(mapping.request.headers)) {
      mapping.request.headers = {
        'Content-Type': {
          'matches': '.*/xml'
        }
      };
      return mapping;
    }
  }

  static helperAddHeaderResponse(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.headers)) {
      mapping.response.headers = {'Content-Type': 'application/json'};
      return mapping;
    }
  }

  static helperAddScenario(mapping: StubMapping) {
    if(UtilService.isUndefined(mapping)){
      return;
    }
    if (UtilService.isUndefined(mapping.scenarioName)) {
      mapping.scenarioName = "";
    }

    if (UtilService.isUndefined(mapping.newScenarioState)) {
      mapping.newScenarioState = "";
    }

    if (UtilService.isUndefined(mapping.requiredScenarioState)) {
      mapping.requiredScenarioState = "";
    }

    return mapping;
  }

  static helperAddProxyBaseUrl(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.proxyBaseUrl)) {
      mapping.response.proxyBaseUrl = 'http://';
      return mapping;
    }
  }

  static helperAddAdditionalProxyRequestHeaders(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) && UtilService.isUndefined(mapping.response.additionalProxyRequestHeaders)) {
      mapping.response.additionalProxyRequestHeaders = {'User-Agent': 'Mozilla/5.0 (iPhone; U; CPU iPhone)'};
      return mapping;
    }
  }

  static helperAddResponseTemplatingTransformer(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response)) {
      if (UtilService.isUndefined(mapping.response.transformers)) {
        mapping.response.transformers = ['response-template'];
      } else if(typeof mapping.response.transformers.includes === 'function' &&
                typeof mapping.response.transformers.push === 'function') {
        const transformers = (mapping.response.transformers as string[]);
        if (!transformers.includes('response-template')) {
          transformers.push('response-template');
        }
      }
      return mapping;
    }
  }

  static helperRtCopyJson(messageService: MessageService): void {
    if(UtilService.copyToClipboard("{{wmJson request.body '$.'}}")){
      messageService.setMessage(new Message("wmJson copied to clipboard", MessageType.INFO,3000));
    }else{
      messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR,10000));
    }
  }

  static helperRtCopyXml(messageService: MessageService) {
    if(UtilService.copyToClipboard("{{wmXml request.body '/'}}")){
      messageService.setMessage(new Message("wmXml copied to clipboard", MessageType.INFO,3000));
    }else{
      messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR,10000));
    }
  }

  static helperRtCopySoap(messageService: MessageService) {
    if(UtilService.copyToClipboard("{{wmSoap request.body '/'}}")){
      messageService.setMessage(new Message("wmSoap copied to clipboard", MessageType.INFO,3000));
    }else{
      messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR,10000));
    }
  }
}
