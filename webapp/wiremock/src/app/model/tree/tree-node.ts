import {Item} from '../wiremock/item';
import {UtilService} from '../../services/util.service';

export class TreeNode {
  constructor(public value: Item, public depth: number, public parent?: TreeNode,
              public children: TreeNode[] = [], public collapsed = false) {
  }

  isLeaf() {
    return this.children.length === 0;
  }

  hasChildren() {
    return !this.isLeaf();
  }

  isHidden() {
    if (UtilService.isDefined(this.parent)) {
      return this.parent.isParentHidden();
    }
    return false;
  }

  private isParentHidden() {
    if (this.collapsed) {
      return true;
    } else if (UtilService.isDefined(this.parent)) {
      return this.parent.isParentHidden();
    }
    return false;
  }

  expandParents() {
    if (UtilService.isDefined(this.parent)) {
      this.parent.collapsed = false;
      this.parent.expandParents();
    }
  }
}
