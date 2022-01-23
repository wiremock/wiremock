import {Component, HostBinding, Input, OnDestroy, OnInit, ViewChild, ViewEncapsulation} from '@angular/core';
import {takeUntil} from 'rxjs/operators';
import {UtilService} from '../../services/util.service';
import {Tab, TabSelectionService} from '../../services/tab-selection.service';
import {Subject} from 'rxjs/internal/Subject';
import {NgbNav} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'wm-raw-separated',
  templateUrl: './raw-separated.component.html',
  styleUrls: [ './raw-separated.component.scss' ],
  encapsulation: ViewEncapsulation.None
})
export class RawSeparatedComponent implements OnInit, OnDestroy {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  private ngUnsubscribe: Subject<any> = new Subject();

  @Input()
  separatedDisabled = false;

  @Input()
  rawDisabled = false;

  activeId = 'tab-raw';

  constructor(private tabSelectionService: TabSelectionService) {
  }

  ngOnInit() {
    this.tabSelectionService.tab$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(tabToSelect => {
      if (UtilService.isDefined(tabToSelect)) {
        switch (tabToSelect) {
          case Tab.RAW:
            // this.nav.select('tab-raw');
            this.activeId = 'tab-raw';
            break;
          case Tab.SEPARATED:
            // this.nav.select('tab-separated');
            this.activeId = 'tab-separated';
            break;
        }
      }
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }

}
