import {LoggedRequest} from './logged-request';

export class FindRequestResult {

  requests: LoggedRequest[];
  requestJournalDisabled: boolean;

  deserialize(unchecked: FindRequestResult): FindRequestResult {
    this.requests = [];
    unchecked.requests.forEach(request => {
      this.requests.push(new LoggedRequest().deserialize(request));
    });
    this.requestJournalDisabled = unchecked.requestJournalDisabled;

    return this;
  }
}
