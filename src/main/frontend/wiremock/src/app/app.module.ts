import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import {APP_BASE_HREF} from '@angular/common';
import { ListViewComponent } from './list-view/list-view.component';
import { ListComponent } from './list/list.component';
import { MatchedComponent } from './matched/matched.component';
import { UnmatchedComponent } from './unmatched/unmatched.component';
import { MappingComponent } from './mapping/mapping.component';
import {WiremockService} from './services/wiremock.service';
import { CodeEntryComponent } from './code-entry/code-entry.component';
import { CodeEntryListComponent } from './code-entry-list/code-entry-list.component';
import {HighlightJsModule, HighlightJsService} from 'angular2-highlight-js';
import {MaterialModule} from './material/material.module';
import { HighlightJsDirective } from './directives/highlight-js.directive';

@NgModule({
  declarations: [
    AppComponent,
    ListViewComponent,
    ListComponent,
    MatchedComponent,
    UnmatchedComponent,
    MappingComponent,
    CodeEntryComponent,
    CodeEntryListComponent,
    HighlightJsDirective
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    AppRoutingModule,
    HighlightJsModule,
    MaterialModule
  ],
  providers: [{provide: APP_BASE_HREF, useValue: '/__admin/webapp/'}, WiremockService, HighlightJsService],
  bootstrap: [AppComponent]
})
export class AppModule { }
