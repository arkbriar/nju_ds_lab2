package services;

import com.google.protobuf.ByteString;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.file.ControlledPacket;
import net.file.FileStoreDeviceGrpc;
import net.file.GetFileRequest;
import net.file.PutFileResponse;
import net.file.TransferCommand;
import org.redisson.api.RedissonClient;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import file.File;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemDataService extends FileStoreDeviceGrpc.FileStoreDeviceImplBase {
    private static Logger logger = Logger.getLogger(FileSystemDataService.class.getName());

    private RedissonClient redissonClient;

    private Path path;

    public FileSystemDataService(RedissonClient redissonClient, String path) {
        this.redissonClient = redissonClient;
        FileSystem fileSystem = FileSystems.getDefault();
        this.path = fileSystem.getPath(path).toAbsolutePath();
        if (!Files.exists(this.path) || !this.path.toFile().isDirectory()) {
            throw new RuntimeException(path + " doesn't exist or isn't a directory.");
        }
    }

    @Override
    public StreamObserver<ControlledPacket> putFile(
        StreamObserver<PutFileResponse> responseObserver) {
        return new StreamObserver<ControlledPacket>() {
            private File file = null;

            private FileOutputStream fileOutputStream = null;

            @Override
            public void onNext(ControlledPacket value) {
                if (file == null) {
                    assert value.getDataCase() == ControlledPacket.DataCase.FILE_TO_CHAP;
                    net.file.File file = value.getFileToChap();
                    this.file = new File(file.getName(), file.getSize(), file.getChecksum());
                    this.file.setUuid(UUID.fromString(file.getUuid()));
                    try {
                        fileOutputStream =
                            new FileOutputStream(path.toString() + "/" + this.file.getUuid());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        throw new StatusRuntimeException(Status.INTERNAL);
                    }
                    responseObserver.onNext(
                        PutFileResponse.newBuilder().setCommand(TransferCommand.INIT).build());
                } else {
                    switch (value.getDataCase()) {
                        case FILE_BLOCK:
                            ByteString bytes = value.getFileBlock().getBlock();
                            try {
                                fileOutputStream.write(bytes.toByteArray());
                            } catch (IOException e) {
                                e.printStackTrace();
                                throw new StatusRuntimeException(Status.INTERNAL);
                            }
                            break;
                        case CHECKSUM:
                            break;
                        case END:
                            responseObserver.onNext(PutFileResponse.newBuilder()
                                                        .setCommand(TransferCommand.CLOSE)
                                                        .build());
                            break;
                        case FILE_TO_CHAP:
                        case DATA_NOT_SET:
                        default:
                            logger.log(Level.WARNING, "Cases are not supported here. Find the "
                                    + "cause!");
                            break;
                    }
                }
            }

            @Override
            public void onError(Throwable t) {
                logger.log(Level.WARNING, t.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    @Override
    public StreamObserver<GetFileRequest> getFile(
        StreamObserver<ControlledPacket> responseObserver) {
        return new StreamObserver<GetFileRequest>() {
            @Override
            public void onNext(GetFileRequest value) {}

            @Override
            public void onError(Throwable t) {}

            @Override
            public void onCompleted() {}
        };
    }
}
