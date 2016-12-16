import io.grpc.BindableService;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import heartbeat.HeartBeatService;
import services.FileSystemDataService;
import utils.AbstractGRPCServer;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
class DataNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(DataNodeServer.class.getName());

    private String path;

    /**
     * serviceList is a list of {@link BindableService} which will be used for this server.
     * This should be initialized before any {@code buildServer} is called.
     */
    private List<BindableService> serviceList = new LinkedList<>();

    DataNodeServer(int port, String path, Config config) {
        super(logger);
        RedissonClient redissonClient = Redisson.create(config);
        serviceList.add(new HeartBeatService());
        serviceList.add(new FileSystemDataService(redissonClient, path));
        buildServer(port);
    }

    @Override
    protected List<BindableService> getServiceList() {
        return serviceList;
    }
}
