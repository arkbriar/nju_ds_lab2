package file;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class File extends FileMeta {
    /**
     * Size of this file.
     */
    private long size;

    public File(String name, long size) {
        this.name = name;
        this.size = size;
    }

    @Override
    public boolean isDir() {
        return false;
    }

    public long getSize() {
        return size;
    }

    public static File fromFileMeta(FileMeta fileMeta) {
        if (fileMeta.isDir()) {
            throw new RuntimeException(fileMeta.getName() + " is not a file");
        }
        return (File) fileMeta;
    }
}
