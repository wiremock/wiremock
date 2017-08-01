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

  saveMappings(): void{
    this.wiremockService.saveMappings().subscribe(data =>{
      //TODO: show popup with save ok.
    }, err =>{
      //TODO: show popup with error
    })
  }

  resetMappings(): void{
    this.wiremockService.resetMappings().subscribe(data =>{
      this.refreshMappings();
    }, err =>{
      //TODO: show popup with error
    })
  }

  removeMapping(): void{
    this.wiremockService.deleteMapping(this.selectedMapping.uuid).subscribe(data =>{
      //it worked fine
      this.refreshMappings();
    }, err =>{
      //TODO: show popup with error
    });
  }

  deleteAllMappings(): void{
    this.wiremockService.deleteAllMappings().subscribe(data =>{
      //it worked fine
      this.refreshMappings();
      this.selectedMapping = null;
    }, err =>{
      //TODO: show popup with error
    });
  }

  private refreshMappings(){
    this.wiremockService.getMappings().subscribe(data => {
        // this.cdr.detach();
        this.mappingResult = new ListStubMappingsResult().deserialize(data.json());
        // this.cdr.reattach();
      },
      err => {
        console.log("failed!", err);
      });
  }
}
