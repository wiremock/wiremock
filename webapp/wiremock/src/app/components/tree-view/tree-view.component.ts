import {Component, EventEmitter, HostBinding, Input, OnChanges, OnInit, Output, SimpleChanges} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {UtilService} from '../../services/util.service';
import {WiremockService} from '../../services/wiremock.service';
import {MessageService} from '../message/message.service';
import {Tree} from '../../model/tree/tree';
import {Root} from '../../model/tree/root';
import {TreeNode} from '../../model/tree/tree-node';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {Folder} from '../../model/tree/folder';

@Component({
  selector: 'wm-tree-view',
  templateUrl: './tree-view.component.html',
  styleUrls: [ './tree-view.component.scss' ]
})
export class TreeViewComponent implements OnInit, OnChanges {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  @Input()
  items: Item[];

  @Input()
  activeItem: Item;

  pageSize = 20;

  page = 1;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  tree: Tree;
  rootNode: TreeNode;
  private root: Item;

  treeItems: TreeNode[];

  constructor(private wiremockService: WiremockService,
              private messageService: MessageService) {
  }

  selectActiveItem(item: Item) {
    if (this.activeItem === item) {
      return;
    }
    this.activeItem = item;
    this.activeItemChange.emit(this.activeItem);
  }

  ngOnInit() {
    this.root = new Root();
  }

  ngOnChanges(changes: SimpleChanges): void {
    // let changed = false;

    if (UtilService.isDefined(changes.items) && UtilService.isDefined(this.items)) {

      this.items.sort((a: Item, b: Item) => {
        if (a.hasGroup() && b.hasGroup()) {
          if (a.getGroup() < b.getGroup()) {
            return -1;
          }
          if (a.getGroup() > b.getGroup()) {
            return 1;
          }
          return 0;
        } else if (a.hasGroup()) {
          return -1;
        } else {
          return 1;
        }
      });

      console.log(this.items);

      this.tree = new Tree(this.root);
      this.rootNode = this.tree.find(this.root.getId());

      this.items.forEach(value => {

        if (value instanceof StubMapping && UtilService.isGroupDefined(value)) {

          const groupText = value.metadata.gui.group as string;
          const groups = groupText.split('.');

          let groupParent: Item = this.root;
          let groupId = '';
          groups.forEach((groupName, index) => {
            if (index === 0) {
              groupId = groupName;
            } else {
              groupId = groupId + '.' + groupName;
            }
            const groupNode = this.tree.find(groupId);
            if (!UtilService.isDefined(groupNode)) {
              // group does not exist yet. Create it
              this.tree.insert(groupParent.getId(), new Folder(groupId, groupName));
            }
            groupParent = this.tree.find(groupId).value;
          });


          const parent = this.tree.find(groupText);

          if (UtilService.isDefined(parent)) {
            // found group
            this.tree.insertByNode(parent, value);
          } else {
            // create group folder
            console.log('oO Something went wrong!!!');
          }
        } else {
          this.tree.insert(this.root.getId(), value);
        }
      });

      console.log(this.tree);

      this.treeItems = Array.from(this.tree.preOrderTraversal());


      //   if (this.items.length > this.pageSize) {
      //     const maxPages = Math.ceil(this.items.length / this.pageSize);
      //     if (maxPages < this.page) {
      //       this.page = maxPages;
      //     }
      //   } else {
      //     this.page = 1;
      //   }
      //   changed = true;
      // }
      //
      // if (UtilService.isDefined(this.activeItem) && UtilService.isDefined(this.items)) {
      //   const index = this.items.findIndex((item: Item) => {
      //     return item.getId() === this.activeItem.getId();
      //   }) + 1;
      //
      //   this.page = Math.ceil(index / this.pageSize);
      //
      //   changed = true;
      // }
      //
      // if (changed) {
      //   this.setFilteredItems();
      // }
    }
  }

  // private setFilteredItems() {
  //   this.filteredItems = this.items.slice((this.page - 1) * this.pageSize, this.page * this.pageSize);
  // }

  onPageChange(page: number) {
    // this.page = page;
    // this.setFilteredItems();
    // this.selectActiveItem(this.filteredItems[0]);
  }

  enableProxy(item: Item) {
    this.wiremockService.enableProxy(item.getId()).subscribe(data => {
        // do nothing
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }

  disableProxy(item: Item) {
    this.wiremockService.disableProxy(item.getId()).subscribe(data => {
        // do nothing
      },
      err => {
        UtilService.showErrorMessage(this.messageService, err);
      });
  }
}
