package utils;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public abstract class AbstractGRPCServer {
    private final Logger logger;

    private Server server;
    private int port;

    protected AbstractGRPCServer(Logger logger) {
        this.logger = logger;
    }

    protected void buildServer(int port) {
        buildServer(ServerBuilder.forPort(port), port);
    }

    private void buildServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        if (getServiceList() == null) {
            throw new RuntimeException("service list should not be empty");
        }
        for (BindableService service : getServiceList()) {
            serverBuilder = serverBuilder.addService(service);
        }
        server = serverBuilder.build();
    }

    protected abstract List<BindableService> getServiceList();

    /** Start serving requests. */
    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on port " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            AbstractGRPCServer.this.stop();
            System.err.println("*** server shut down");
        }));
    }

    /** Stop serving requests and shutdown resources. */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public void startAndBlock() throws IOException, InterruptedException {
        start();
        blockUntilShutdown();
    }
}
