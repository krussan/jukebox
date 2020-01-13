package se.qxx.jukebox.junit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.concurrent.Executor;
import se.qxx.jukebox.concurrent.JukeboxThreadPoolExecutor;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.JukeboxRpcServerFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMovieIdentifier;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.servercomm.JukeboxRpcServerConnection;
import se.qxx.jukebox.servercomm.TcpListener;

import java.io.IOException;

public class TestTcpListenerInit {

	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule();
	
	@Mock LoggerFactory loggerFactoryMock;
	@Mock ISettings settingsMock;
	@Mock IDatabase dbMock;
	@Mock IStreamingWebServer webServerMock;
	@Mock ISubtitleDownloader subtitleDownloaderMock;
	@Mock IMovieIdentifier movieIdentifierMock;
	@Mock JukeboxRpcServerFactory rpcFactoryMock;
	@Mock IExecutor executorMock;
	@Mock IUtils utilsMock;
	
	
	@Test
	public void TestInitializeTcpListener() throws IOException {
		IJukeboxLogger log = new Log(settingsMock, LogType.NONE);
		JukeboxThreadPoolExecutor executorService = new JukeboxThreadPoolExecutor(loggerFactoryMock);
		when(loggerFactoryMock.create(any(LogType.class))).thenReturn(log);
		
		Executor executor = new Executor(executorService, loggerFactoryMock);
		
		JukeboxRpcServerConnection conn = new JukeboxRpcServerConnection(
				settingsMock, 
				dbMock, 
				subtitleDownloaderMock,
				movieIdentifierMock, 
				loggerFactoryMock,
				executorMock,
				utilsMock,
				webServerMock);
		
		when(rpcFactoryMock.create(any(IStreamingWebServer.class))).thenReturn(conn);
		
		TcpListener listener = new TcpListener(
				executor,
				executorService,
				loggerFactoryMock, 
				rpcFactoryMock,
				webServerMock,
				2152);
		
		listener.initialize();
		//executor.start(listener.getRunnable());
		
		try {
			executor.stop(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
