package file;

import io.grpc.stub.StreamObserver;
import net.file.ControlledPacket;
import net.file.FileStoreDeviceGrpc;
import net.file.GetFileRequest;
import net.file.PutFileResponse;

/**
 * Created by Shunjie Ding on 12/12/2016.
 */
public class FileSystemDataService extends FileStoreDeviceGrpc.FileStoreDeviceImplBase {
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
