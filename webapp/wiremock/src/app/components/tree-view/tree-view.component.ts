import {
  AfterViewChecked, AfterViewInit,
  Component, ElementRef,
  EventEmitter,
  HostBinding,
  Input,
  OnChanges,
  OnInit,
  Output, QueryList,
  SimpleChanges, ViewChild,
  ViewChildren
} from '@angular/core';
import {Item} from '../../model/wiremock/item';
import {UtilService} from '../../services/util.service';
import {WiremockService} from '../../services/wiremock.service';
import {MessageService} from '../message/message.service';
import {Tree} from '../../model/tree/tree';
import {Root} from '../../model/tree/root';
import {TreeNode} from '../../model/tree/tree-node';
import {StubMapping} from '../../model/wiremock/stub-mapping';
import {Folder} from '../../model/tree/folder';
import {SearchService} from '../../services/search.service';

@Component({
  selector: 'wm-tree-view',
  templateUrl: './tree-view.component.html',
  styleUrls: [ './tree-view.component.scss' ]
})
export class TreeViewComponent implements OnInit, OnChanges, AfterViewInit, AfterViewChecked {

  @HostBinding('class') classes = 'wmHolyGrailBody column';

  @Input()
  items: Item[];

  @Input()
  activeItem: Item;
  activeItemChanged = false;

  pageSize = 20;

  page = 1;

  @Output()
  activeItemChange: EventEmitter<Item> = new EventEmitter();

  tree: Tree;
  rootNode: TreeNode;
  private root: Item;

  treeItems: TreeNode[];

  @ViewChild('childrenContainer')
  childrenContainer: ElementRef;

  @ViewChildren('listChildren')
  listChildren: QueryList<ElementRef>;

  constructor(private wiremockService: WiremockService,
              private messageService: MessageService,
              private searchService: SearchService) {
  }

  selectActiveItem(node: TreeNode) {
    if (node.value instanceof Folder) {
      node.collapsed = !node.collapsed;
      return;
    }

    if (this.activeItem === node.value) {
      return;
    }
    this.activeItem = node.value;
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

      // console.log(this.items);

      const newTree = new Tree(this.root);
      this.rootNode = newTree.find(this.root.getId());

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
            const groupNode = newTree.find(groupId);
            if (!UtilService.isDefined(groupNode)) {
              // group does not exist yet. Create it
              newTree.insert(groupParent.getId(), new Folder(groupId, groupName));
            }
            const folder = newTree.find(groupId);
            // TODO: Not sure how to do that with available information. We have no clue here what triggered the
            //  rendering. And I actually want to keep it that way. But we have:
            //  Focus active item seems useful as long as we do not prevent usage
            //  Show search results. Not all? Just at least one. But this could be active one.
            // folder.collapsed = this.items.length >= 10;
            folder.collapsed = true;
            groupParent = folder.value;
          });


          const parent = newTree.find(groupText);

          if (UtilService.isDefined(parent)) {
            // found group
            newTree.insertByNode(parent, value);
          } else {
            // create group folder
            console.log('oO Something went wrong!!!');
          }
        } else {
          newTree.insert(this.root.getId(), value);
        }
      });

      // open folders again
      if (UtilService.isDefined(this.tree)) {
        for (const node of this.tree.preOrderTraversal()) {
          if (!node.collapsed) {
            const newValue = newTree.find(node.value.getId());
            if (UtilService.isDefined(newValue)) {
              newValue.collapsed = false;
            }
          }
        }
      }
      const nV = newTree.find(this.activeItem.getId());
      if (UtilService.isDefined(nV)) {
        nV.expandParents();
      }


      this.tree = newTree;
      this.treeItems = Array.from(this.tree.preOrderTraversal());

      this.activeItemChanged = true;
    }
  }

  ngAfterViewInit(): void {
  }

  ngAfterViewChecked(): void {
    if (this.activeItemChanged) {
      // only once after something changed.
      this.listChildren.forEach(item => {
        if (item.nativeElement.id === this.activeItem.getId()) {
          const rectElem = item.nativeElement.getBoundingClientRect();
          const rectContainer = this.childrenContainer.nativeElement.getBoundingClientRect();
          if (rectElem.bottom > rectContainer.bottom) {
            item.nativeElement.scrollIntoView({behavior: 'smooth', block: 'end'});
          } else if (rectElem.top < rectContainer.top) {
            item.nativeElement.scrollIntoView({behavior: 'smooth', block: 'start'});
          }
          this.activeItemChanged = false;
        }
      });
    }
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
