import {TreeNode} from './tree-node';
import {Item} from '../wiremock/item';

export class Tree {
  private root: TreeNode;

  constructor(value: Item) {
    this.root = new TreeNode(value, -1);
  }

  * preOrderTraversal(node: TreeNode = this.root): IterableIterator<TreeNode> {
    yield node;
    if (node.children.length) {
      for (const child of node.children) {
        yield* this.preOrderTraversal(child);
      }
    }
  }

  * postOrderTraversal(node = this.root): IterableIterator<TreeNode> {
    if (node.children.length) {
      for (const child of node.children) {
        yield* this.postOrderTraversal(child);
      }
    }
    yield node;
  }

  insert(parentId: string, value: Item): boolean {
    for (const node of this.preOrderTraversal()) {
      if (node.value.getId() === parentId) {
        node.children.push(new TreeNode(value, this.find(parentId).depth + 1, node));
        return true;
      }
    }
    return false;
  }

  insertByNode(parent: TreeNode, value: Item): boolean {
    for (const node of this.preOrderTraversal()) {
      if (node.value.getId() === parent.value.getId()) {
        node.children.push(new TreeNode(value, parent.depth + 1, node));
        return true;
      }
    }
    return false;
  }

  remove(id: string): boolean {
    for (const node of this.preOrderTraversal()) {
      const filtered = node.children.filter(c => c.value.getId() !== id);
      if (filtered.length !== node.children.length) {
        node.children = filtered;
        return true;
      }
    }
    return false;
  }

  find(id: string): TreeNode | undefined {
    for (const node of this.preOrderTraversal()) {
      if (node.value.getId() === id) {
        return node;
      }
    }
    return undefined;
  }
}
