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

	private final int THREAD_POOL_SIZE = 50;

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
		this.getExecutorService().shutdown();
		
		endJukeboxThreads();

		shutdown(timeoutSeconds);
	}
	
	private void shutdown(int timeoutSeconds) {
		ExecutorService pool = this.getExecutorService();
		try {
		     // Wait a while for existing tasks to terminate
		     if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
		       pool.shutdownNow(); // Cancel currently executing tasks
		       
		       // Wait a while for tasks to respond to being cancelled
		       if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS))
		           System.err.println("Pool did not terminate");
		     }
	   } catch (InterruptedException ie) {
	     // (Re-)Cancel if current thread also interrupted
	     pool.shutdownNow();
	     // Preserve interrupt status
	     Thread.currentThread().interrupt();
	   }

	}

	private void endJukeboxThreads() {
		for (Object o : this.getRunnables()) {
			if (o instanceof JukeboxThread) {
				JukeboxThread t = (JukeboxThread) o;
				this.getLog().Info(String.format("Exeutor is ending thread %s", t.getName()));
				t.end();
				
				this.getLog().Info(String.format("Nr of threads left :: %s", Thread.activeCount()));
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
