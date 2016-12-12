package file;

import io.grpc.stub.StreamObserver;
import net.file.CheckExistenceResponse;
import net.file.CopyRequest;
import net.file.CopyResponse;
import net.file.CreateDirectoryResponse;
import net.file.CreateFileMetaResponse;
import net.file.DeleteResponse;
import net.file.FileStore;
import net.file.FileStoreURI;
import net.file.FileSystemGrpc;
import net.file.GetFileMetaResponse;
import net.file.MoveRequest;
import net.file.MoveResponse;
import net.file.Path;
import org.redisson.api.RedissonClient;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemMetaService extends FileSystemGrpc.FileSystemImplBase {
    private RedissonClient redissonClient;

    public FileSystemMetaService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void checkExistence(
        Path request, StreamObserver<CheckExistenceResponse> responseObserver) {
        super.checkExistence(request, responseObserver);
    }

    @Override
    public void copy(CopyRequest request, StreamObserver<CopyResponse> responseObserver) {
        super.copy(request, responseObserver);
    }

    @Override
    public void createDirectory(
        Path request, StreamObserver<CreateDirectoryResponse> responseObserver) {
        super.createDirectory(request, responseObserver);
    }

    @Override
    public void delete(Path request, StreamObserver<DeleteResponse> responseObserver) {
        super.delete(request, responseObserver);
    }

    @Override
    public void deleteIfExists(Path request, StreamObserver<DeleteResponse> responseObserver) {
        super.deleteIfExists(request, responseObserver);
    }

    @Override
    public void move(MoveRequest request, StreamObserver<MoveResponse> responseObserver) {
        super.move(request, responseObserver);
    }

    @Override
    public void getFileStore(FileStoreURI request, StreamObserver<FileStore> responseObserver) {
        super.getFileStore(request, responseObserver);
    }

    @Override
    public void createFileMeta(
        Path request, StreamObserver<CreateFileMetaResponse> responseObserver) {
        super.createFileMeta(request, responseObserver);
    }

    @Override
    public void getFileMeta(Path request, StreamObserver<GetFileMetaResponse> responseObserver) {
        super.getFileMeta(request, responseObserver);
    }
}
