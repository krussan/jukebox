package se.qxx.jukebox.concurrent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IStoppableRunnable;
import se.qxx.jukebox.watcher.FileSystemWatcher;

@Singleton
public class JukeboxExecutor implements IExecutor {

	private List<Object> runnables = new ArrayList<>();
	private ExecutorService executorService;
	private IJukeboxLogger log;

	@Inject
	public JukeboxExecutor(ExecutorService executorService, LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.MAIN));

		this.setExecutorService(executorService);
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
		// kill jukebox threads orderly
		endJukeboxThreads();
		
		ExecutorService pool = this.getExecutorService();
		
		this.getLog().Info("Main threads ended. Waiting for executor shutdown ...");
		pool.shutdown();
		
		try {
		     // Wait a while for existing tasks to terminate
		     if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS)) {
		       pool.shutdownNow(); // Cancel currently executing tasks
		       
		       // Wait a while for tasks to respond to being cancelled
		       if (!pool.awaitTermination(timeoutSeconds, TimeUnit.SECONDS))
		           this.getLog().Error("Pool did not terminate");
		       
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
			}
			else if (o instanceof JukeboxRunnable) {
				((JukeboxRunnable)o).stop();
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

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}
}
