import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;
import heartbeat.HeartBeatService;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
public class MasterNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(MasterNodeServer.class.getName());

    public MasterNodeServer(int port) {
        getServiceList().add(new HeartBeatService());
        buildServer(port);
    }
}
