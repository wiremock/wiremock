import { Component, OnInit } from '@angular/core';
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
  selectedMapping: StubMapping | null;

  constructor(private wiremockService: WiremockService) { }

  ngOnInit() {
    this.selectedMapping = null;
    this.refreshMappings();
  }

  private refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
        this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
        if(this.mappingResult != null && this.mappingResult.mappings != null && this.mappingResult.mappings.length > 0){
          this.selectedMapping = this.mappingResult.mappings[0];
        }else{
          this.selectedMapping = null;
        }
      },
      err => {
        console.log("failed!", err);
      });
  }

}
