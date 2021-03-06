package services;

import com.google.protobuf.ByteString;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.file.ControlledPacket;
import net.file.FileBlock;
import net.file.FileStoreDeviceGrpc;
import net.file.GetFileRequest;
import net.file.PutFileResponse;
import net.file.TransferCommand;
import org.redisson.api.RedissonClient;

import java.io.FileInputStream;
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

    public FileSystemDataService(RedissonClient redissonClient, String path) throws IOException {
        if (redissonClient == null) {
            throw new NullPointerException();
        }
        this.redissonClient = redissonClient;
        FileSystem fileSystem = FileSystems.getDefault();
        this.path = fileSystem.getPath(path).toAbsolutePath();
        if (!Files.exists(this.path) || !this.path.toFile().isDirectory()) {
            if (this.path.startsWith("/tmp")) {
                logger.info(this.path + " does not exists, creating");
                Files.createDirectory(this.path);
            } else {
                throw new RuntimeException(path + " doesn't exist or isn't a directory.");
            }
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
                        responseObserver.onError(new StatusRuntimeException(Status.INTERNAL));
                    }
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
                        case COMMAND:
                            TransferCommand command = value.getCommand();
                            assert command == TransferCommand.CLOSE;
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
                t.printStackTrace();
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
            private FileInputStream fileInputStream = null;

            @Override
            public void onNext(GetFileRequest value) {
                if (fileInputStream == null) {
                    assert value.getCommand() == TransferCommand.INIT;
                    try {
                        fileInputStream =
                            new FileInputStream(path + "/" + value.getUuid().toStringUtf8());
                        byte[] buffer = new byte[1024];
                        while (fileInputStream.available() > 0) {
                            int readBytes = fileInputStream.read(buffer);
                            responseObserver.onNext(
                                ControlledPacket.newBuilder()
                                    .setFileBlock(FileBlock.newBuilder().setBlock(
                                        ByteString.copyFrom(buffer, 0, readBytes)))
                                    .build());
                        }
                        fileInputStream.close();
                        responseObserver.onNext(ControlledPacket.newBuilder()
                                                    .setCommand(TransferCommand.CLOSE)
                                                    .build());
                    } catch (FileNotFoundException e) {
                        responseObserver.onError(new StatusRuntimeException(Status.INTERNAL));
                    } catch (IOException e) {
                        responseObserver.onError(new StatusRuntimeException(Status.INTERNAL));
                    }
                } else {
                    assert value.getCommand() == TransferCommand.CLOSE;
                    fileInputStream = null;
                    responseObserver.onCompleted();
                }
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
                logger.log(Level.WARNING, t.getLocalizedMessage());
            }

            @Override
            public void onCompleted() {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }
}
