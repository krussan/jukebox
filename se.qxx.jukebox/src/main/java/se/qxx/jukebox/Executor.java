package se.qxx.jukebox;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import se.qxx.jukebox.interfaces.IExecutor;

@Singleton
public class Executor implements IExecutor {

	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	@Inject
	public Executor() {
	}
	
	@Override
	public void start(Runnable runnable) {
		this.getExecutorService().submit(runnable);
		
	}

	@Override
	public <T> Future<T> start(Callable<T> callable) {
		return this.getExecutorService().submit(callable);
	}
	
	private ExecutorService getExecutorService() {
		return executorService;
	}
}
