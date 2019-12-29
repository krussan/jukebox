package se.qxx.jukebox.servercomm;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import se.qxx.jukebox.concurrent.JukeboxThread;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxServiceGrpc;
import se.qxx.jukebox.factories.JukeboxRpcServerFactory;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ITcpListener;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public class TcpListener extends JukeboxThread implements ITcpListener {

	private Server server;
	private JukeboxRpcServerConnection serverConnection;
	private IJukeboxLogger log;
	private IExecutor executor;
	private JukeboxRpcServerFactory rpcFactory;
	private int port;
	private ExecutorService executorService;

	@Inject
	public TcpListener(
			IExecutor executor,
			@Assisted("executorservice") ExecutorService executorService,
			LoggerFactory loggerFactory,
			JukeboxRpcServerFactory rpcFactory,
			@Assisted("webserver") IStreamingWebServer webServer,
			@Assisted("port") int port) {

		super("TcpListener", 0, loggerFactory.create(LogType.FIND), executor);

		this.setExecutorService(executorService);
		this.setPort(port);
		this.setServerConnection((JukeboxRpcServerConnection)rpcFactory.create(webServer));
		this.setRpcFactory(rpcFactory);
		this.setExecutor(executor);
		this.setLog(loggerFactory.create(LogType.COMM));

	}
	
	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public JukeboxRpcServerFactory getRpcFactory() {
		return rpcFactory;
	}

	public void setRpcFactory(JukeboxRpcServerFactory rpcFactory) {
		this.rpcFactory = rpcFactory;
	}

	public IExecutor getExecutor() {
		return executor;
	}

	public void setExecutor(IExecutor executor) {
		this.executor = executor;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public JukeboxRpcServerConnection getServerConnection() {
		return serverConnection;
	}

	public void setServerConnection(JukeboxRpcServerConnection serverConnection) {
		this.serverConnection = serverConnection;
	}

	public JukeboxServiceGrpc.JukeboxServiceImplBase getService() {
		return this.getServerConnection();
	}

	@Override
	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public void initialize() {
		this.getLog().Info(String.format("Starting up RPC server. Listening on port %s",  this.getPort()));

		Server server = ServerBuilder.forPort(this.getPort())
				.executor(this.getExecutorService())
				.addService(this.getServerConnection())
				.build();

		this.setServer(server);

		try {
			server.start();
		}
		catch (IOException ex) {
			this.getLog().Error("Error when setting up tcp listener", ex);
		}
	}

	@Override
	public Runnable getRunnable() {
		return this;
	}

	@Override
	protected void execute() throws InterruptedException {
		try {
			Thread.currentThread().sleep(1000);
		}
		catch (InterruptedException ex) {
		}
	}

	@Override
	public void stop() {
		try {
			if (this.getServer() != null) {
					this.getServer().shutdown();
					this.getServer().awaitTermination();
			}
		} catch (InterruptedException e) {
			this.getLog().Error("Error when shutting down", e);
		}
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
}
