package se.qxx.jukebox.junit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.googlecode.protobuf.socketrpc.RpcConnectionFactory;

import se.qxx.jukebox.builders.MovieBuilderFactory;
import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.concurrent.Executor;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.JukeboxRpcServerFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDistributor;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.servercomm.JukeboxRpcServerConnection;
import se.qxx.jukebox.servercomm.TcpListener;
import se.qxx.jukebox.settings.Settings;

public class TestTcpListenerInit {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock ISettings settingsMock;
	@Mock IDatabase dbMock;
	@Mock IDistributor distributorMock;
	@Mock IStreamingWebServer webServerMock;
	@Mock ISubtitleDownloader subtitleDownloaderMock;
	@Mock IMovieIdentifier movieIdentifierMock;
	@Mock JukeboxRpcServerFactory rpcFactoryMock;
	
	
	@Test
	public void TestInitializeTcpListener() {
		Executor executor = new Executor();
		IJukeboxLogger log = new Log(settingsMock, LogType.NONE);
		JukeboxRpcServerConnection conn = new JukeboxRpcServerConnection(
				settingsMock, 
				dbMock, 
				distributorMock, 
				subtitleDownloaderMock, 
				movieIdentifierMock, 
				loggerFactoryMock, 
				webServerMock);
		
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		when(rpcFactoryMock.create(any(IStreamingWebServer.class))).thenReturn(conn);
		
		TcpListener listener = new TcpListener(
				executor,  
				loggerFactoryMock, 
				rpcFactoryMock,
				webServerMock,
				2152);
		
		listener.initialize();
		executor.start(listener.getRunnable());
		
		try {
			executor.stop(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
