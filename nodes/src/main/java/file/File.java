package file;

import com.google.protobuf.ByteString;

import java.util.UUID;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class File extends FileMeta {
    /**
     * Size of this file.
     */
    private long size;

    private ByteString checksum;

    private UUID uuid;

    private FileStore.FileStoreUrl url = null;

    public File(String name, long size, ByteString checksum) {
        this.name = name;
        this.size = size;
        this.checksum = checksum;
        this.uuid = UUID.randomUUID();
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

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public ByteString getChecksum() {
        return checksum;
    }

    public void setChecksum(ByteString checksum) {
        this.checksum = checksum;
    }

    public FileStore.FileStoreUrl getUrl() {
        return url;
    }

    public void setUrl(FileStore.FileStoreUrl url) {
        this.url = url;
    }
}
