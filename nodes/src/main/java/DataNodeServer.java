import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.logging.Logger;
import file.FileSystemDataService;
import heartbeat.HeartBeatService;
import utils.AbstractGRPCServer;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
class DataNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(DataNodeServer.class.getName());

    private RedissonClient redissonClient;

    DataNodeServer(int port, Config config) {
        super(logger);
        redissonClient = Redisson.create(config);
        super.addService(new HeartBeatService())
            .addService(new FileSystemDataService(redissonClient));
        buildServer(port);
    }
}
