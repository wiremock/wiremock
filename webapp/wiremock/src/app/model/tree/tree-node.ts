import {Item} from '../wiremock/item';

export class TreeNode {
  constructor(public value: Item, public parent?: TreeNode, public children: TreeNode[] = []) {
  }

  isLeaf() {
    return this.children.length === 0;
  }

  hasChildren() {
    return !this.isLeaf();
  }
}
