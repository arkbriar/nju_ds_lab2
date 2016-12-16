package utils;

import com.sun.istack.internal.NotNull;
import io.grpc.ServerCall;

/**
 * Created by Shunjie Ding on 16/12/2016.
 */
public abstract class SimpleForwardingServerCallListener<ReqT> extends ServerCall.Listener<ReqT> {
    private ServerCall.Listener<ReqT> delegate;

    public SimpleForwardingServerCallListener(@NotNull ServerCall.Listener<ReqT> delegate) {
        super();
        this.delegate = delegate;
    }

    protected ServerCall.Listener<ReqT> delegate() {
        return delegate;
    }

    /**
     * A request message has been received. For streaming calls, there may be zero or more request
     * messages.
     * @param message a received request message.
     */
    @Override
    public void onMessage(ReqT message) {
        delegate.onMessage(message);
    }

    /**
     * The client completed all message sending. However, the call may still be cancelled.
     */
    @Override
    public void onHalfClose() {
        delegate.onHalfClose();
    }

    /**
     * The call was cancelled and the server is encouraged to abort processing to save resources,
     * since the client will not process any further messages. Cancellations can be caused by
     * timeouts, explicit cancellation by the client, network errors, etc.
     *
     * <p>There will be no further callbacks for the call.
     */
    @Override
    public void onCancel() {
        delegate.onCancel();
    }

    /**
     * The call is considered complete and {@link #onCancel} is guaranteed not to be called.
     * However, the client is not guaranteed to have received all messages.
     *
     * <p>There will be no further callbacks for the call.
     */
    @Override
    public void onComplete() {
        delegate.onComplete();
    }

    /**
     * This indicates that the call is now capable of sending additional messages
     * without requiring excessive buffering internally. This event is
     * just a suggestion and the application is free to ignore it, however doing so may
     * result in excessive buffering within the call.
     */
    @Override
    public void onReady() {
        delegate.onReady();
    }
}
