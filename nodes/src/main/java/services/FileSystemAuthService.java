package services;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import dfs.auth.AuthGrpc;
import dfs.auth.LoginRequest;
import dfs.auth.LoginResponse;

/**
 * Created by Shunjie Ding on 14/12/2016.
 */
public class FileSystemAuthService extends AuthGrpc.AuthImplBase {
    private RedissonClient redissonClient;

    public FileSystemAuthService(RedissonClient redissonClient) {
        if (redissonClient == null) {
            throw new NullPointerException();
        }
        this.redissonClient = redissonClient;
    }

    private static final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        String username = request.getUsername();
        String password = request.getPassword();
        RBucket<String> passwordBucket = redissonClient.getBucket("user/" + username + "/password");
        String passwordStored = passwordBucket.get();
        if (passwordStored.equals(DigestUtils.md5Hex(password))) {
            RAtomicLong loginTimes =
                redissonClient.getAtomicLong("user/" + username + "/login_count");
            String token = DigestUtils.md5Hex(UUID.randomUUID().toString());
            RBucket<String> tokenBucket = redissonClient.getBucket("token/" + token);
            tokenBucket.set(username, 5, TimeUnit.MINUTES);
            responseObserver.onNext(LoginResponse.newBuilder()
                                        .setToken(token)
                                        .setTime(simpleDateFormat.format(new Date()))
                                        .setLoginTimes(loginTimes.getAndIncrement())
                                        .build());
            responseObserver.onCompleted();
        } else {
            Metadata trailer = new Metadata();
            if (passwordStored.isEmpty()) {
                trailer.put(
                    Metadata.Key.of("error", Metadata.ASCII_STRING_MARSHALLER), "user not found");
            } else {
                trailer.put(Metadata.Key.of("error", Metadata.ASCII_STRING_MARSHALLER),
                    "password not equal");
            }
            throw new StatusRuntimeException(Status.UNAUTHENTICATED, trailer);
        }
    }
}
