import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {HomeComponent} from './components/home/home.component';
import {MappingsComponent} from './components/mappings/mappings.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {library} from '@fortawesome/fontawesome-svg-core';
import {faBars, faPencilAlt, faPlus, faSave, faSearch, faTimes, faTrash} from '@fortawesome/free-solid-svg-icons';
import {ListViewComponent} from './components/list-view/list-view.component';
import {MatchedComponent} from './components/matched/matched.component';
import {UnmatchedComponent} from './components/unmatched/unmatched.component';
import {LayoutComponent} from './components/layout/layout.component';
import {HttpClientModule} from '@angular/common/http';
import {WiremockService} from './services/wiremock.service';
import {CodeEntryComponent} from './components/code-entry/code-entry.component';
import {HighlightJsDirective} from './directives/highlight-js.directive';
import {LayoutContentDirective} from './components/layout/layout-content.directive';
import {LayoutActionButtonsDirective} from './components/layout/layout-action-buttons.directive';
import {ReactiveFormsModule} from '@angular/forms';

// add icons. Only remove if not used anymore otherwise app will crash!
library.add(faBars);
library.add(faSearch);
library.add(faPlus);
library.add(faPencilAlt);
library.add(faTrash);
library.add(faSave);
library.add(faTimes);


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
    LayoutContentDirective,
    LayoutActionButtonsDirective
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule.forRoot(),
    FontAwesomeModule,
    HttpClientModule,
    ReactiveFormsModule
  ],
  providers: [WiremockService],
  bootstrap: [AppComponent]
})
export class AppModule {
}
