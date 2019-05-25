import {
  AfterViewInit,
  Component,
  ElementRef,
  HostListener,
  OnChanges, OnDestroy,
  OnInit,
  SimpleChanges,
  ViewChild
} from '@angular/core';

import * as joint from 'jointjs';
import * as $ from "jquery";
import {dia} from "jointjs";
import Paper = dia.Paper;
import {debounceTime, filter, takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";
import {WiremockService} from "../../services/wiremock.service";
import {WebSocketService} from "../../services/web-socket.service";
import {MessageService} from "../message/message.service";
import {TabSelectionService} from "../../services/tab-selection.service";
import {AutoRefreshService} from "../../services/auto-refresh.service";
import {ProxyConfig} from "../../model/wiremock/proxy-config";
import {ListStubMappingsResult} from "../../model/wiremock/list-stub-mappings-result";
import {UtilService} from "../../services/util.service";
import {forEach} from "@angular/router/src/utils/collection";
import {ScenarioGroup} from "../../model/scenario-group";

@Component({
  selector: 'wm-state',
  templateUrl: './state.component.html',
  styleUrls: ['./state.component.scss']
})
export class StateComponent implements OnInit, OnDestroy {

  @ViewChild('canvas')
  canvas: ElementRef;

  @ViewChild('container')
  container: ElementRef;

  result: ScenarioGroup[];

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
        this.loadMappings();
      });

    this.loadMappings();
  }

  private loadMappings() {
    this.wiremockService.getProxyConfig().subscribe(proxyData => {
      this.loadActualMappings(new ProxyConfig().deserialze(proxyData));
    }, err => {
      console.log('Could not load proxy config. Proxy feature deactivated');
      this.loadActualMappings(null);
    });
  }

  private loadActualMappings(proxyConfig: ProxyConfig) {
    this.wiremockService.getMappings().subscribe(data => {
        const mappingsList = new ListStubMappingsResult().deserialize(data, proxyConfig);

        const map = {};

        mappingsList.mappings.slice().reverse().forEach((mapping, index, object) => {
          if(UtilService.isUndefined(mapping.scenarioName)){
            mappingsList.mappings.splice(object.length - 1 - index,1);
          }else{
            if(UtilService.isUndefined(map[mapping.scenarioName])){
              map[mapping.scenarioName] = [];
            }
            map[mapping.scenarioName].push(mapping);
          }
        });

        // clear
        this.result = [];

        for(let key in map){
          this.result.push(new ScenarioGroup(map[key]));
        }
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }


  ngOnDestroy(): void {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
}
