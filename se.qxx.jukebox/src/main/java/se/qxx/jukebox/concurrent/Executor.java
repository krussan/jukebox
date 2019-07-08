package se.qxx.jukebox.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.watcher.FileSystemWatcher;

@Singleton
public class Executor implements IExecutor {

	private List<Object> runnables = new ArrayList<Object>();
	private ExecutorService executorService = null;
	private IJukeboxLogger log;
	private final int THREAD_POOL_SIZE = 15;

	@Inject
	public Executor(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.MAIN));

		executorService = new JukeboxThreadPoolExecutor(
				THREAD_POOL_SIZE, 
				THREAD_POOL_SIZE, 
				0L, 
				TimeUnit.MILLISECONDS, 
				new LinkedBlockingQueue<>(), 
				loggerFactory);
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public List<Object> getRunnables() {
		return runnables;
	}

	@Override
	public void start(Runnable runnable) {
		this.getRunnables().add(runnable);
		this.getExecutorService().execute(runnable);
	}

	@Override
	public <T> Future<T> start(Callable<T> callable) {
		this.getRunnables().add(callable);
		return this.getExecutorService().submit(callable);
	}

	@Override
	public ExecutorService getExecutorService() {
		return executorService;
	}

	@Override
	public void stop(int timeoutSeconds) throws InterruptedException {
		endJukeboxThreads();

		this.getExecutorService().shutdownNow();
		this.getExecutorService().awaitTermination(timeoutSeconds, TimeUnit.SECONDS);
	}

	private void endJukeboxThreads() {
		for (Object o : this.getRunnables()) {
			if (o instanceof JukeboxThread) {
				JukeboxThread t = (JukeboxThread) o;
				this.getLog().Info(String.format("Exeutor is ending thread %s", t.getName()));
				t.end();
			}
		}
	}

	@Override
	public void stopWatchers() {
		for (Object o : this.getRunnables()) {
			if (o instanceof FileSystemWatcher) {
				// end all but the configuration watcher
				if (!StringUtils.equalsIgnoreCase(((JukeboxThread) o).getName(), "ConfigurationWatcher"))
					((JukeboxThread) o).end();
			}
		}
	}

}
