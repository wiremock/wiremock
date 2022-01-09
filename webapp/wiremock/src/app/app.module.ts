import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {HomeComponent} from './components/home/home.component';
import {MappingsComponent} from './components/mappings/mappings.component';
import {NgbModal, NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {library} from '@fortawesome/fontawesome-svg-core';
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
  faStop,
  faSyncAlt,
  faTimes,
  faTrash,
  faFolder
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


// add icons. Only remove if not used anymore otherwise app will crash!
library.add(faBars);
library.add(faSearch);
library.add(faPlus);
library.add(faPencilAlt);
library.add(faTrash);
library.add(faSave);
library.add(faTimes);
library.add(faSyncAlt);
library.add(faClock);
library.add(faAngleDoubleUp);
library.add(faAlignJustify);
library.add(faFileAlt);
library.add(faLink);
library.add(faExchangeAlt);
library.add(faCopy);
library.add(faCog);
library.add(faPowerOff);
library.add(faDotCircle);
library.add(faStop);
library.add(faCamera);
library.add(faFolder);

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
    CurlPreviewComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule.forRoot(),
    FontAwesomeModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule
  ],
  providers: [WiremockService, WebSocketService, MessageService, SearchService, NgbModal],
  bootstrap: [AppComponent],
  entryComponents: [DialogRecordingComponent, StateMappingInfoComponent, CurlPreviewComponent]
})
export class AppModule {
}
