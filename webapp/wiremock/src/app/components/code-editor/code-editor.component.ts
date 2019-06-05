import {AfterViewInit, Component, Input, NgZone, OnChanges, OnInit, SimpleChanges, ViewChild} from '@angular/core';
import {UtilService} from '../../services/util.service';

@Component({
  selector: 'wm-code-editor',
  templateUrl: './code-editor.component.html',
  styleUrls: ['./code-editor.component.scss']
})
export class CodeEditorComponent implements OnInit, OnChanges, AfterViewInit {

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
    // copyWithEmptySelection: false, // not working anymore
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

  @ViewChild('editor') editor;

  @Input()
  code: string;

  @Input()
  language: string;

  @Input()
  options = CodeEditorComponent.DEFAULT_OPTIONS;

  constructor(private zone: NgZone) {
  }

  ngOnInit() {
  }

  ngOnChanges(changes: SimpleChanges): void {
    // prettify with cast to string
    this.code = UtilService.prettify(this.code) + '';
    this.startLanguageDetection();
  }

  textChanged(newText: string) {
    this.code = newText;
  }

  getCode(): string {
    return this.code;
  }

  private startLanguageDetection() {
    this.editor.getEditor().setOptions(this.options);
    if (this.options.readOnly === true) {
      this.editor.getEditor().renderer.$cursorLayer.element.style.display = 'none';
    } else {
      this.editor.getEditor().renderer.$cursorLayer.element.style.display = 'block';
    }
  }

  ngAfterViewInit(): void {
    this.editor.getEditor().container.style.lineHeight = 1.5;
  }
}
