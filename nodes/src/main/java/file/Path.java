package file;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import file.exceptions.InvalidPathException;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class Path {
    private String path;
    private String[] subPaths;
    private String fileName = null;

    public Path(String path) throws InvalidPathException {
        if (!path.startsWith("/")) {
            throw new InvalidPathException("Invalid path " + path);
        }
        this.path = path;
        this.subPaths = path.split("/");
        assert this.subPaths.length > 0;
        this.subPaths = Arrays.copyOfRange(this.subPaths, 1, this.subPaths.length);
        if (!isRoot()) {
            this.fileName = subPaths[subPaths.length - 1];
        }
    }

    protected Path(String path, String[] subPaths) {
        this.path = path;
        this.subPaths = subPaths;
        if (subPaths.length > 0) {
            this.fileName = subPaths[subPaths.length - 1];
        }
    }

    /**
     * Returns the name of the file or directory denoted by this path.
     * @return Name of file or directory.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Returns the parent path, or null if this path does not have a parent.
     * @return The parent path or null.
     */
    public Path getParent() {
        if (isRoot()) {
            return null;
        }
        return new Path(path.substring(0, path.length() - fileName.length() - 1),
            Arrays.copyOf(subPaths, subPaths.length - 1));
    }

    public boolean isRoot() {
        return path.equals("/");
    }

    public final Enumeration<String> subPaths() {
        return Collections.enumeration(Arrays.asList(subPaths));
    }

    @Override
    public String toString() {
        return path;
    }
}
