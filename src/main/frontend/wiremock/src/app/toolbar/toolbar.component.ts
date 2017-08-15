import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {SettingsService} from '../services/settings.service';
import {animate, state, style, transition, trigger} from '@angular/animations';
import {UtilService} from '../services/util.service';
import {SseService} from '../services/sse.service';
import {Observer} from 'rxjs/Observer';
import {Observable} from 'rxjs/Observable';
import {RecordingStatusResult} from '../wiremock/model/recording-status-result';
import {RecordingStatus} from '../wiremock/model/recording-status';
import {MessageService} from '../message/message.service';
import {RecordSpec} from '../wiremock/model/record-spec';
import {MdDialog} from '@angular/material';
import {DialogRecordingComponent} from 'app/dialogs/dialog-recording/dialog-recording.component';
import {SnapshotRecordResult} from '../wiremock/model/snapshot-record-result';
import {SearchService} from 'app/services/search.service';
import {environment} from '../../environments/environment';

@Component({
  selector: 'wm-toolbar',
  templateUrl: './toolbar.component.html',
  styleUrls: ['./toolbar.component.scss'],
  animations: [
    trigger('recordingState', [
      state('flashOn', style({
        opacity: 1.0
      })),
      state('flashOff', style({
        opacity: 0.2
      })),
      transition('flashOn => flashOff', animate('800ms ease-out')),
      transition('flashOff => flashOn', animate('800ms ease-in'))
    ])
  ]
})
export class ToolbarComponent implements OnInit {

  isDarkTheme: boolean;
  isTabSlide: boolean;
  areEmptyCodeEntriesHidden: boolean;

  // this value is for the flashing animation
  recordingState: string;

  // interval remembered.
  recordingInterval;

  // actual hide or show the recording status
  showRecording = false;
  private recordingStatusObserver: Observer<RecordingStatus>;

  // for the rest interface
  private refreshRecordingStateObserver: Observer<RecordingStatus>;

  wmLogo: string;

  @Output()
  themeChanged = new EventEmitter();

  constructor(private wiremockService: WiremockService, private settingsService: SettingsService,
              private sseService: SseService, private messageService: MessageService, private dialog: MdDialog,
              private searchService: SearchService) {
  }

  resetAll(): void {
    this.wiremockService.resetAll().subscribe();
  }

  changeHideEmptyCodeEntries(): void {
    this.areEmptyCodeEntriesHidden = !this.areEmptyCodeEntriesHidden;
    this.settingsService.setEmptyCodeEntriesHidden(this.areEmptyCodeEntriesHidden);
  }

  changeTheme(): void {
    this.isDarkTheme = !this.isDarkTheme;
    this.settingsService.setDarkTheme(this.isDarkTheme);
    this.themeChanged.emit(this.isDarkTheme);
  }

  ngOnInit() {
    this.wmLogo = environment.resourcesUrl + 'assets/images/wiremock-concept-icon-01.png';
    this.isDarkTheme = this.settingsService.isDarkTheme();
    this.isTabSlide = this.settingsService.isTabSlide();
    this.areEmptyCodeEntriesHidden = this.settingsService.areEmptyCodeEntriesHidden();

    // soft update of mappings can  be triggered via observer
    Observable.create(observer => {
      this.refreshRecordingStateObserver = observer;
    }).debounceTime(100).subscribe(() => {
      this.refreshRecordingState();
    });

    // SSE registration for mappings updates
    this.sseService.register('message', data => {
      if (data.data === 'recording') {
        this.refreshRecordingStateObserver.next(data.data);
      }
    });

    Observable.create(observer => {
      this.recordingStatusObserver = observer;
    }).subscribe(next => {

      const recordingStatus: RecordingStatus = (next as RecordingStatus);

      switch (recordingStatus) {
        case RecordingStatus.Recording:
          if (UtilService.isUndefined(this.recordingInterval)) {
            this.recordingInterval = setInterval(() => {
              if (UtilService.isUndefined(this.recordingState) || this.recordingState === 'flashOff') {
                this.recordingState = 'flashOn';
              } else {
                this.recordingState = 'flashOff';
              }
            }, 1100);
          }
          this.showRecording = true;
          break;
        case RecordingStatus.Stopped:
        case RecordingStatus.NeverStarted:
          if (UtilService.isDefined(this.recordingInterval)) {
            clearInterval(this.recordingInterval);
            this.recordingInterval = null;
          }
          this.showRecording = false;
          break;
      }
    });

    this.refreshRecordingState();
  }

  changeTabSlide(): void {
    this.isTabSlide = !this.isTabSlide;
    this.settingsService.setTabSlide(this.isTabSlide);
  }

  shutdown(): void {
    this.wiremockService.shutdown().subscribe();
  }

  startRecording(): void {
    const dialogRef = this.dialog.open(DialogRecordingComponent);

    dialogRef.afterClosed().subscribe(result => {
      if (result !== 'cancel') {
        this.actualStartRecording(result);
      }
    });
  }

  private actualStartRecording(url: string): void {
    const recordSpec: RecordSpec = new RecordSpec();
    recordSpec.targetBaseUrl = url;

    this.wiremockService.startRecording(recordSpec).subscribe();
  }

  stopRecording(): void {
    this.wiremockService.stopRecording().subscribe(response => {
      const recordings = new SnapshotRecordResult().deserialize(response.json());

      let result = '';
      const ids = recordings.getIds();

      for (const id of ids) {
        if (result.length !== 0) {
          result += '|';
        }
        result += id;
      }

      this.searchService.setValue(result);
    });
  }

  snapshot(): void {
    this.wiremockService.snapshot().subscribe(response => {
      const recordings = new SnapshotRecordResult().deserialize(response.json());

      let result = '';
      const ids = recordings.getIds();

      for (const id of ids) {
        if (result.length !== 0) {
          result += '|';
        }
        result += id;
      }

      this.searchService.setValue(result);
    });
  }

  refreshRecordingState() {
    this.wiremockService.getRecordingStatus().subscribe(data => {
        this.recordingStatusObserver.next(new RecordingStatusResult().deserialize(data.json()).status);
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

}
