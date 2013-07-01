package se.qxx.jukebox.comm.client;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.socketrpc.SocketRpcController;

import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestMovieID;
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
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class JukeboxConnectionHandler {
	
	private JukeboxResponseListener listener;
	
	public JukeboxConnectionHandler(String serverIPaddress, int port) {
		JukeboxConnectionPool.setup(serverIPaddress, port);
	}
	
	public JukeboxConnectionHandler(JukeboxResponseListener listener) {
		this.setListener(listener);
	}

	private JukeboxService getNonBlockingService() {
		return JukeboxConnectionPool.get().getNonBlockingService();
	}

//	public static void sendCommandWithProgressDialog(Context context, String message, JukeboxRequestType type, Object... args) {
//		ConnectionWrapper.sendCommandWithProgressDialog(context, new OnDismissListener() {
//			@Override
//			public void onDismiss(DialogInterface dialog) {}
//		}, message, type, args);
//	}	  
//	
//	public static void sendCommandWithProgressDialog(Context context, OnDismissListener listener, String message, JukeboxRequestType type, Object... args) {
//       	ProgressDialog d = ProgressDialog.show(context, "Jukebox", message);
//
//       	if (listener != null)
//       		d.setOnDismissListener(listener);
//       	
//       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(context, d), type, args);
//       	Thread t = new Thread(h);
//       	t.start();						
//	}
//	
//	public static void sendCommandWithResponseListener(final JukeboxResponseListener listener, String message, JukeboxRequestType type, Object... args) {
//       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(listener, type, args);
//       	Thread t = new Thread(h);
//       	t.start();						
//	}	

//	public JukeboxConnectionHandler getInstance() {
//		if (this.instance == null)
//			this.instance = new ConnectionWrapper();
//		
//		return this.instance;
//	}
	
	//----------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------- RPC Calls
	//----------------------------------------------------------------------------------------------------------------
	
	public void blacklist(final Movie m) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();

		Thread t = new Thread() {
			public void run() {
				JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
				service.blacklist(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
				
			}
		};
		t.start();
		
		
	}
	
	public void startMovie(final String playerName, final Movie m, final RpcCallback<JukeboxResponseStartMovie> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();

		Thread t = new Thread() {
			public void run() {
				JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
						.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
						.setMovieId(m.getID()) // Model.get().getCurrentMovie().getID()
						.build();		

				service.startMovie(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseStartMovie>() {
					@Override
					public void run(JukeboxDomain.JukeboxResponseStartMovie response) {			
						onRequestComplete(controller);
						if (callback != null) 
							callback.run(response);
					}
				});				
			}
		};
		t.start();
		
		

	}
	
	public void stopMovie(final String playerName, final RpcCallback<Empty> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();

				service.stopMovie(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
						
						if (callback != null) 				
							callback.run(arg0);
					}
				});				
			}
		};
		t.start();
		
		
		
	}
	
	public void pauseMovie(final String playerName) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();

				service.pauseMovie(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
			}
		};
		t.start();
		
		
		
	}
	
	public void listMovies(String searchString, final RpcCallback<JukeboxResponseListMovies> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
		
				JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
						.setSearchString("")
						.build();
		
				try {
					service.listMovies(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseListMovies>() {
						@Override
						public void run(JukeboxResponseListMovies response) {
							onRequestComplete(controller);
							if (callback != null) 				
								callback.run(response);							
						}
					});
				}
				catch (Exception e) {
					onRequestComplete(controller);
				}
				
			}
		};
		t.start();
		
	}

	
	public void wakeup(final String playerName) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();

				service.wakeup(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});				
			}
		};
		t.start();
		
		
	}

	public void toggleFullscreen(final String playerName) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();

				service.toggleFullscreen(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
			}
		};
		t.start();
		
		
	}

	public void isPlaying(final String playerName, final RpcCallback<JukeboxResponseIsPlaying> callback) {
		final JukeboxService service = this.getNonBlockingService();
		final RpcController controller = new SocketRpcController();
		
		Thread t = new Thread() {
			public void run() {
				
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();
				
				service.isPlaying(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseIsPlaying>() {
					@Override
					public void run(JukeboxResponseIsPlaying response) {
						onRequestComplete(controller);
						
						if (callback != null) 				
							callback.run(response);
					}
				});				
			}
		};
		t.start();
		
		
	}

	public void getTime(final String playerName, final RpcCallback<JukeboxResponseTime> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();
				
				service.getTime(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseTime>() {
					@Override
					public void run(JukeboxResponseTime response) {
						onRequestComplete(controller);
						
						if (callback != null) 
							callback.run(response);
					}
				});
			}
		};
		t.start();		

	}

	public void getTitle(final String playerName, final RpcCallback<JukeboxResponseGetTitle> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();
				
				service.getTitle(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseGetTitle>() {
					@Override
					public void run(JukeboxResponseGetTitle response) {
						onRequestComplete(controller);
						
						if (callback != null) 				
							callback.run(response);
					}
				});				
			}
		};
		t.start();
		


	}

	public void suspend(final String playerName) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
						.setPlayerName(playerName)
						.build();
				
				service.suspend(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});				
			}
		};
		t.start();

	}	
	
	public void listPlayers(final RpcCallback<JukeboxResponseListPlayers> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				service.listPlayers(controller, Empty.getDefaultInstance(), new RpcCallback<JukeboxDomain.JukeboxResponseListPlayers>() {			
					@Override
					public void run(JukeboxResponseListPlayers response) {
						onRequestComplete(controller);

						if (callback != null) 				
							callback.run(response);

					}
				});
			}
		};
		t.start();

		
		
	}
	
	public void listSubtitles(final Media md, final RpcCallback<JukeboxResponseListSubtitles> callback) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
						.setMediaId(md.getID())
						.build();

				service.listSubtitles(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseListSubtitles>() {	
					@Override
					public void run(JukeboxResponseListSubtitles response) {
						onRequestComplete(controller);
						
						if (callback != null) 				
							callback.run(response);						
					}
				});				
			}
		};
		t.start();
		

	}
	
	public void seek(final String playerName,final int seconds) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
						.setPlayerName(playerName)
						.setSeconds(seconds)
						.build();
				
				service.seek(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
			}
		};
		t.start();

	}
	
	public void setSubtitle(final String playerName, final Media md, final Subtitle subtitle) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();
		
		Thread t = new Thread() {
			public void run() {
				JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
						.setPlayerName(playerName)
						.setMediaID(md.getID())
						.setSubtitleDescription(subtitle.getDescription())
						.build();		
				
				service.setSubtitle(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty response) { 
						onRequestComplete(controller);
					}
				});
			}
		};
		t.start();


	}

	public void toggleWatched(final Movie m) {
		final RpcController controller = new SocketRpcController();
		final JukeboxService service = this.getNonBlockingService();

		Thread t = new Thread() {
			public void run() {
				JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
				service.toggleWatched(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
					@Override
					public void run(Empty arg0) {
						onRequestComplete(controller);
					}
				});
			}
		};
		t.start();
		

	}

	public JukeboxResponseListener getListener() {
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
