package se.qxx.jukebox.comm;

import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;

public class EmptyRpcCallback extends RpcCallback<Empty> {
    public EmptyRpcCallback() {
        super(null);
    }

    public EmptyRpcCallback(JukeboxResponseListener<Empty> listener) {
        super(listener);
    }
}
