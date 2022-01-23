import {Injectable} from '@angular/core';
import {environment} from '../../environments/environment';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {RecordSpec} from '../model/wiremock/record-spec';
import {Observable} from 'rxjs/internal/Observable';
import {finalize, map, mergeMap, retryWhen} from 'rxjs/operators';
import {throwError} from 'rxjs/internal/observable/throwError';
import {ResponseDefinition} from '../model/wiremock/response-definition';
import {ListStubMappingsResult} from '../model/wiremock/list-stub-mappings-result';
import {UtilService} from './util.service';
import {StubMapping} from '../model/wiremock/stub-mapping';
import {timer} from 'rxjs/internal/observable/timer';
import {FindRequestResult} from '../model/wiremock/find-request-result';
import {GetServeEventsResult} from '../model/wiremock/get-serve-events-result';
import {SnapshotRecordResult} from '../model/wiremock/snapshot-record-result';
import {ProxyConfig} from '../model/wiremock/proxy-config';
import {RecordingStatus} from '../model/wiremock/recording-status';
import {ScenarioResult} from '../model/wiremock/scenario-result';

@Injectable()
export class WiremockService {

  private static getUrl(path: string): string {
    return environment.url + path;
  }

  private static mapBody(body: any): string | null {
    if (body === null || typeof body === 'undefined') {
      return null;
    }
    return typeof body === 'string' ? body : UtilService.toJson(body);
  }

  constructor(private http: HttpClient) {
  }

  resetAll(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post<ResponseDefinition>(WiremockService.getUrl('reset'), null));
  }

  getMappings(): Observable<ListStubMappingsResult> {
    return this.defaultPipe(this.http.get<ListStubMappingsResult>(WiremockService.getUrl('mappings')));
  }

  saveMappings(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post<ResponseDefinition>(WiremockService.getUrl('mappings/save'), null));
  }

  resetMappings(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post<ResponseDefinition>(WiremockService.getUrl('mappings/reset'), null));
  }

  deleteAllMappings(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.delete<ResponseDefinition>(WiremockService.getUrl('mappings')));
  }

  saveMapping(id: string, mapping: string): Observable<StubMapping> {
    return this.defaultPipe(this.http.put<StubMapping>(WiremockService.getUrl('mappings/' + id),
      WiremockService.mapBody(mapping))).pipe(map(editedMapping => new StubMapping().deserialize(editedMapping, null)));
  }

  saveNewMapping(mapping: string): Observable<StubMapping> {
    return this.defaultPipe(this.http.post<StubMapping>(WiremockService.getUrl('mappings'),
      WiremockService.mapBody(mapping))).pipe(map(newMapping => new StubMapping().deserialize(newMapping, null)));
  }

  deleteMapping(id: string): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.delete<ResponseDefinition>(WiremockService.getUrl('mappings/' + id)));
  }

  getScenarios(): Observable<ScenarioResult> {
    return this.defaultPipe(this.http.get<ScenarioResult>(WiremockService.getUrl('scenarios')));
  }

  resetJournal(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.delete<ResponseDefinition>(WiremockService.getUrl('requests')));
  }

  resetScenarios(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post<ResponseDefinition>(WiremockService.getUrl('scenarios/reset'), null));
  }

  getRequests(): Observable<GetServeEventsResult> {
    return this.defaultPipe(this.http.get<GetServeEventsResult>(WiremockService.getUrl('requests')));
  }

  getUnmatched(): Observable<FindRequestResult> {
    return this.defaultPipe(this.http.get<FindRequestResult>(WiremockService.getUrl('requests/unmatched')));
  }

  startRecording(recordSpec: RecordSpec): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post <ResponseDefinition>(WiremockService.getUrl('recordings/start'),
      WiremockService.mapBody(recordSpec)));
  }

  stopRecording(): Observable<SnapshotRecordResult> {
    return this.defaultPipe(this.http.post<SnapshotRecordResult>(WiremockService.getUrl('recordings/stop'), null))
      .pipe(map(data => new SnapshotRecordResult().deserialize(data)));
  }

  snapshot(): Observable<SnapshotRecordResult> {
    return this.defaultPipe(this.http.post<SnapshotRecordResult>(WiremockService.getUrl('recordings/snapshot'), null))
      .pipe(map(snapshot => new SnapshotRecordResult().deserialize(snapshot)));
  }

  getRecordingStatus(): Observable<RecordingStatus> {
    return this.defaultPipe(this.http.get<RecordingStatus>(WiremockService.getUrl('recordings/status')))
      .pipe(map((status: any) => (<any>RecordingStatus)[status.status]));
  }

  shutdown(): Observable<ResponseDefinition> {
    return this.defaultPipe(this.http.post<ResponseDefinition>(WiremockService.getUrl('shutdown'), null));
  }

  getProxyConfig(): Observable<ProxyConfig> {
    return this.defaultPipe(this.http.get<ProxyConfig>(WiremockService.getUrl('proxy')));
  }

  enableProxy(uuid: string): Observable<any> {
    return this.defaultPipe(this.http.put<any>(WiremockService.getUrl('proxy/' + uuid), null));
  }

  disableProxy(uuid: string): Observable<any> {
    return this.defaultPipe(this.http.delete<any>(WiremockService.getUrl('proxy/' + uuid)));
  }

  getFileBody(fileName: string): Observable<string> {
    return this.defaultPipe(this.http.get<string>(WiremockService.getUrl('files/' + fileName)));
  }

  private handleError(error: HttpErrorResponse) {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      // The backend returned an unsuccessful response code.
      // The response body may contain clues as to what went wrong,
      // console.error(
      //   `Backend returned code ${error.status}, ` +
      //   `body was: ${error.error}`);
    }
    // return an observable with a user-facing error message
    return throwError(
      'Something bad happened; please try again later.');
  }

  private defaultPipe<T>(observable: Observable<T>) {
    // return observable.pipe(retry(3), debounceTime(100), catchError(this.handleError));
    // return observable.pipe(retryWhen(errors => errors.pipe(delay(1000))));
    return observable.pipe(retryWhen(this.genericRetryStrategy({
      scalingDuration: 1000,
      maxRetryAttempts: 3,
      excludedStatusCodes: [ 400, 422 ]
    })));
  }


  private genericRetryStrategy = ({
                                    maxRetryAttempts = 3,
                                    scalingDuration = 1000,
                                    excludedStatusCodes = []
                                  }: {
    maxRetryAttempts?: number,
    scalingDuration?: number,
    excludedStatusCodes?: number[]
  } = {}) => (attempts: Observable<any>) => {
    return attempts.pipe(
      mergeMap((error, i) => {
        const retryAttempt = i + 1;
        // if maximum number of retries have been met
        // or response is a status code we don't wish to retry, throw error
        if (
          retryAttempt > maxRetryAttempts ||
          excludedStatusCodes.find(e => e === error.status)
        ) {
          throw(error);
        }
        console.log(
          `Attempt ${retryAttempt}: retrying in ${retryAttempt *
          scalingDuration}ms`
        );
        // retry after 1s, 2s, etc...
        return timer(retryAttempt * scalingDuration);
      }),
      finalize(() => console.log('We are done!'))
    );
  }
}
