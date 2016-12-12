import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import heartbeat.HeartBeatService;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
public class DataNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(DataNodeServer.class.getName());

    public DataNodeServer(int port) {
        getServiceList().add(new HeartBeatService());
        buildServer(port);
    }
}
