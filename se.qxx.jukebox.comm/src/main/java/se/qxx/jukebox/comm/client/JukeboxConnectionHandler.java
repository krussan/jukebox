package se.qxx.jukebox.comm.client;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.socketrpc.SocketRpcController;

import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestID;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.RequestType;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class JukeboxConnectionHandler {
	
	private JukeboxResponseListener listener;
	
	public JukeboxConnectionHandler(String serverIPaddress, int port) {
		JukeboxConnectionPool.setup(serverIPaddress, port);
	}
	
	public JukeboxConnectionHandler(String serverIPaddress, int port, JukeboxResponseListener listener) {
		JukeboxConnectionPool.setup(serverIPaddress, port);		
		this.setListener(listener);
	}
	
	//----------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------- RPC Calls
	//----------------------------------------------------------------------------------------------------------------

	public void getItem(final int id, final RequestType requestType,final RpcCallback<JukeboxDomain.JukeboxResponseGetItem> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
			JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();
			JukeboxDomain.JukeboxRequestGetItem request = JukeboxDomain.JukeboxRequestGetItem.newBuilder()
					.setRequestType(requestType)
					.setID(id)
					.setReturnFullSizePictures(false)
					.build();

			service.getItem(controller, request, response -> {
				if (callback != null)
					callback.run(response);

			});
		});
		t.start();
	}

	public void blacklist(final int id, final RequestType requestType) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestID request = JukeboxRequestID.newBuilder()
                    .setId(id)
                    .setRequestType(requestType)
                    .build();

            service.blacklist(controller, request, arg0 -> onRequestComplete(controller));

        });
		t.start();
		
		
	}
	
	public void reIdentify(final int id, final RequestType requestType) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestID request = JukeboxRequestID.newBuilder()
                    .setId(id)
                    .setRequestType(requestType)
                    .build();

            service.reIdentify(controller, request, arg0 -> onRequestComplete(controller));

        });
		t.start();
		
		
	}
	
	public void startMovie(
			final String playerName, 
			final Movie m,
			final Episode ep,
			final RpcCallback<JukeboxResponseStartMovie> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
			if (m != null || ep != null) {
				JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();
				int id = m == null ? ep.getID() : m.getID();
				RequestType requestType = m == null ? RequestType.TypeEpisode : RequestType.TypeMovie;

				JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
						.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
						.setMovieOrEpisodeId(id) // Model.get().getCurrentMovie().getID()
						.setRequestType(requestType)
						.build();

				service.startMovie(controller, request, response -> {
					onRequestComplete(controller);
					if (callback != null)
						callback.run(response);

				});
			}
        });
		t.start();
		
		

	}
	
	public void stopMovie(final String playerName, final RpcCallback<Empty> callback) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.stopMovie(controller, request, arg0 -> {
onRequestComplete(controller);

if (callback != null)
callback.run(arg0);
});
        });
		t.start();
		
		
		
	}
	
	public void pauseMovie(final String playerName) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.pauseMovie(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();
		
		
		
	}
	
	public void listMovies(String searchString, int nrOfItems, int offset, final RpcCallback<JukeboxResponseListMovies> callback) {
		list(searchString, RequestType.TypeMovie, nrOfItems, offset, callback); 
	}
	
	public void listSeries(String searchString, int nrOfItems, int offset, final RpcCallback<JukeboxResponseListMovies> callback) {
		list(searchString, RequestType.TypeSeries, nrOfItems, offset, callback);
	}

	private void list(final String searchString, final RequestType type, final int nrOfItems, final int offset, final RpcCallback<JukeboxResponseListMovies> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
                    .setSearchString(searchString)
                    .setRequestType(type)
                    .setNrOfItems(nrOfItems)
                    .setStartIndex(offset)
                    .setReturnFullSizePictures(false)
                    .build();

            try {
                service.listMovies(controller, request, response -> {
onRequestComplete(controller);
if (callback != null)
callback.run(response);
});
            }
            catch (Exception e) {
                onRequestComplete(controller);
            }

        });
		t.start();
		
	}

	
	public void wakeup(final String playerName) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.wakeup(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();
		
		
	}

	public void toggleFullscreen(final String playerName) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.toggleFullscreen(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();
		
		
	}

	public void isPlaying(final String playerName, final RpcCallback<JukeboxResponseIsPlaying> callback) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.isPlaying(controller, request, response -> {
onRequestComplete(controller);

if (callback != null)
callback.run(response);
});
        });
		t.start();
		
		
	}

	public void getTime(final String playerName, final RpcCallback<JukeboxResponseTime> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.getTime(controller, request, response -> {
onRequestComplete(controller);

if (callback != null)
callback.run(response);
});
        });
		t.start();		

	}

	public void getTitle(final String playerName, final RpcCallback<JukeboxResponseGetTitle> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.getTitle(controller, request, response -> {
onRequestComplete(controller);

if (callback != null)
callback.run(response);
});
        });
		t.start();
		


	}

	public void suspend(final String playerName) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
                    .setPlayerName(playerName)
                    .build();

            service.suspend(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();

	}	
	
	public void listPlayers(final RpcCallback<JukeboxResponseListPlayers> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            service.listPlayers(controller, Empty.getDefaultInstance(), response -> {
                onRequestComplete(controller);

                if (callback != null)
                    callback.run(response);

            });
        });
		t.start();

		
		
	}
	
	public void listSubtitles(final Media md, final RpcCallback<JukeboxResponseListSubtitles> callback) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
                    .setMediaId(md.getID())
                    .build();

            service.listSubtitles(controller, request, response -> {
onRequestComplete(controller);

if (callback != null)
callback.run(response);
});
        });
		t.start();
		

	}
	
	public void seek(final String playerName,final int seconds) {
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
                    .setPlayerName(playerName)
                    .setSeconds(seconds)
                    .build();

            service.seek(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();

	}
	
	public void setSubtitle(final String playerName, final Media md, final Subtitle subtitle) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
                    .setPlayerName(playerName)
                    .setMediaID(md.getID())
                    .setSubtitleDescription(subtitle.getDescription())
                    .build();

            service.setSubtitle(controller, request, response -> onRequestComplete(controller));
        });
		t.start();


	}

	public void toggleWatched(final int id, final RequestType requestType) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread(() -> {
            JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();

            JukeboxRequestID request = JukeboxRequestID.newBuilder()
                    .setId(id)
                    .setRequestType(requestType)
                    .build();

            service.toggleWatched(controller, request, arg0 -> onRequestComplete(controller));
        });
		t.start();
		

	}
	
	public void reenlistSub(final int id, final RequestType requestType) {
		final RpcController controller = new SocketRpcController();

		Thread t = new Thread() {
			public void run() {
				JukeboxService service = JukeboxConnectionPool.get().getNonBlockingService();
				
				JukeboxRequestID request = JukeboxRequestID.newBuilder()
						.setId(id)
						.setRequestType(requestType)
						.build();
				
				service.reenlistSubtitles(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
				
			}
		};
		t.start();
		
		
	}


	private JukeboxResponseListener getListener() {
		return listener;
	}

	public void setListener(JukeboxResponseListener listener) {
		this.listener = listener;
	}	
		
	private JukeboxConnectionMessage checkResponse(RpcController controller) {
		return new JukeboxConnectionMessage(!controller.failed(), controller.errorText());		
	}
	
	private void onRequestComplete(RpcController controller) {
		JukeboxConnectionMessage msg = checkResponse(controller);
		
		if (this.getListener() != null)
			this.listener.onRequestComplete(msg);		
	}


}
