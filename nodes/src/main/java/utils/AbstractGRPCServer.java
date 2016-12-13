package utils;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public abstract class AbstractGRPCServer {
    private final Logger logger;

    private Server server;
    private int port;
    /**
     * serviceList is a list of {@link BindableService} which will be used for this server.
     * This should be initialized before any {@code buildServer} is called.
     */
    private List<BindableService> serviceList = new LinkedList<>();

    protected AbstractGRPCServer(Logger logger) {
        this.logger = logger;
    }

    protected void buildServer(int port) {
        buildServer(ServerBuilder.forPort(port), port);
    }

    void buildServer(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        for (BindableService service : serviceList) {
            serverBuilder = serverBuilder.addService(service);
        }
        server = serverBuilder.build();
    }

    protected List<BindableService> getServiceList() {
        return serviceList;
    }

    public AbstractGRPCServer addService(BindableService service) {
        serviceList.add(service);
        return this;
    }

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
