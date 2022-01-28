import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {HomeComponent} from './components/home/home.component';
import {MappingsComponent} from './components/mappings/mappings.component';
import {
  NgbAlertModule,
  NgbButtonsModule,
  NgbCollapseModule, NgbDropdownMenu, NgbDropdownModule,
  NgbModal,
  NgbModalModule,
  NgbModule,
  NgbNav,
  NgbNavbar,
  NgbNavModule, NgbPopoverModule, NgbTooltipModule
} from '@ng-bootstrap/ng-bootstrap';
import {FontAwesomeModule, FaIconLibrary} from '@fortawesome/angular-fontawesome';
import {
  faAlignJustify,
  faAngleDoubleUp,
  faBars,
  faCamera,
  faClock,
  faCog,
  faCopy,
  faDotCircle,
  faExchangeAlt,
  faFileAlt,
  faLink,
  faPencilAlt,
  faPlus,
  faPowerOff,
  faSave,
  faSearch,
  faPlay,
  faStop,
  faSyncAlt,
  faTimes,
  faTrash,
  faFolder,
  faCheck,
  faChevronRight,
  faChevronDown,
} from '@fortawesome/free-solid-svg-icons';
import {ListViewComponent} from './components/list-view/list-view.component';
import {MatchedComponent} from './components/matched/matched.component';
import {UnmatchedComponent} from './components/unmatched/unmatched.component';
import {LayoutComponent} from './components/layout/layout.component';
import {HttpClientModule} from '@angular/common/http';
import {WiremockService} from './services/wiremock.service';
import {CodeEntryComponent} from './components/code-entry/code-entry.component';
import {HighlightJsDirective} from './directives/highlight-js.directive';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RawSeparatedComponent} from './components/raw-separated/raw-separated.component';
import {RawDirective} from './components/raw-separated/raw.directive';
import {SeparatedComponent} from './components/separated/separated.component';
import {SeparatedDirective} from './components/raw-separated/separated.directive';
import {KeysPipe} from './pipes/keys.pipe';
import {CapitalizeFirstPipe} from './pipes/capitalize-first.pipe';
import {IsObjectPipe} from './pipes/is-object.pipe';
import {IsNoObjectPipe} from './pipes/is-no-object.pipe';
import {PrettifyPipe} from './pipes/prettify.pipe';
import {SplitCamelCasePipe} from './pipes/split-camel-case.pipe';
import {WebSocketService} from './services/web-socket.service';
import {MessageComponent} from './components/message/message.component';
import {MessageService} from './components/message/message.service';
import {DialogRecordingComponent} from './dialogs/dialog-recording/dialog-recording.component';
import {SearchService} from './services/search.service';
import {CodeEditorComponent} from './components/code-editor/code-editor.component';
import {StateComponent} from './components/state/state.component';
import {StateMachineComponent} from './components/state-machine/state-machine.component';
import {StateMappingInfoComponent} from './components/state-mapping-info/state-mapping-info.component';
import {CurlPreviewComponent} from './components/curl-preview/curl-preview.component';
import { MappingTestComponent } from './components/mapping-test/mapping-test.component';
import {TestDirective} from './components/raw-separated/test.directive';
import { TreeViewComponent } from './components/tree-view/tree-view.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MappingsComponent,
    ListViewComponent,
    MatchedComponent,
    UnmatchedComponent,
    LayoutComponent,
    CodeEntryComponent,
    HighlightJsDirective,
    RawSeparatedComponent,
    RawDirective,
    SeparatedDirective,
    TestDirective,
    SeparatedComponent,
    KeysPipe,
    CapitalizeFirstPipe,
    IsObjectPipe,
    IsNoObjectPipe,
    PrettifyPipe,
    SplitCamelCasePipe,
    MessageComponent,
    DialogRecordingComponent,
    CodeEditorComponent,
    StateComponent,
    StateMachineComponent,
    StateMappingInfoComponent,
    CurlPreviewComponent,
    MappingTestComponent,
    TreeViewComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FontAwesomeModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    // ng-bootstrap
    NgbNavModule,
    NgbCollapseModule,
    NgbModalModule,
    NgbButtonsModule,
    NgbDropdownModule,
    NgbAlertModule,
    NgbTooltipModule,
    NgbPopoverModule,
  ],
  providers: [ WiremockService, WebSocketService, MessageService, SearchService, NgbModal ],
  bootstrap: [ AppComponent ],
  entryComponents: [ DialogRecordingComponent, StateMappingInfoComponent, CurlPreviewComponent, MappingTestComponent ]
})
export class AppModule {

  constructor(library: FaIconLibrary) {
    // add icons. Only remove if not used anymore otherwise app will crash!
    library.addIcons(faBars);
    library.addIcons(faSearch);
    library.addIcons(faPlus);
    library.addIcons(faPencilAlt);
    library.addIcons(faTrash);
    library.addIcons(faSave);
    library.addIcons(faTimes);
    library.addIcons(faSyncAlt);
    library.addIcons(faClock);
    library.addIcons(faAngleDoubleUp);
    library.addIcons(faAlignJustify);
    library.addIcons(faFileAlt);
    library.addIcons(faLink);
    library.addIcons(faExchangeAlt);
    library.addIcons(faCopy);
    library.addIcons(faCog);
    library.addIcons(faPowerOff);
    library.addIcons(faDotCircle);
    library.addIcons(faPlay);
    library.addIcons(faStop);
    library.addIcons(faCamera);
    library.addIcons(faFolder);
    library.addIcons(faCheck);
    library.addIcons(faChevronRight);
    library.addIcons(faChevronDown);
  }
}
