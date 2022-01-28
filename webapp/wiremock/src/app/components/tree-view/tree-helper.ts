import {Item} from '../../model/wiremock/item';
import {Tree} from '../../model/tree/tree';
import {UtilService} from '../../services/util.service';
import {Folder} from '../../model/tree/folder';
import {TreeNode} from '../../model/tree/tree-node';

export class TreeHelper {

  /**
   * Sort items by folder name. Items with folder definition before items without folder definition.
   * Otherwise folders are sorted alphabetical.
   *
   * @param items
   *        items to sort
   */
  public static sortItemsByFolderName(items: Item[]): void {
    items.sort((a: Item, b: Item) => {
      if (a.hasFolderDefinition() && b.hasFolderDefinition()) {
        if (a.getFolderName() < b.getFolderName()) {
          return -1;
        }
        if (a.getFolderName() > b.getFolderName()) {
          return 1;
        }
        return 0;
      } else if (a.hasFolderDefinition()) {
        return -1;
      } else if (b.hasFolderDefinition()) {
        return 1;
      } else {
        return 0;
      }
    });
  }

  /**
   * Insert an array of items into a tree.
   * @param tree
   *        tree to insert items into
   * @param items
   *        items to insert into the tree
   * @param rootNode
   *        root node in case item is not part of any folder
   */
  public static insertIntoTree(tree: Tree, items: Item[], rootNode: TreeNode): void {
    items.forEach(value => {
      if (value.hasFolderDefinition()) {
        const folderName = value.getFolderName();
        const folderNode = this.createFoldersAndGetFolderNode(folderName, tree, rootNode);
        tree.insertByNode(folderNode, value);
      } else {
        // not part of any folder. So just add to root item.
        tree.insertByNode(rootNode, value);
      }
    });
  }

  /**
   * Create folders based on folder path. At the end return the actual folder for the provided folderText.
   * @param folderText
   *        Text from which folders will be created
   * @param tree
   *        Tree to which folders will be added
   * @param rootNode
   *        Creation start from root node
   *
   * @return the actual folder for the provided folderText.
   * @private
   */
  private static createFoldersAndGetFolderNode(folderText: string, tree: Tree, rootNode: TreeNode): TreeNode {
    const folders = folderText.split('/').filter(i => i);

    let folderParentNode: TreeNode = rootNode;
    let folderId = '';
    folders.forEach((groupName, index) => {
      if (index === 0) {
        folderId = groupName;
      } else {
        folderId = folderId + '/' + groupName;
      }
      let folder = tree.find(folderId);
      if (!UtilService.isDefined(folder)) {
        // folder does not exist yet. Create it
        folder = tree.insertByNode(folderParentNode, new Folder(folderId, groupName));
      }
      // collapse all folders
      folder.collapsed = true;
      folderParentNode = folder;
    });

    return folderParentNode;
  }

  /**
   * Open folders based on an older tree
   * @param newTree
   *        Open folders of the new tree based on the old tree
   * @param oldTree
   *        Provide open folder information for new tree
   * @param activeItem
   *        Also open folders based on active item.
   */
  public static openFolders(newTree: Tree, oldTree: Tree, activeItem: Item): void {
    // open folders again
    if (UtilService.isDefined(oldTree)) {
      for (const node of oldTree.preOrderTraversal()) {
        if (!node.collapsed) {
          const newValue = newTree.find(node.value.getId());
          if (UtilService.isDefined(newValue)) {
            newValue.collapsed = false;
          }
        }
      }
    }
    if (UtilService.isDefined(activeItem)) {
      const nV = newTree.find(activeItem.getId());
      if (UtilService.isDefined(nV)) {
        nV.expandParents();
      }
    }
  }

  /**
   * Sort tree so that items in folders are sorted behind folders. Folders first.
   * @param tree
   *        Tree to sort
   *
   * @return an array representing the provided tree. Sorted in pre order.
   */
  public static sortTreeFoldersFirstAndMapToList(tree: Tree): TreeNode[] {
    const newTreeItems: TreeNode[] = [];
    for (const node of tree.preOrderTraversal()) {
      // we sort children by folder before we add them too tree list.
      node.children.sort((a, b) => {
        if (a.value instanceof Folder && b.value instanceof Folder) {
          return a.value.getFolderName() <= b.value.getFolderName() ? -1 : 1;
        } else if (a.value instanceof Folder) {
          return -1;
        } else if (b.value instanceof Folder) {
          return 1;
        } else {
          return 0;
        }
      });

      newTreeItems.push(node);
    }
    return newTreeItems;
  }
}
