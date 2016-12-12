import java.util.logging.Logger;
import file.FileSystemDataService;
import heartbeat.HeartBeatService;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
public class DataNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(DataNodeServer.class.getName());

    public DataNodeServer(int port) {
        super(logger);
        super.addService(new HeartBeatService()).addService(new FileSystemDataService());
        buildServer(port);
    }
}
