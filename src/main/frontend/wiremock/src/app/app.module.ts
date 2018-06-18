import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {HomeComponent} from './components/home/home.component';
import {MappingsComponent} from './components/mappings/mappings.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {library} from '@fortawesome/fontawesome-svg-core';
import {faBars} from '@fortawesome/free-solid-svg-icons';
import { ListViewComponent } from './components/list-view/list-view.component';
import { MatchedComponent } from './components/matched/matched.component';
import { UnmatchedComponent } from './components/unmatched/unmatched.component';
import { LayoutComponent } from './components/layout/layout.component';

// add icons. Only remove if not used anymore otherwise app will crash!
library.add(faBars);

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    MappingsComponent,
    ListViewComponent,
    MatchedComponent,
    UnmatchedComponent,
    LayoutComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    NgbModule.forRoot(),
    FontAwesomeModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
