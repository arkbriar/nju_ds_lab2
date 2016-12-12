import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.logging.Logger;
import file.FileSystemMetaService;
import heartbeat.HeartBeatService;
import utils.AbstractGRPCServer;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
class MasterNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(MasterNodeServer.class.getName());

    private RedissonClient redissonClient;

    MasterNodeServer(int port, Config config) {
        super(logger);
        redissonClient = Redisson.create(config);
        super.addService(new HeartBeatService())
            .addService(new FileSystemMetaService(redissonClient));
        buildServer(port);
    }
}
