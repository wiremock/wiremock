import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {APP_BASE_HREF} from '@angular/common';
// import { ListComponent } from './list/list.component';
import { MatchedComponent } from './matched/matched.component';
import { UnmatchedComponent } from './unmatched/unmatched.component';
import { MappingComponent } from './mapping/mapping.component';
import {WiremockService} from './services/wiremock.service';
import { CodeEntryComponent } from './code-entry/code-entry.component';
import { CodeEntryListComponent } from './code-entry-list/code-entry-list.component';
import {MaterialModule} from './material/material.module';
import { HighlightJsDirective } from './directives/highlight-js.directive';
import { MappingViewComponent } from './mapping-view/mapping-view.component';
import {ListViewModule} from './list-view/list-view.module';
// import { ListEntryComponent } from './list-entry/list-entry.component';
import {CookieService} from './services/cookie.service';
import { ToolbarComponent } from './toolbar/toolbar.component';
import { TabViewComponent } from './tab-view/tab-view.component';
import { TabViewRawDirective } from './tab-view/tab-view-raw.directive';
import { TabViewSeparatedDirective } from './tab-view/tab-view-separated.directive';
import {UtilService} from './services/util.service';
import {PagerService} from './services/pager.service';

@NgModule({
  declarations: [
    AppComponent,
    // ListComponent,
    MatchedComponent,
    UnmatchedComponent,
    MappingComponent,
    CodeEntryComponent,
    CodeEntryListComponent,
    HighlightJsDirective,
    MappingViewComponent,
    // ListEntryComponent,
    ToolbarComponent,
    TabViewComponent,
    TabViewRawDirective,
    TabViewSeparatedDirective
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    MaterialModule,
    ListViewModule
  ],
  providers: [{provide: APP_BASE_HREF, useValue: '/__admin/webapp/'}, WiremockService, CookieService, UtilService, PagerService],
  bootstrap: [AppComponent]
})
export class AppModule { }
