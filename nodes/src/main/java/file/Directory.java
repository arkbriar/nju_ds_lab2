package file;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class Directory extends FileMeta {
    public Directory(String name) {
        this.name = name;
    }

    @Override
    public boolean isDir() {
        return true;
    }

    public static Directory fromFileMeta(FileMeta fileMeta) {
        if (!fileMeta.isDir()) {
            throw new RuntimeException(fileMeta.getName() + " is not a dir.");
        }
        return (Directory) fileMeta;
    }
}
