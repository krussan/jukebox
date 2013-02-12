package se.qxx.android.jukebox.comm;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;
import com.googlecode.protobuf.socketrpc.SocketRpcController;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.model.Model;
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
import android.os.Bundle;
import android.os.Message;

public class JukeboxConnectionHandler {
	
	private JukeboxResponseListener listener;
	
	public JukeboxConnectionHandler() {
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
	
	public void blacklist(Movie m) {
		final RpcController controller = new SocketRpcController();
		JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
		this.getNonBlockingService().blacklist(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});
		
	}
	
	public void startMovie(String playerName, Movie m, final RpcCallback<JukeboxResponseStartMovie> callback) {
		final RpcController controller = new SocketRpcController();		
		
		JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
				.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
				.setMovieId(m.getID()) // Model.get().getCurrentMovie().getID()
				.build();		

		this.getNonBlockingService().startMovie(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseStartMovie>() {
			@Override
			public void run(JukeboxDomain.JukeboxResponseStartMovie response) {
				Model.get().clearSubtitles();
				Model.get().addAllSubtitles(response.getSubtitleList());				
				onRequestComplete(controller);
				if (callback != null) 
					callback.run(response);
			}
		});

	}
	
	public void stopMovie(String playerName, final RpcCallback<Empty> callback) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getNonBlockingService().stopMovie(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
				
				if (callback != null) 				
					callback.run(arg0);
			}
		});
		
	}
	
	public void pauseMovie(String playerName) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getNonBlockingService().pauseMovie(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});
		
	}
	
	public void listMovies(String searchString) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
				.setSearchString("")
				.build();

		this.getNonBlockingService().listMovies(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseListMovies>() {
			@Override
			public void run(JukeboxResponseListMovies response) {
	  			Model.get().clearMovies();
				Model.get().addAllMovies(response.getMoviesList());
				Model.get().setInitialized(true);
				onRequestComplete(controller);
			}
		});
		
	}
	
	public void wakeup(String playerName) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getNonBlockingService().wakeup(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});
		
	}

	public void toggleFullscreen(String playerName) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getNonBlockingService().toggleFullscreen(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});
		
	}

	public void isPlaying(String playerName, final RpcCallback<JukeboxResponseIsPlaying> callback) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().isPlaying(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseIsPlaying>() {
			@Override
			public void run(JukeboxResponseIsPlaying response) {
				onRequestComplete(controller);
				
				if (callback != null) 				
					callback.run(response);
			}
		});
		
	}

	public void getTime(String playerName, final RpcCallback<JukeboxResponseTime> callback) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().getTime(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseTime>() {
			@Override
			public void run(JukeboxResponseTime response) {
				onRequestComplete(controller);
				
				if (callback != null) 
					callback.run(response);
			}
		});

	}

	public void getTitle(String playerName, final RpcCallback<JukeboxResponseGetTitle> callback) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().getTitle(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseGetTitle>() {
			@Override
			public void run(JukeboxResponseGetTitle response) {
				onRequestComplete(controller);
				
				if (callback != null) 				
					callback.run(response);
			}
		});

	}

	public void suspend(String playerName) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().suspend(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});

	}	
	
	public void listPlayers() {
		final RpcController controller = new SocketRpcController();

		this.getNonBlockingService().listPlayers(controller, Empty.getDefaultInstance(), new RpcCallback<JukeboxDomain.JukeboxResponseListPlayers>() {			
			@Override
			public void run(JukeboxResponseListPlayers response) {
				Model.get().clearPlayers();
				Model.get().addAllPlayers(response.getHostnameList());				
				onRequestComplete(controller);
			}
		});
		
		
	}
	
	public void listSubtitles(Media md) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(md.getID())
				.build();

		this.getNonBlockingService().listSubtitles(controller, request, new RpcCallback<JukeboxDomain.JukeboxResponseListSubtitles>() {	
			@Override
			public void run(JukeboxResponseListSubtitles response) {
				Model.get().clearSubtitles();
				Model.get().addAllSubtitles(response.getSubtitleList());				
				onRequestComplete(controller);
			}
		});

	}
	
	public void seek(String playerName, int seconds) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
				.setPlayerName(playerName)
				.setSeconds(seconds)
				.build();
		
		this.getNonBlockingService().seek(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});

	}
	
	public void setSubtitle(String playerName, Media md, Subtitle subtitle) {
		final RpcController controller = new SocketRpcController();
		
		JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.setMediaID(md.getID())
				.setSubtitleDescription(subtitle.getDescription())
				.build();		
		
		this.getNonBlockingService().setSubtitle(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty response) { 
				onRequestComplete(controller);
			}
		});

	}

	public void toggleWatched(Movie m) {
		final RpcController controller = new SocketRpcController();
		JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
		this.getNonBlockingService().toggleWatched(controller, request, new RpcCallback<JukeboxDomain.Empty>() {
			@Override
			public void run(Empty arg0) {
				onRequestComplete(controller);
			}
		});

	}

	private Bundle setResponse(Boolean success) {
		Bundle b = new Bundle();
		b.putBoolean("success", success);
		
		return b;
	}
	
	private Bundle setResponse(Boolean success, String message) {
		Bundle b = setResponse(success);
		b.putString("message", message);
		
		return b;
	}

	public JukeboxResponseListener getListener() {
		return listener;
	}

	public void setListener(JukeboxResponseListener listener) {
		this.listener = listener;
	}	
		
	private Bundle checkResponse(RpcController controller) {
		return setResponse(!controller.failed(), controller.errorText());		
	}
	
	private void onRequestComplete(RpcController controller) {
		Message msg = new Message();
		msg.setData(checkResponse(controller));
		
		if (this.getListener() != null)
			this.listener.onRequestComplete(msg);		
	}


}
