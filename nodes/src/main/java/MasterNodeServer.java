import java.util.logging.Logger;
import file.FileSystemMetaService;
import heartbeat.HeartBeatService;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
public class MasterNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(MasterNodeServer.class.getName());

    public MasterNodeServer(int port) {
        super(logger);
        super.addService(new HeartBeatService()).addService(new FileSystemMetaService());
        buildServer(port);
    }
}
