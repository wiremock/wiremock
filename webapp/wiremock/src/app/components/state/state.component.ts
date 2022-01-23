import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  ViewChild
} from '@angular/core';

import {debounceTime, filter, takeUntil} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {WiremockService} from '../../services/wiremock.service';
import {WebSocketService} from '../../services/web-socket.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {TabSelectionService} from '../../services/tab-selection.service';
import {AutoRefreshService} from '../../services/auto-refresh.service';
import {UtilService} from '../../services/util.service';
import {Scenario} from '../../model/wiremock/scenario';
import {ScenarioResult} from '../../model/wiremock/scenario-result';
import {ProxyConfig} from '../../model/wiremock/proxy-config';

@Component({
  selector: 'wm-state',
  templateUrl: './state.component.html',
  styleUrls: [ './state.component.scss' ]
})
export class StateComponent implements OnInit, OnDestroy {

  @ViewChild('canvas')
  canvas: ElementRef;

  @ViewChild('container')
  container: ElementRef;

  result: Scenario[];

  private ngUnsubscribe: Subject<any> = new Subject();

  constructor(private wiremockService: WiremockService, private webSocketService: WebSocketService,
              private messageService: MessageService, private tabSelectionService: TabSelectionService,
              private autoRefreshService: AutoRefreshService) {
  }


  ngOnInit() {
    this.webSocketService.observe('mappings').pipe(
      filter(() => this.autoRefreshService.isAutoRefreshEnabled()),
      takeUntil(this.ngUnsubscribe), debounceTime(100))
      .subscribe(() => {
        this.loadScenarios();
      });

    this.webSocketService.observe('scenario').pipe(
      filter(() => this.autoRefreshService.isAutoRefreshEnabled()),
      takeUntil(this.ngUnsubscribe), debounceTime(100))
      .subscribe(() => {
        this.loadScenarios();
      });

    this.loadScenarios();
  }

  private loadScenarios() {
    this.wiremockService.getProxyConfig().subscribe(proxyData => {
      this.loadActualScenarios(new ProxyConfig().deserialze(proxyData));
    }, err => {
      console.log('Could not load proxy config. Proxy feature deactivated');
      this.loadActualScenarios(null);
    });
  }

  private loadActualScenarios(proxyConfig: ProxyConfig) {
    this.wiremockService.getScenarios().subscribe(data => {
        const scenarioList = new ScenarioResult().deserialize(data, proxyConfig);
        this.result = scenarioList.scenarios;
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }

  resetAllScenarios() {
    this.wiremockService.resetScenarios().subscribe(() => {
      this.messageService.setMessage(new Message('Reset of all scenarios successful', MessageType.INFO, 3000));
    }, err => {
      UtilService.showErrorMessage(this.messageService, err);
    });
  }
}
