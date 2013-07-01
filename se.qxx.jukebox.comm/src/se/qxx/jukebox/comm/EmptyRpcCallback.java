package se.qxx.jukebox.comm;

import com.google.protobuf.RpcCallback;

import se.qxx.jukebox.domain.JukeboxDomain.Empty;

public class EmptyRpcCallback implements RpcCallback<Empty> {

	@Override
	public void run(Empty arg0) {
		// Left intentionally blank, since.... ITS EMPTY!
	}
}
