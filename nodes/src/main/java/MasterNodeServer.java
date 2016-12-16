import io.grpc.BindableService;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.ServerServiceDefinition;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import services.FileSystemAuthService;
import services.FileSystemMetaService;
import heartbeat.HeartBeatService;
import services.interceptors.FileSystemAuthInterceptor;
import utils.AbstractGRPCServer;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
class MasterNodeServer extends AbstractGRPCServer {
    private static final Logger logger = Logger.getLogger(MasterNodeServer.class.getName());

    /**
     * serviceList is a list of {@link BindableService} which will be used for this server.
     * This should be initialized before any {@code buildServer} is called.
     */
    private List<BindableService> serviceList = new LinkedList<>();

    MasterNodeServer(int port, Config config) {
        super(logger);
        RedissonClient redissonClient = Redisson.create(config);
        serviceList.add(new HeartBeatService());
        serviceList.add(new BindableService() {
            @Override
            public ServerServiceDefinition bindService() {
                return ServerInterceptors.intercept(new FileSystemMetaService(redissonClient),
                    new FileSystemAuthInterceptor(redissonClient));
            }
        });
        serviceList.add(new FileSystemAuthService(redissonClient));
        buildServer(port);
    }

    @Override
    protected List<BindableService> getServiceList() {
        return serviceList;
    }
}
