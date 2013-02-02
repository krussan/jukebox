import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;


public class JukeboxRpcServerConnection extends se.qxx.jukebox.domain.JukeboxDomain.JukeboxService {

	@Override
	public void listMovies(RpcController controller,
			JukeboxRequestListMovies request,
			RpcCallback<JukeboxResponseListMovies> done) {
		
		
	}

}
