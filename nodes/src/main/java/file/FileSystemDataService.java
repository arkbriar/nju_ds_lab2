package file;

import io.grpc.stub.StreamObserver;
import net.file.ControlledPacket;
import net.file.FileStoreDeviceGrpc;
import net.file.GetFileRequest;
import net.file.PutFileResponse;
import org.redisson.api.RedissonClient;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemDataService extends FileStoreDeviceGrpc.FileStoreDeviceImplBase {
    private RedissonClient redissonClient;

    private Path path;

    public FileSystemDataService(RedissonClient redissonClient, String path) {
        this.redissonClient = redissonClient;
        FileSystem fileSystem = FileSystems.getDefault();
        this.path = fileSystem.getPath(path);
        if (!Files.exists(this.path) || !this.path.toFile().isDirectory()) {
            throw new RuntimeException(path + " doesn't exist or isn't a directory.");
        }
    }

    @Override
    public StreamObserver<ControlledPacket> putFile(
        StreamObserver<PutFileResponse> responseObserver) {
        return super.putFile(responseObserver);
    }

    @Override
    public void getFile(GetFileRequest request, StreamObserver<ControlledPacket> responseObserver) {
        super.getFile(request, responseObserver);
    }
}
