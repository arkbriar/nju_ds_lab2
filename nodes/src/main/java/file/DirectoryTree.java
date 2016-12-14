package file;

import java.util.Enumeration;
import java.util.Set;
import file.exceptions.FileAlreadyExistsException;
import file.exceptions.FileSystemException;
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
    private final TreeNode<FileMeta> root = new TreeNode<FileMeta>(null, new Directory("/"));

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

    public TreeNode<FileMeta> getNode(Path path) {
        if (path == null) {
            return null;
        }
        if (path.isRoot()) {
            return root;
        }
        TreeNode<FileMeta> current = root;
        Enumeration<String> subPaths = path.subPaths();
        assert subPaths != null;
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

    public FileMeta createDirectory(Path path)
        throws PathNotFoundException, FileAlreadyExistsException {
        TreeNode<FileMeta> node = getNode(path.getParent());
        if (node == null) {
            throw new PathNotFoundException(path + " is not found");
        }
        if (getNextNode(node, path.getFileName()) != null) {
            throw new FileAlreadyExistsException(path + " is already exists");
        }
        return node.addChild(new Directory(path.getFileName())).getValue();
    }

    private TreeNode<FileMeta> createIntermediateDirectories(Path path) {
        TreeNode<FileMeta> node = root;
        Enumeration<String> subPaths = path.getParent().subPaths();
        assert subPaths != null;
        while (subPaths.hasMoreElements()) {
            String subPath = subPaths.nextElement();
            node = getNextNode(node, subPath);
            if (node != null) {
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
    public FileMeta createDirectory(Path path, boolean force)
        throws FileAlreadyExistsException, PathNotFoundException {
        if (force) {
            TreeNode<FileMeta> node = createIntermediateDirectories(path);
            if (getNextNode(node, path.getFileName()) != null) {
                throw new FileAlreadyExistsException(path + " is already exists");
            } else {
                return node.addChild(new Directory(path.getFileName())).getValue();
            }
        } else {
            return createDirectory(path);
        }
    }

    public FileMeta createFile(Path path, File file)
        throws PathNotFoundException, FileAlreadyExistsException {
        TreeNode<FileMeta> node = getNode(path.getParent());
        if (node == null) {
            throw new PathNotFoundException(path + " is not found");
        }
        if (getNextNode(node, path.getFileName()) != null) {
            throw new FileAlreadyExistsException(path + " is already exists");
        }
        file.setName(path.getFileName());
        return node.addChild(file).getValue();
    }

    public FileMeta createFile(Path path, File file, boolean force)
        throws FileAlreadyExistsException, PathNotFoundException {
        if (force) {
            TreeNode<FileMeta> node = createIntermediateDirectories(path);
            if (getNextNode(node, path.getFileName()) != null) {
                throw new FileAlreadyExistsException(path + " is already exists");
            } else {
                file.setName(path.getFileName());
                return node.addChild(file).getValue();
            }
        } else {
            return createFile(path, file);
        }
    }

    public boolean delete(Path path, boolean recursive) throws FileSystemException {
        TreeNode<FileMeta> node = getNode(path);
        if (node == null) {
            throw new PathNotFoundException(path + " is not found");
        }
        if (recursive || !node.getValue().isDir()) {
            return node.getParent().removeChild(node);
        } else {
            throw new FileSystemException(path + " is a directory");
        }
    }

    public boolean deleteIfExists(Path path, boolean recursive) throws FileSystemException {
        TreeNode<FileMeta> node = getNode(path);
        if (node == null) {
            return false;
        }
        if (recursive || !node.getValue().isDir()) {
            return node.getParent().removeChild(node);
        } else {
            throw new FileSystemException(path + " is a directory");
        }
    }

    public void move(Path src, Path dest) throws PathNotFoundException {
        TreeNode<FileMeta> srcNode = getNode(src);
        TreeNode<FileMeta> destNode = getNode(dest.getParent());
        if (srcNode == null) {
            throw new PathNotFoundException(src + " is not found");
        }
        if (destNode == null) {
            throw new PathNotFoundException(dest + " is not found");
        }
        srcNode.getParent().removeChild(srcNode);
        srcNode.getValue().setName(dest.getFileName());
        destNode.addChild(srcNode);
    }
}
