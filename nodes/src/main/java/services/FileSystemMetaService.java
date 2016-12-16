package services;

import com.google.protobuf.ByteString;

import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import net.file.CopyRequest;
import net.file.CopyResponse;
import net.file.CreateDirectoryResponse;
import net.file.CreateFileMetaResponse;
import net.file.DeleteResponse;
import net.file.FileStore;
import net.file.FileSystemError;
import net.file.FileSystemGrpc;
import net.file.GetFileMetaResponse;
import net.file.ListResponse;
import net.file.MoveRequest;
import net.file.MoveResponse;
import net.file.Path;
import net.file.Token;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import file.DirectoryTree;
import file.File;
import file.FileMeta;
import file.exceptions.FileAlreadyExistsException;
import file.exceptions.FileSystemException;
import file.exceptions.InvalidPathException;
import file.exceptions.PathNotFoundException;
import utils.TreeNode;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemMetaService extends FileSystemGrpc.FileSystemImplBase {
    private RedissonClient redissonClient;

    private DirectoryTree directoryTree = new DirectoryTree();

    public FileSystemMetaService(RedissonClient redissonClient) {
        if (redissonClient == null) {
            throw new NullPointerException();
        }
        this.redissonClient = redissonClient;
    }

    private boolean verifyAuthToken(String token) {
        RBucket<String> tokenBucket = redissonClient.getBucket("token/" + token);
        String username = tokenBucket.get();
        return !username.isEmpty();
    }

    @Override
    public void list(Path request, StreamObserver<ListResponse> responseObserver) {
        try {
            file.Path path = new file.Path(request.getPath());
            TreeNode<FileMeta> node = directoryTree.getNode(path);
            if (node == null) {
                responseObserver.onNext(
                    ListResponse.newBuilder()
                        .setError(FileSystemError.newBuilder()
                                      .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                      .setErrorMessage(path.toString() + " does not exists"))
                        .build());
            } else {
                FileMeta fileMeta = node.getValue();
                assert fileMeta != null;
                if (!fileMeta.isDir()) {
                    responseObserver.onNext(
                        ListResponse.newBuilder().addName(fileMeta.getName()).build());
                } else {
                    List<String> fileList = new ArrayList<>();
                    for (TreeNode<FileMeta> child : node.children()) {
                        fileList.add(child.getValue().getName());
                    }
                    responseObserver.onNext(ListResponse.newBuilder().addAllName(fileList).build());
                }
            }
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                ListResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
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
            responseObserver.onNext(CopyResponse.getDefaultInstance());
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
        String path = request.getPath();
        if (path.endsWith("/")) {
            responseObserver.onNext(
                CreateDirectoryResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(path + " isn't a file")
                                  .build())
                    .build());
        }
        try {
            directoryTree.createDirectory(new file.Path(path));
            responseObserver.onNext(CreateDirectoryResponse.getDefaultInstance());
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                CreateDirectoryResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                CreateDirectoryResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
        } catch (FileAlreadyExistsException e) {
            responseObserver.onNext(
                CreateDirectoryResponse.newBuilder()
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
    public void delete(Path request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            file.Path target = new file.Path(request.getPath());
            directoryTree.delete(target, false);
            responseObserver.onNext(DeleteResponse.getDefaultInstance());
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (FileSystemException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(
                        FileSystemError.newBuilder()
                            .setStatus(FileSystemError.FileSystemErrorStatus.DIRECTORY_NOT_EMPTY)
                            .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void deleteIfExists(Path request, StreamObserver<DeleteResponse> responseObserver) {
        try {
            file.Path target = new file.Path(request.getPath());
            directoryTree.deleteIfExists(target, false);
            responseObserver.onNext(DeleteResponse.getDefaultInstance());
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (FileSystemException e) {
            responseObserver.onNext(
                DeleteResponse.newBuilder()
                    .setError(
                        FileSystemError.newBuilder()
                            .setStatus(FileSystemError.FileSystemErrorStatus.DIRECTORY_NOT_EMPTY)
                            .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void move(MoveRequest request, StreamObserver<MoveResponse> responseObserver) {
        try {
            file.Path srcPath = new file.Path(request.getSrc().getPath());
            file.Path destPath = new file.Path(request.getDest().getPath());
            directoryTree.move(srcPath, destPath);
            responseObserver.onNext(MoveResponse.getDefaultInstance());
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                MoveResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                MoveResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void createFileMeta(
        net.file.File request, StreamObserver<CreateFileMetaResponse> responseObserver) {
        try {
            file.Path filePath = new file.Path(request.getPath().getPath());
            File file = new File(filePath.getFileName(), request.getSize(), request.getChecksum());
            directoryTree.createFile(filePath, file);
            responseObserver.onNext(CreateFileMetaResponse.newBuilder()
                                        .setFile(
                                            // TODO(aribriar@gmail.com) set store url.
                                            net.file.File.newBuilder(request)
                                                .setUuid(file.getUuid().toString())
                                                .build())
                                        .build());
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                CreateFileMetaResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (PathNotFoundException e) {
            responseObserver.onNext(
                CreateFileMetaResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                  .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        } catch (FileAlreadyExistsException e) {
            responseObserver.onNext(
                CreateFileMetaResponse.newBuilder()
                    .setError(
                        FileSystemError.newBuilder()
                            .setStatus(FileSystemError.FileSystemErrorStatus.FILE_ALREADY_EXSITS)
                            .setErrorMessage(e.getLocalizedMessage()))
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getFileMeta(Path request, StreamObserver<GetFileMetaResponse> responseObserver) {
        try {
            file.Path filePath = new file.Path(request.getPath());
            FileMeta fileMeta = directoryTree.getFile(filePath);
            if (fileMeta == null || fileMeta.isDir()) {
                responseObserver.onNext(
                    GetFileMetaResponse.newBuilder()
                        .setError(
                            FileSystemError.newBuilder()
                                .setStatus(FileSystemError.FileSystemErrorStatus.NO_SUCH_FILE)
                                .setErrorMessage(filePath + " doesn't exists or is a directory.")
                                .build())
                        .build());
            } else {
                File file = File.fromFileMeta(fileMeta);
                responseObserver.onNext(
                    GetFileMetaResponse.newBuilder()
                        .setFile(
                            // TODO(arkbriar@gmail.com) add store url
                            net.file.File.newBuilder()
                                .setName(fileMeta.getName())
                                .setPath(Path.newBuilder().setPath(filePath.toString()))
                                .setSize(file.getSize())
                                .setUuid(file.getUuid().toString())
                                .setChecksum(file.getChecksum())
                                .build())
                        .build());
            }
        } catch (InvalidPathException e) {
            responseObserver.onNext(
                GetFileMetaResponse.newBuilder()
                    .setError(FileSystemError.newBuilder()
                                  .setStatus(FileSystemError.FileSystemErrorStatus.INVALID_PATH)
                                  .setErrorMessage(e.getLocalizedMessage())
                                  .build())
                    .build());
        }
        responseObserver.onCompleted();
    }

    @Override
    public void requestAccessToken(Path request, StreamObserver<Token> responseObserver) {
        try {
            file.Path filePath = new file.Path(request.getPath());
            FileMeta fileMeta = directoryTree.getFile(filePath);
            if (fileMeta == null || fileMeta.isDir()) {
                responseObserver.onNext(Token.getDefaultInstance());
            } else {
                String token = DigestUtils.md5Hex(request.getPath());
                RReadWriteLock rwLock = redissonClient.getReadWriteLock(token);
                rwLock.writeLock().forceUnlockAsync().await(1000 * 3600);
                responseObserver.onNext(
                    Token.newBuilder().setToken(ByteString.copyFromUtf8(token)).build());
            }
        } catch (InvalidPathException e) {
            responseObserver.onNext(Token.getDefaultInstance());
        } catch (InterruptedException e) {
            // do nothing
        }
        responseObserver.onCompleted();
    }
}
