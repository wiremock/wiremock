import {Injectable} from '@angular/core';

@Injectable()
export class PagerService {

  constructor() {
  }

  public getPager(totalItems: number, currentPage: number = 1, pageSize: number, maxShowPage: number | 10) {
    // calculate total pages
    const totalPages = Math.ceil(totalItems / pageSize);

    if (currentPage > totalPages) {
      currentPage = totalPages;
    }

    if (currentPage < 1) {
      currentPage = 1;
    }

    let startPage: number, endPage: number;
    if (totalPages <= maxShowPage) {
      // less than 10 total pages so show all
      startPage = 1;
      endPage = totalPages;
    } else {
      // more than 10 total pages so calculate start and end pages
      if (currentPage <= maxShowPage / 2) {
        startPage = 1;
        endPage = maxShowPage;
      } else if (currentPage + (maxShowPage / 2 - 1) >= totalPages) {
        startPage = totalPages - (maxShowPage - 1);
        endPage = totalPages;
      } else {
        startPage = currentPage - Math.ceil(maxShowPage / 2 - 1);
        endPage = startPage + maxShowPage - 1;
      }
    }

    // calculate start and end item indexes
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize - 1, totalItems - 1);

    // create an array of pages to ng-repeat in the pager control
    const pages = this.getPages(startPage, endPage + 1);

    // return object with all pager properties required by the view
    return {
      totalItems: totalItems,
      currentPage: currentPage,
      pageSize: pageSize,
      totalPages: totalPages,
      startPage: startPage,
      endPage: endPage,
      startIndex: startIndex,
      endIndex: endIndex,
      pages: pages
    };
  }

  private getPages(startPage: number, endPage: number): any {
    const array = [];
    for (let i = startPage; i < endPage; i++) {
      array.push(i);
    }

    return array;
  }
}
