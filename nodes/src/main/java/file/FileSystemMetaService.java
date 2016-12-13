package file;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.file.CheckExistenceResponse;
import net.file.CopyRequest;
import net.file.CopyResponse;
import net.file.CreateDirectoryResponse;
import net.file.CreateFileMetaResponse;
import net.file.DeleteResponse;
import net.file.FileStore;
import net.file.FileStoreURL;
import net.file.FileSystemError;
import net.file.FileSystemGrpc;
import net.file.GetFileMetaResponse;
import net.file.MoveRequest;
import net.file.MoveResponse;
import net.file.Path;
import org.redisson.api.RedissonClient;

import file.exceptions.FileAlreadyExistsException;
import file.exceptions.InvalidPathException;
import file.exceptions.PathNotFoundException;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemMetaService extends FileSystemGrpc.FileSystemImplBase {
    private RedissonClient redissonClient;

    private DirectoryTree directoryTree = new DirectoryTree();

    public FileSystemMetaService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void checkExistence(
        Path request, StreamObserver<CheckExistenceResponse> responseObserver) {
        try {
            file.Path path = new file.Path(request.getPath());
            FileMeta fileMeta = directoryTree.getFile(path);
            CheckExistenceResponse checkExistenceResponse =
                CheckExistenceResponse.newBuilder().setExists(fileMeta != null).build();
            responseObserver.onNext(checkExistenceResponse);
        } catch (InvalidPathException e) {
            CheckExistenceResponse checkExistenceResponse =
                CheckExistenceResponse.newBuilder()
                    .setExists(false)
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build();
            responseObserver.onNext(checkExistenceResponse);
            // throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void copy(CopyRequest request, StreamObserver<CopyResponse> responseObserver) {
        String srcPath = request.getSrc().getPath();
        String destPath = request.getDest().getPath();
        if (srcPath.endsWith("/")) {
            throw new StatusRuntimeException(Status.UNIMPLEMENTED);
        }
        try {
            file.Path src = new file.Path(srcPath);
            if (destPath.endsWith("/")) {
                destPath = destPath + "/" + src.getFileName();
            }
            file.Path dest = new file.Path(destPath);
            FileMeta fileMeta = directoryTree.getFile(src);
            if (fileMeta == null || fileMeta.isDir()) {
                responseObserver.onNext(
                    CopyResponse.newBuilder()
                        .setError(
                            FileSystemError.newBuilder()
                                .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                .setErrorMessage("file " + srcPath + " is not found or not a file")
                                .build())
                        .build());
                // throw new StatusRuntimeException(Status.NOT_FOUND);
            }
            directoryTree.createFile(dest, File.fromFileMeta(fileMeta));
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                CopyResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
            // throw new StatusRuntimeException(Status.INVALID_ARGUMENT);
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                CopyResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
        } catch (FileAlreadyExistsException e) {
            responseObserver.onNext(
                CopyResponse.newBuilder()
                    .setError(
                        FileSystemError.newBuilder()
                            .setStatus(FileSystemError.FileSystemErrorStatus.FILE_ALREADY_EXSITS)
                            .setErrorMessage(e.getLocalizedMessage())
                            .build())
                    .build());
        }

        responseObserver.onCompleted();
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
    public void getFileStore(FileStoreURL request, StreamObserver<FileStore> responseObserver) {
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
