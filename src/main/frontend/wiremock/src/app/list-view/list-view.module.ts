import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ListViewComponent} from './list-view.component';
import {ListViewListDirective} from './list-view-list.directive';
import {ListViewActionsDirective} from './list-view-actions.directive';
import {ListViewContentDirective} from './list-view-content.directive';
import {ListViewSearchDirective} from './list-view-search.directive';

@NgModule({
  imports: [
    CommonModule
  ],
  exports: [
    ListViewListDirective,
    ListViewActionsDirective,
    ListViewContentDirective,
    ListViewSearchDirective,
    ListViewComponent
  ],
  declarations: [
    ListViewListDirective,
    ListViewActionsDirective,
    ListViewContentDirective,
    ListViewSearchDirective,
    ListViewComponent
  ]
})
export class ListViewModule { }
