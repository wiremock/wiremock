import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component, ElementRef,
  EventEmitter,
  Input, NgZone,
  OnChanges, OnDestroy,
  OnInit,
  Output,
  SimpleChanges, ViewChild
} from '@angular/core';
import {UtilService} from '../../services/util.service';
import {Subject, timer} from 'rxjs';
import * as ace from 'ace-builds';
import {debounce, takeUntil} from 'rxjs/operators';

@Component({
  selector: 'wm-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: [ './code-editor.component.scss' ],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class CodeEditorComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {

  public static DEFAULT_OPTIONS = {
    selectionStyle: 'text',
    highlightActiveLine: true,
    highlightSelectedWord: true,
    readOnly: false,
    cursorStyle: 'ace',
    mergeUndoDeltas: 'true',
    behavioursEnabled: true,
    wrapBehavioursEnabled: true,
    autoScrollEditorIntoView: true, // we need that
    copyWithEmptySelection: true,
    useSoftTabs: true,
    // navigateWithinSoftTabs: true, // not working anymore
    // ...
    highlightGutterLine: false,
    showPrintMargin: false,
    printMarginColumn: false,
    printMargin: false,
    showGutter: true,
    displayIndentGuides: true,
    fontSize: 14,
    fontFamily: 'SFMono-Regular, Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
    showLineNumbers: true,
    // ..
    // firstLineNumber: 1
    wrap: true,
    enableMultiselect: true
    // maxLines: 100
    // minLines: 10
  };

  @ViewChild('editorCanvas')
  editorCanvas: ElementRef;

  private editor;

  private ngUnsubscribe: Subject<any> = new Subject();

  private editorChanges: Subject<string> = new Subject();

  _code: string;

  @Input()
  set code(value: string) {
    if (this._code !== value) {
      // prettify with cast to string. Due to javascript type in-safety
      this._code = UtilService.prettify(value) + '';
      this.setEditorValue();
    }
  }

  @Input()
  language: string;

  @Input()
  options = CodeEditorComponent.DEFAULT_OPTIONS;

  @Output()
  valueChange = new EventEmitter<string>();

  constructor(private zone: NgZone) {
  }

  ngOnInit() {
    // debounce fast changes which occur in copy / paste scenarios and make ace crash if value is changed during paste scenario.
    this.editorChanges.pipe(takeUntil(this.ngUnsubscribe), debounce(() => timer(500)))
      .subscribe(value => {
        this._code = value;
        this.valueChange.emit(this._code);
      });
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (UtilService.isDefined(this.editor)) {

      if (UtilService.isDefined(changes.options)) {
        this.zone.runOutsideAngular(() => {
          this.setOptions();
        });
      }

      if (UtilService.isDefined(changes.language)) {
        this.zone.runOutsideAngular(() => {
          this.editor.session.setMode('ace/mode/' + this.language);
        });
      }
    }
  }

  private setEditorValue() {
    if (UtilService.isDefined(this.editor)) {
      this.zone.runOutsideAngular(() => {
        this.editor.setValue(this._code);
        this.editor.selection.clearSelection();
      });
    }
  }

  getCode(): string {
    return this._code;
  }

  resize(): void {
    if (UtilService.isDefined(this.editor)) {
      this.editor.resize();
    }
  }

  private setOptions() {
    this.editor.setOptions(this.options);
    if (this.options.readOnly === true) {
      this.editor.renderer.$cursorLayer.element.style.display = 'none';
    } else {
      this.editor.renderer.$cursorLayer.element.style.display = 'block';
    }
  }

  ngAfterViewInit(): void {
    this.zone.runOutsideAngular(() => {
      this.editor = ace.edit(this.editorCanvas.nativeElement);
      this.editor.setTheme('ace/theme/monokai');
      this.editor.container.style.lineHeight = 1.5;
      this.setOptions();
      this.editor.session.setMode('ace/mode/' + this.language);
      this.setEditorValue();

      this.editor.session.on('change', () => {
        // param would be delta but I do not need it
        // delta.start, delta.end, delta.lines, delta.action
        this.zone.run(() => {
          this.editorChanges.next(this.editor.getValue());
        });
      });
    });
  }

  ngOnDestroy(): void {
    this.ngUnsubscribe.next(true);
    this.ngUnsubscribe.complete();
  }
}
