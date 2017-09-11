import {ServeEvent} from './serve-event';

export class GetServeEventsResult {

  requests: ServeEvent[];
  meta: any;
  requestJournalDisabled: boolean;

  deserialize(unchecked: GetServeEventsResult, onlyMatched: boolean): GetServeEventsResult {
    this.meta = unchecked.meta;
    this.requests = [];
    unchecked.requests.forEach(request => {
      if (onlyMatched && request.wasMatched) {
        this.requests.push(new ServeEvent().deserialize(request));
      } else if (!onlyMatched) {
        this.requests.push(new ServeEvent().deserialize(request));
      }
    });
    this.requestJournalDisabled = unchecked.requestJournalDisabled;

    return this;
  }
}
