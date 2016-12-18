package services.interceptors;

import com.google.common.annotations.VisibleForTesting;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.SimpleForwardingServerCallListener;

/**
 * Created by Shunjie Ding on 14/12/2016.
 */
public class FileSystemAuthInterceptor implements ServerInterceptor {
    private static Logger logger = Logger.getLogger(FileSystemAuthInterceptor.class.getName());

    private final RedissonClient redissonClient;

    @VisibleForTesting
    static final Metadata.Key<String> TokenHeader =
        Metadata.Key.of("TOKEN", Metadata.ASCII_STRING_MARSHALLER);

    public FileSystemAuthInterceptor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * Intercept {@link ServerCall} dispatch by the {@code next} {@link ServerCallHandler}. General
     * semantics of {@link ServerCallHandler#startCall} apply and the returned
     * {@link ServerCall.Listener} must not be {@code null}.
     *
     * <p>If the implementation throws an exception, {@code call} will be closed with an error.
     * Implementations must not throw an exception if they started processing that may use {@code
     * call} on another thread.
     * @param call object to receive response messages
     * @param next next processor in the interceptor chain  @return listener for processing incoming
     * messages for {@code call}, never {@code null}.
     */
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        logger.log(Level.OFF, "header received from client:" + headers);
        String token = headers.get(TokenHeader);
        ServerCall.Listener<ReqT> delegateListener = next.startCall(call, headers);
        boolean tokenValid = false;
        if (token != null && !token.isEmpty()) {
            RBucket<String> tokenBucket =
                redissonClient.getBucket("token/" + token, new StringCodec());
            if (tokenBucket.isExists() && !tokenBucket.get().isEmpty()) {
                tokenBucket.expireAsync(5, TimeUnit.MINUTES);
                tokenValid = true;
            }
        }
        if (!tokenValid) {
            return new SimpleForwardingServerCallListener<ReqT>(delegateListener) {
                /**
                 * The client completed all message sending. However, the call may still be
                 * cancelled.
                 */
                @Override
                public void onHalfClose() {
                    throw Status.UNAUTHENTICATED
                        .withDescription("Token invalid or may be expired, please relogin.")
                        .asRuntimeException();
                }
            };
        }
        return delegateListener;
    }
}
