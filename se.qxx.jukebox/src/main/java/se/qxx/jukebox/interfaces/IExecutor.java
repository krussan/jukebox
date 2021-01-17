package se.qxx.jukebox.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public interface IExecutor {

	void start(Runnable runnable);
	<T> Future<T> start(Callable<T> callable);
	void stop(int timeoutSeconds) throws InterruptedException;
	void stopWatchers();
	ExecutorService getExecutorService();
}
