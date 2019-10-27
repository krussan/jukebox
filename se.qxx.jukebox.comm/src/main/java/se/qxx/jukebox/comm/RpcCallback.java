package se.qxx.jukebox.comm;

import com.google.common.util.concurrent.FutureCallback;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;

public class RpcCallback<T> implements FutureCallback<T> {
    JukeboxResponseListener listener;
    private HandlerCallback<T> callback;

    public RpcCallback(JukeboxResponseListener listener, HandlerCallback<T> callback) {
        this.setCallback(callback);
        this.setListener(listener);
    }

    private JukeboxResponseListener getListener() {
        return listener;
    }

    public void setListener(JukeboxResponseListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSuccess(@NullableDecl T t) {
        if (getListener() != null) {
            JukeboxConnectionMessage msg = new JukeboxConnectionMessage(true, "");
            if (this.getCallback() != null) {
                getCallback().run(t);
            }
            getListener().onRequestComplete(msg);
        }
    }

    @Override
    public void onFailure(Throwable throwable) {
        if (getListener() != null) {
            JukeboxConnectionMessage msg = new JukeboxConnectionMessage(false, throwable.getMessage());
            getListener().onRequestComplete(msg);
        }
    }

    public HandlerCallback<T> getCallback() {
        return callback;
    }

    public void setCallback(HandlerCallback<T> callback) {
        this.callback = callback;
    }
}
