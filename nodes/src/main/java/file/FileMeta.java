package file;

import java.util.Objects;

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

    public void setName(String name) {
        this.name = name;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof FileMeta)) {
            return false;
        }
        final FileMeta other = (FileMeta) obj;
        return Objects.equals(this.name, other.name);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
}
