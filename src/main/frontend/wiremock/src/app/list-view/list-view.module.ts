import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ListViewComponent} from './list-view.component';
import {ListViewListDirective} from './list-view-list.directive';
import {ListViewActionsDirective} from './list-view-actions.directive';
import {ListViewContentDirective} from './list-view-content.directive';
import {ListViewSearchDirective} from './list-view-search.directive';
import {SearchComponent} from '../search/search.component';
import {MaterialModule} from '../material/material.module';
import {ListComponent} from '../list/list.component';
import {ListEntryComponent} from '../list-entry/list-entry.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {SettingsService} from '../services/settings.service';

@NgModule({
  imports: [
    CommonModule,
    MaterialModule,
    ReactiveFormsModule,
    FormsModule
  ],
  exports: [
    ListViewListDirective,
    ListViewActionsDirective,
    ListViewContentDirective,
    ListViewSearchDirective,
    ListViewComponent,
    ListComponent,
    ListEntryComponent,
    ReactiveFormsModule,
    FormsModule
  ],
  declarations: [
    ListViewListDirective,
    ListViewActionsDirective,
    ListViewContentDirective,
    ListViewSearchDirective,
    ListViewComponent,
    SearchComponent,
    ListComponent,
    ListEntryComponent
  ]
})
export class ListViewModule { }
