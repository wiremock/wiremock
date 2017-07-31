import {ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {WiremockService} from '../services/wiremock.service';
import {ListStubMappingsResult} from '../wiremock/model/list-stub-mappings-result';
import {StubMapping} from '../wiremock/model/stub-mapping';

@Component({
  selector: 'wm-mapping-view',
  templateUrl: './mapping-view.component.html',
  styleUrls: ['./mapping-view.component.scss']
})
export class MappingViewComponent implements OnInit {

  mappingResult: ListStubMappingsResult;
  selectedMapping: StubMapping;

  constructor(private wiremockService: WiremockService, private cdr: ChangeDetectorRef) { }

  ngOnInit() {
    this.refreshMappings();
  }

  setSelectedMapping(mapping: StubMapping){
    this.selectedMapping = mapping;
    this.cdr.detectChanges();
  }

  private refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
        this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
      },
      err => {
        console.log("failed!", err);
      });
  }
}
