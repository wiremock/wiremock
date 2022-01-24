import {
  AfterViewInit,
  Component, ContentChild, ElementRef,
  HostBinding,
  Input,
  OnDestroy,
  OnInit,
  ViewChild,
  ViewEncapsulation
} from '@angular/core';
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
export class RawSeparatedComponent implements OnInit, OnDestroy, AfterViewInit {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  private ngUnsubscribe: Subject<any> = new Subject();

  @Input()
  separatedDisabled = false;

  @Input()
  rawDisabled = false;

  @Input()
  testHidden = true;

  @ContentChild('wm-raw-separated-test')
  test: ElementRef;

  activeId = 'tab-raw';

  constructor(private tabSelectionService: TabSelectionService) {
  }

  ngOnInit() {
    this.tabSelectionService.tab$.pipe(takeUntil(this.ngUnsubscribe)).subscribe(tabToSelect => {
      if (UtilService.isDefined(tabToSelect)) {
        switch (tabToSelect) {
          case Tab.RAW:
            this.activeId = 'tab-raw';
            break;
          case Tab.SEPARATED:
            this.activeId = 'tab-separated';
            break;
          case Tab.TEST:
            if (!this.testHidden) {
              this.activeId = 'tab-test';
              break;
            }
        }
      }
    });
  }


  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }

  ngAfterViewInit(): void {
    console.log(this.test);
  }

}
