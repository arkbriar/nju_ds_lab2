package file;

/**
 * Created by Shunjie Ding on 13/12/2016.
 */
public class FileStore {
    private String name;

    private long totalSpace;

    private long unallocatedSpace;

    private long usableSpace;

    public static class FileStoreUrl {
        private String host;
        private int port;

        public FileStoreUrl(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }
    }

    private FileStoreUrl url;

    public FileStore(String name, long totalSpace, long usableSpace, String host, int port) {
        this.name = name;
        this.totalSpace = totalSpace;
        this.unallocatedSpace = usableSpace;
        this.usableSpace = usableSpace;
        this.url = new FileStoreUrl(host, port);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public long getUnallocatedSpace() {
        return unallocatedSpace;
    }

    public void setUnallocatedSpace(long unallocatedSpace) {
        this.unallocatedSpace = unallocatedSpace;
    }

    public long getUsableSpace() {
        return usableSpace;
    }

    public void setUsableSpace(long usableSpace) {
        this.usableSpace = usableSpace;
    }
}
