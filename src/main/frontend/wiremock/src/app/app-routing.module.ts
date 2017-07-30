import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {MappingViewComponent} from './mapping-view/mapping-view.component';

const routes: Routes = [
  /*{
    path: '',
    children: []
  }*/
  {path: '', component: MappingViewComponent},
  {path: 'matched', component: MappingViewComponent},
  {path: 'unmatched', component: MappingViewComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
