package se.qxx.android.jukebox.comm;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.protobuf.BlockingRpcChannel;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;
import com.google.protobuf.RpcController;
import com.google.protobuf.RpcUtil;
import com.googlecode.protobuf.socketrpc.RpcChannels;
import com.googlecode.protobuf.socketrpc.RpcConnectionFactory;
import com.googlecode.protobuf.socketrpc.SocketRpcConnectionFactories;
import com.googlecode.protobuf.socketrpc.SocketRpcController;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.ProgressDialogHandler;
import se.qxx.jukebox.domain.JukeboxDomain.Empty;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestGeneral;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestMovieID;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSeek;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestSetSubtitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseGetTitle;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseIsPlaying;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListMovies;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListPlayers;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseListSubtitles;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseStartMovie;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxResponseTime;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService;
import se.qxx.jukebox.domain.JukeboxDomain.JukeboxService.BlockingInterface;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;

public class JukeboxConnectionHandler {
	
	private JukeboxConnectionHandler  instance;
	
	private BlockingInterface blockingService;
	private JukeboxService service;
	
	public BlockingInterface getBlockingService() {
		return blockingService;
	}
	public void setBlockingService(BlockingInterface service) {
		this.blockingService = service;
	}
	
	public JukeboxService getNonBlockingService() {
		return service;
	}
	public void setNonBlockingService(JukeboxService service) {
		this.service = service;
	}	
	
	protected JukeboxConnectionHandler() {
		setupBlockingService();
		setupNonBlockingService();
	}

	private void setupBlockingService() {
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
		RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories.createRpcConnectionFactory(
    			JukeboxSettings.get().getServerIpAddress(), 
    			JukeboxSettings.get().getServerPort());
    			
		BlockingRpcChannel channel = RpcChannels.newBlockingRpcChannel(connectionFactory);
		
		this.setBlockingService(JukeboxService.newBlockingStub(channel));		
	}

	private void setupNonBlockingService() {
		ExecutorService threadPool = Executors.newFixedThreadPool(1);
		
		RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories.createRpcConnectionFactory(
    			JukeboxSettings.get().getServerIpAddress(), 
    			JukeboxSettings.get().getServerPort());
    			
		RpcChannel channel = RpcChannels.newRpcChannel(connectionFactory, threadPool);
		
		this.setNonBlockingService(JukeboxService.newStub(channel));		
	}

