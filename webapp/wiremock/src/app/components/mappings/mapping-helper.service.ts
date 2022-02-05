import {Injectable} from '@angular/core';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {UtilService} from '../../services/util.service';

@Injectable()
export class MappingHelperService {

  // private static COPY_FAILURE = 'Was not able to copy. Details in log';

  static helperAddFolder(mapping: StubMapping): StubMapping {
    if (UtilService.isUndefined(mapping.metadata)) {
      mapping.metadata = {};
    }
    if (UtilService.isUndefined(mapping.metadata[UtilService.WIREMOCK_GUI_KEY])) {
      mapping.metadata[UtilService.WIREMOCK_GUI_KEY] = {};
    }
    if (UtilService.isUndefined(mapping.metadata[UtilService.WIREMOCK_GUI_KEY][UtilService.DIR_KEY])) {
      mapping.metadata[UtilService.WIREMOCK_GUI_KEY][UtilService.DIR_KEY] = '/some/path';
    }
    return mapping;
  }

  static helperAddDelay(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) &&
      UtilService.isUndefined(mapping.response.fixedDelayMilliseconds)) {
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
    if (UtilService.isUndefined(mapping)) {
      return;
    }
    if (UtilService.isUndefined(mapping.scenarioName)) {
      mapping.scenarioName = '';
    }

    if (UtilService.isUndefined(mapping.newScenarioState)) {
      mapping.newScenarioState = '';
    }

    if (UtilService.isUndefined(mapping.requiredScenarioState)) {
      mapping.requiredScenarioState = '';
    }

    return mapping;
  }

  static helperToJsonBody(mapping: StubMapping) {
    if (UtilService.isUndefined(mapping) || UtilService.isUndefined(mapping.response) || UtilService.isDefined(mapping.response.jsonBody)
      || UtilService.isUndefined(mapping.response.body)) {
      return;
    }

    mapping.response.jsonBody = JSON.parse(mapping.response.body);
    delete mapping.response.body;

    return mapping;
  }

  static helperAddProxyBaseUrl(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) &&
      UtilService.isUndefined(mapping.response.proxyBaseUrl)) {
      mapping.response.proxyBaseUrl = 'http://';
      return mapping;
    }
  }

  static helperAddRemoveProxyPathPrefix(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) &&
      UtilService.isUndefined(mapping.response.proxyUrlPrefixToRemove)) {
      mapping.response.proxyUrlPrefixToRemove = '/other/service/';
      return mapping;
    }
  }

  static helperAddAdditionalProxyRequestHeaders(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response) &&
      UtilService.isUndefined(mapping.response.additionalProxyRequestHeaders)) {
      mapping.response.additionalProxyRequestHeaders = {'User-Agent': 'Mozilla/5.0 (iPhone; U; CPU iPhone)'};
      return mapping;
    }
  }

  static helperAddResponseTemplatingTransformer(mapping: StubMapping): StubMapping {
    if (UtilService.isDefined(mapping) && UtilService.isDefined(mapping.response)) {
      if (UtilService.isUndefined(mapping.response.transformers)) {
        mapping.response.transformers = [ 'response-template' ];
      } else if (typeof mapping.response.transformers.includes === 'function' &&
        typeof mapping.response.transformers.push === 'function') {
        const transformers = (mapping.response.transformers as string[]);
        if (!transformers.includes('response-template')) {
          transformers.push('response-template');
        }
      }
      return mapping;
    }
  }

  // static helperRtCopyJson(messageService: MessageService): void {
  //   if (UtilService.copyToClipboard('{{jsonPath request.body \'$.\'}}')) {
  //     messageService.setMessage(new Message('jsonPath copied to clipboard', MessageType.INFO, 3000));
  //   } else {
  //     messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR, 10000));
  //   }
  // }
  //
  // static helperRtCopyXml(messageService: MessageService) {
  //   if (UtilService.copyToClipboard('{{xPath request.body \'/\'}}')) {
  //     messageService.setMessage(new Message('xPath copied to clipboard', MessageType.INFO, 3000));
  //   } else {
  //     messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR, 10000));
  //   }
  // }
  //
  // static helperRtCopySoap(messageService: MessageService) {
  //   if (UtilService.copyToClipboard('{{soapXPath request.body \'/\'}}')) {
  //     messageService.setMessage(new Message('soapXPath copied to clipboard', MessageType.INFO, 3000));
  //   } else {
  //     messageService.setMessage(new Message(MappingViewHelperService.COPY_FAILURE, MessageType.ERROR, 10000));
  //   }
  // }

}
