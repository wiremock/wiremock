import {TreeNode} from './tree-node';
import {Item} from '../wiremock/item';

export class Tree {
  private root: TreeNode;

  private constructor(node: TreeNode) {
    this.root = node;
  }

  public static createFromItem(value: Item) {
    return new Tree(new TreeNode(value, -1));
  }

  public static createFromNode(node: TreeNode) {
    node.depth = -1;
    return new Tree(node);
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

  insert(parentId: string, value: Item): TreeNode {
    for (const node of this.preOrderTraversal()) {
      if (node.value.getId() === parentId) {
        const newNode = new TreeNode(value, this.find(parentId).depth + 1, node);
        node.children.push(newNode);
        return newNode;
      }
    }
    return undefined;
  }

  insertByNode(parent: TreeNode, value: Item): TreeNode {
    const newNode = new TreeNode(value, parent.depth + 1, parent);
    parent.children.push(newNode);
    return newNode;
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