	public static void sendCommandWithProgressDialog(Context context, String message, JukeboxRequestType type, Object... args) {
		ConnectionWrapper.sendCommandWithProgressDialog(context, new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {}
		}, message, type, args);
	}	  
	
	public static void sendCommandWithProgressDialog(Context context, OnDismissListener listener, String message, JukeboxRequestType type, Object... args) {
       	ProgressDialog d = ProgressDialog.show(context, "Jukebox", message);

       	if (listener != null)
       		d.setOnDismissListener(listener);
       	
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(new ProgressDialogHandler(context, d), type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}
	
	public static void sendCommandWithResponseListener(final JukeboxResponseListener listener, String message, JukeboxRequestType type, Object... args) {
       	JukeboxConnectionHandler h = new JukeboxConnectionHandler(listener, type, args);
       	Thread t = new Thread(h);
       	t.start();						
	}	

	public JukeboxConnectionHandler getInstance() {
		if (this.instance == null)
			this.instance = new ConnectionWrapper();
		
		return this.instance;
	}
	
	//----------------------------------------------------------------------------------------------------------------
	//--------------------------------------------------------------------------------------------------- RPC Calls
	//----------------------------------------------------------------------------------------------------------------
	
	public Bundle blacklist(Movie m) {
		RpcController controller = new SocketRpcController();
		JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
		this.getBlockingService().blacklist(controller, request);
		
		return checkResponse(controller);

	}
	
	public Bundle startMovie(String playerName, Movie m) {
		RpcController controller = new SocketRpcController();		
		
		JukeboxRequestStartMovie request = JukeboxRequestStartMovie.newBuilder()
				.setPlayerName(playerName)  // JukeboxSettings.get().getCurrentMediaPlayer()
				.setMovieId(m.getID()) // Model.get().getCurrentMovie().getID()
				.build();		

		JukeboxResponseStartMovie response = this.getBlockingService().startMovie(controller, request);

		if (!controller.failed()) {
			Model.get().clearSubtitles();
			Model.get().addAllSubtitles(response.getSubtitleList());				

			return setResponse(true);
		}
		else {
			return setResponse(false, controller.errorText());
		}
	}
	
	public Bundle stopMovie(String playerName) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getBlockingService().stopMovie(controller, request);
		
		return checkResponse(controller);
	}
	
	public Bundle pauseMovie(String playerName) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getBlockingService().pauseMovie(controller, request);
		
		return checkResponse(controller);		
	}
	
	public Bundle listMovies(String searchString) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestListMovies request = JukeboxRequestListMovies.newBuilder()
				.setSearchString("")
				.build();

		JukeboxResponseListMovies response = this.getBlockingService().listMovies(controller, request);
		
		if (!controller.failed()) {
  			Model.get().clearMovies();
			Model.get().addAllMovies(response.getMoviesList());
			Model.get().setInitialized(true);

			return setResponse(true);
		}
		else {
			return setResponse(false, controller.errorText());
		}		
	}
	
	public Bundle wakeup(String playerName) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getBlockingService().wakeup(controller, request);
		
		return checkResponse(controller);		
	}

	public Bundle toggleFullscreen(String playerName) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();

		this.getBlockingService().toggleFullscreen(controller, request);
		
		return checkResponse(controller);		
	}

	public Bundle isPlaying(String playerName, RpcCallback<JukeboxResponseIsPlaying> callback) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().isPlaying(controller, request, callback);

		return checkResponse(controller);		
	}

	public Bundle getTime(String playerName, RpcCallback<JukeboxResponseTime> callback) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().getTime(controller, request, callback);

		return checkResponse(controller);		
	}

	public Bundle getTitle(String playerName, RpcCallback<JukeboxResponseGetTitle> callback) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().getTitle(controller, request, callback);

		return checkResponse(controller);		
	}

	public Bundle suspend(String playerName) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestGeneral request = JukeboxRequestGeneral.newBuilder()
				.setPlayerName(playerName)
				.build();
		
		this.getNonBlockingService().suspend(controller, request, null);

		return checkResponse(controller);		
	}	
	
	public Bundle listPlayers() {
		RpcController controller = new SocketRpcController();

		JukeboxResponseListPlayers response = this.getBlockingService().listPlayers(controller, Empty.getDefaultInstance());
		
		if (!controller.failed()) {
			Model.get().clearPlayers();
			Model.get().addAllPlayers(response.getHostnameList());
			return setResponse(true);
		}
		else {
			return setResponse(false, controller.errorText());
		}		
	}
	
	public Bundle listSubtitles(Media md) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestListSubtitles request = JukeboxRequestListSubtitles.newBuilder()
				.setMediaId(md.getID())
				.build();

		JukeboxResponseListSubtitles response = this.getBlockingService().listSubtitles(controller, request);

		if (!controller.failed()) {
			Model.get().clearSubtitles();
			Model.get().addAllSubtitles(response.getSubtitleList());
			return setResponse(true);
		}
		else {
			return setResponse(false, controller.errorText());
		}		
		
	}
	
	public Bundle seek(String playerName, int seconds) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestSeek request = JukeboxRequestSeek.newBuilder()
				.setPlayerName(playerName)
				.setSeconds(seconds)
				.build();
		
		this.getBlockingService().seek(controller, request);

		return checkResponse(controller);
	}
	
	public Bundle setSubtitle(String playerName, Media md, String subtitle) {
		RpcController controller = new SocketRpcController();
		
		JukeboxRequestSetSubtitle request = JukeboxRequestSetSubtitle.newBuilder()
				.setPlayerName(JukeboxSettings.get().getCurrentMediaPlayer())
				.setMediaID(Model.get().getCurrentMedia().getID())
				.setSubtitleDescription(Model.get().getCurrentSubtitle())
				.build();		
		
		this.getBlockingService().setSubtitle(controller, request);

		return checkResponse(controller);
	}

	public Bundle toggleWatched(Movie m) {
		RpcController controller = new SocketRpcController();
		JukeboxRequestMovieID request = JukeboxRequestMovieID.newBuilder().setMovieId(m.getID()).build();
		this.getBlockingService().toggleWatched(controller, request);

		return checkResponse(controller);
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

	private Bundle checkResponse(RpcController controller) {
		if (controller.failed())
			return setResponse(false, controller.errorText());
		else
			return setResponse(true);
	}
}
