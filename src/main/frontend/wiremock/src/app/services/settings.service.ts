import {Injectable} from '@angular/core';
import {CookieService} from './cookie.service';
import {UtilService} from './util.service';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';

@Injectable()
export class SettingsService {
  private darkTheme: boolean;
  private tabSlide: boolean;
  private codeEntriesHidden = new BehaviorSubject<boolean>(null);
  codeEntriesHidden$ = this.codeEntriesHidden.asObservable();

  private pagerMaxItemsPerPage: number;

  constructor() {
    this.areEmptyCodeEntriesHidden();
  }

  getPagerMaxItemsPerPage(): number {
    if (UtilService.isUndefined(this.pagerMaxItemsPerPage)) {
      this.pagerMaxItemsPerPage = parseInt(CookieService.getCookie(
        this.getCookieKey(Settings.PAGER_MAX_ITEMS_PER_PAGE)), 10);

      if (isNaN(this.pagerMaxItemsPerPage)) {
        this.pagerMaxItemsPerPage = 20;
      }
    }
    return this.pagerMaxItemsPerPage;
  }

  setPagerMaxItemsPerPage(value: number): void {
    this.pagerMaxItemsPerPage = value;
    CookieService.setCookie(this.getCookieKey(Settings.PAGER_MAX_ITEMS_PER_PAGE), String(this.pagerMaxItemsPerPage), 7300);
  }

  areEmptyCodeEntriesHidden(): boolean {
    if (UtilService.isUndefined(this.codeEntriesHidden.getValue())) {
      this.codeEntriesHidden.next(CookieService.getCookie(this.getCookieKey(Settings.EMPTY_CODE_ENTRIES_HIDDEN)) === 'true');
    }
    return this.codeEntriesHidden.getValue();
  }

  setEmptyCodeEntriesHidden(value: boolean): void {
    this.codeEntriesHidden.next(value);
    CookieService.setCookie(this.getCookieKey(Settings.EMPTY_CODE_ENTRIES_HIDDEN), String(value), 7300);
  }

  isDarkTheme(): boolean {
    if (UtilService.isUndefined(this.darkTheme)) {
      this.darkTheme = CookieService.getCookie(this.getCookieKey(Settings.DARK_THEME)) === 'true';
    }
    return this.darkTheme;
  }

  setDarkTheme(value: boolean): void {
    this.darkTheme = value;
    CookieService.setCookie(this.getCookieKey(Settings.DARK_THEME), String(this.darkTheme), 7300);
  }

  isTabSlide(): boolean {
    if (UtilService.isUndefined(this.tabSlide)) {
      this.tabSlide = CookieService.getCookie(this.getCookieKey(Settings.TAB_SLIDE)) === 'true';
    }
    return this.tabSlide;
  }

  setTabSlide(value: boolean): void {
    this.tabSlide = value;
    CookieService.setCookie(this.getCookieKey(Settings.TAB_SLIDE), String(this.tabSlide), 7300);
  }

  private getCookieKey(setting: Settings): string {
    switch (setting) {
      case Settings.DARK_THEME:
        return 'darkTheme';
      case Settings.EMPTY_CODE_ENTRIES_HIDDEN:
        return 'areEmptyCodeEntriesHidden';
      case Settings.TAB_SLIDE:
        return 'tabSlide';
      case Settings.PAGER_MAX_ITEMS_PER_PAGE:
        return 'pagerMaxItemsPerPage';
    }
  }

}

export enum Settings {
  TAB_SLIDE,
  EMPTY_CODE_ENTRIES_HIDDEN,
  DARK_THEME,
  PAGER_MAX_ITEMS_PER_PAGE,
}
