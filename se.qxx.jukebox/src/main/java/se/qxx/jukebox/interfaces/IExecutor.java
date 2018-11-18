package se.qxx.jukebox.interfaces;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface IExecutor {

	public void start(Runnable runnable);
	public <T> Future<T> start(Callable<T> callable);
}
