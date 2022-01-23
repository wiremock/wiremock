import {
  AfterContentChecked,
  AfterViewInit,
  Component, ElementRef,
  OnChanges,
  OnInit,
  SimpleChanges, ViewChild
} from '@angular/core';
import {Curl, CurlExtractor} from '../../services/curl-extractor';
import {CodeEditorComponent} from '../code-editor/code-editor.component';
import {UtilService} from '../../services/util.service';
import {Message, MessageService, MessageType} from '../message/message.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'wm-curl-preview',
  templateUrl: './curl-preview.component.html',
  styleUrls: [ './curl-preview.component.scss' ]
})
export class CurlPreviewComponent implements OnInit, OnChanges, AfterViewInit, AfterContentChecked {

  @ViewChild('editor', {static: false})
  private codeEditor: CodeEditorComponent;

  private _curl: Curl;
  curlString: string;

  private visible: boolean;

  set curl(value: Curl) {
    const toString = value.toString();
    if (this.curlString !== toString) {
      this.visible = false;
      this._curl = value;
      this.curlString = toString;
    }
  }

  constructor(private elementRef: ElementRef, private messageService: MessageService, public activeModal: NgbActiveModal) {
  }

  ngOnInit() {
  }

  copyCurl() {
    this.activeModal.dismiss();
    if (UtilService.copyToClipboard(this.codeEditor.getCode())) {
      this.messageService.setMessage(new Message('Curl copied to clipboard', MessageType.INFO, 3000));
    } else {
      this.messageService.setMessage(new Message('Was not able to copy. Details in log', MessageType.ERROR, 10000));
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
  }

  ngAfterViewInit(): void {
  }

  ngAfterContentChecked(): void {
    // this may look very strange but we need to resize ace when actual view size is known. This way we know when
    // ng-bootstrap created modal, because it is initially created in body and then moved to separate div. I could
    // not find a suitable hook there.

    const offsetParent = this.elementRef.nativeElement.offsetParent;

    if (!this.visible && UtilService.isDefined(offsetParent) && offsetParent.offsetParent !== document.body) {
      this.visible = true;
      this.codeEditor.resize();
    }
  }
}
