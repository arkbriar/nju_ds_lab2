package file;

import java.util.Date;
import java.util.UUID;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public abstract class FileMeta {
    /**
     * Name of this file/directory.
     */
    protected String name;
    /**
     * Modification time of this file, measured in milliseconds in format of UTC time.
     */
    protected long modificationTime;

    public String getName() {
        return name;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public abstract boolean isDir();
}
