import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {
  MdButtonModule,
  MdCardModule,
  MdCheckboxModule,
  MdChipsModule,
  MdDialogModule,
  MdIconModule,
  MdIconRegistry,
  MdInputModule,
  MdListModule,
  MdMenuModule,
  MdSelectModule,
  MdSlideToggleModule,
  MdSnackBarModule,
  MdTableModule,
  MdTabsModule,
  MdToolbarModule
} from '@angular/material';
import {CdkTableModule} from '@angular/cdk';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

@NgModule({
  imports: [
    CommonModule
  ],
  exports: [
    MdInputModule,
    MdButtonModule,
    MdTabsModule,
    MdCardModule,
    MdListModule,
    MdChipsModule,
    MdIconModule,
    CdkTableModule,
    MdTableModule,
    MdToolbarModule,
    MdSlideToggleModule,
    MdMenuModule,
    MdCheckboxModule,
    MdSelectModule,
    BrowserAnimationsModule,
    MdSnackBarModule,
    MdDialogModule
  ],
  declarations: [],
  providers: [MdIconRegistry]
})
export class MaterialModule {
}
