package se.qxx.jukebox.concurrent;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;

public class JukeboxThreadPoolExecutor extends ThreadPoolExecutor {

	private IJukeboxLogger log;
	
	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

    private static final int THREAD_POOL_SIZE = 50;

    @Inject
	public JukeboxThreadPoolExecutor(LoggerFactory loggerFactory) {
        super(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new JukeboxPriorityQueue());
        this.setLog(loggerFactory.create(LogType.MAIN));
	}
	

    @Override
    public void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        // If submit() method is called instead of execute()
        if (t == null && r instanceof Future<?>) {
            try {
                Object result = ((Future<?>) r).get();
            } catch (CancellationException e) {
                t = e;
            } catch (ExecutionException e) {
                t = e.getCause();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (t != null && !(t instanceof RejectedExecutionException)) {
            // Exception occurred
        	this.getLog().Error("Uncaught exception is detected! " + t
                    + " st: " + Arrays.toString(t.getStackTrace()));

        	// restart if this is a JukeboxThread
        	if (r instanceof JukeboxThread) {
        		this.getLog().Info(
    				String.format("Restarting thread [%s]....", 
    						((JukeboxThread)r).getName()));

        		execute(r);
        	}
        }
        // ... Perform cleanup actions
}


}
