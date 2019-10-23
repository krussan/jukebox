package se.qxx.jukebox.comm;

import io.grpc.stub.StreamObserver;
import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain;

public class RpcCallback<T> implements StreamObserver<T> {
    JukeboxResponseListener<T> listener;

    public RpcCallback(JukeboxResponseListener<T> listener) {
        this.setListener(listener)
    }

    private JukeboxResponseListener<T> getListener() {
        return listener;
    }

    public void setListener(JukeboxResponseListener<T> listener) {
        this.listener = listener;
    }

    @Override
    public void onNext(T t) {
        if (getListener() != null) {
            JukeboxConnectionMessage msg = new JukeboxConnectionMessage(true, "");
            getListener().onDataReceived(t);
            getListener().onRequestComplete(msg);

        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (getListener() != null) {
            JukeboxConnectionMessage msg = new JukeboxConnectionMessage(false, throwable.getMessage());
            getListener().onRequestComplete(msg);
        }
    }

    @Override
    public void onCompleted() {

    }


}
