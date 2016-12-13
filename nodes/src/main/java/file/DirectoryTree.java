package file;

import java.util.Enumeration;
import java.util.Set;
import utils.TreeNode;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */

/**
 * DirectoryTree is used to represent the tree structure of file system.
 * The node of this tree carries either {@link File} or {@link Directory}.
 */
public class DirectoryTree {
    /**
     * Root of this directory tree. It must carry a Directory with name "/".
     */
    private final TreeNode<FileMeta> root = new TreeNode(null, new Directory("/"));

    public TreeNode getRoot() {
        return root;
    }

    private TreeNode<FileMeta> getNext(TreeNode<FileMeta> current, String subPath) {
        if (current.isLeaf()) {
            return null;
        }
        Set<TreeNode<FileMeta>> children = current.children();
        for (TreeNode<FileMeta> child : children) {
            FileMeta fileMeta = child.getValue();
            if (fileMeta.name.equals(subPath)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns file meta of given path, returns null when file or directory is not found.
     * @param path Path of the file or directory.
     * @return File meta of the file or directory.
     */
    public FileMeta getFile(Path path) {
        TreeNode<FileMeta> current = root;
        Enumeration<String> subPaths = path.subPaths();
        while (subPaths.hasMoreElements()) {
            String subPath = subPaths.nextElement();
            current = getNext(current, subPath);
            if (current == null || !current.getValue().isDir()) {
                return null;
            }
        }
        return current.getValue();
    }
}
