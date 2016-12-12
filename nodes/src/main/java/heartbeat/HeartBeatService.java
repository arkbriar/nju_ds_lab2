package heartbeat;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.heartbeat.HeartBeatGrpc;
import net.heartbeat.HeartBeatRequest;
import net.heartbeat.HeartBeatResponse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Shunjie Ding on 10/12/2016.
 */
public class HeartBeatService extends HeartBeatGrpc.HeartBeatImplBase {
    private static final Logger logger = Logger.getLogger(HeartBeatService.class.getName());

    private static final HeartBeatResponse StatusUnknown =
        HeartBeatResponse.newBuilder().setStatus(HeartBeatResponse.ServingStatus.UNKNOMN).build();
    private static final HeartBeatResponse StatusServing =
        HeartBeatResponse.newBuilder().setStatus(HeartBeatResponse.ServingStatus.SERVING).build();
    private static final HeartBeatResponse StatusNotServing =
        HeartBeatResponse.newBuilder()
            .setStatus(HeartBeatResponse.ServingStatus.NOT_SERVING)
            .build();

    public StreamObserver<HeartBeatRequest> checkHealth(
        StreamObserver<HeartBeatResponse> responseObserver) {
        // TODO(arkbriar@gmail.com) Implement this StreamObserver.
        return new StreamObserver<HeartBeatRequest>() {
            @Override
            public void onNext(HeartBeatRequest value) {
                // If the service name is empty, it stands for all services.
                if (value.getService().isEmpty()) {
                    responseObserver.onNext(StatusServing);
                } else {
                    throw new StatusRuntimeException(Status.NOT_FOUND.withDescription(
                        "Service " + value.getService() + " could not be found."));
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, "check health failed: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
