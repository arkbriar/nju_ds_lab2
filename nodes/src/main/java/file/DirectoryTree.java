package file;

import java.util.Enumeration;
import java.util.Set;
import file.exceptions.FileAlreadyExistsException;
import file.exceptions.PathNotFoundException;
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

    private TreeNode<FileMeta> getNextNode(TreeNode<FileMeta> current, String subPath) {
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

    private TreeNode<FileMeta> getNode(Path path) {
        TreeNode<FileMeta> current = root;
        Enumeration<String> subPaths = path.subPaths();
        while (subPaths.hasMoreElements()) {
            String subPath = subPaths.nextElement();
            current = getNextNode(current, subPath);
            if (current == null || !current.getValue().isDir()) {
                return null;
            }
        }
        return current;
    }

    /**
     * Returns file meta of given path, returns null when file or directory is not found.
     * @param path Path of the file or directory.
     * @return File meta of the file or directory.
     */
    public FileMeta getFile(Path path) {
        TreeNode<FileMeta> node = getNode(path);
        if (node == null) {
            return null;
        }
        return node.getValue();
    }

    public void createDirectory(Path path)
        throws PathNotFoundException, FileAlreadyExistsException {
        TreeNode<FileMeta> node = getNode(path.getParent());
        if (node == null) {
            throw new PathNotFoundException(path + " is not found");
        }
        if (getNextNode(node, path.getFileName()) != null) {
            throw new FileAlreadyExistsException(path + " is already exists");
        }
        node.addChild(new Directory(path.getFileName()));
    }

    private TreeNode<FileMeta> createIntermediateDirectories(Path path) {
        TreeNode<FileMeta> node = root;
        Enumeration<String> subPaths = path.getParent().subPaths();
        while (subPaths.hasMoreElements()) {
            String subPath = subPaths.nextElement();
            node = getNextNode(node, subPath);
            if (node == null) {
                node = node.addChild(new Directory(subPath));
            }
        }
        assert node != null;
        return node;
    }

    /**
     * Create intermediate directories as required when {@code force} is true.
     * @param path Path of the directory.
     * @param force Boolean option described as above.
     * @throws FileAlreadyExistsException Thrown when there is already a file or directory.
     * @throws PathNotFoundException Thrown when {@code force} is false and intermediate path is
     * not found.
     */
    public void createDirectory(Path path, boolean force)
        throws FileAlreadyExistsException, PathNotFoundException {
        if (force) {
            TreeNode<FileMeta> node = createIntermediateDirectories(path);
            if (getNextNode(node, path.getFileName()) != null) {
                throw new FileAlreadyExistsException(path + " is already exists");
            } else {
                node.addChild(new Directory(path.getFileName()));
            }
        } else {
            createDirectory(path);
        }
    }

    public void createFile(Path path, File file)
        throws PathNotFoundException, FileAlreadyExistsException {
        TreeNode<FileMeta> node = getNode(path.getParent());
        if (node == null) {
            throw new PathNotFoundException(path + " is not found");
        }
        if (getNextNode(node, path.getFileName()) != null) {
            throw new FileAlreadyExistsException(path + " is already exists");
        }
        file.setName(path.getFileName());
        node.addChild(file);
    }

    public void createFile(Path path, File file, boolean force)
        throws FileAlreadyExistsException, PathNotFoundException {
        if (force) {
            TreeNode<FileMeta> node = createIntermediateDirectories(path);
            if (getNextNode(node, path.getFileName()) != null) {
                throw new FileAlreadyExistsException(path + " is already exists");
            } else {
                file.setName(path.getFileName());
                node.addChild(file);
            }
        } else {
            createFile(path, file);
        }
    }
}
