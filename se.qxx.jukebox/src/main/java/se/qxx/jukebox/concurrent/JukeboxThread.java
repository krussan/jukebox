package se.qxx.jukebox.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IJukeboxLogger;

public abstract class JukeboxThread implements Runnable {
	private boolean isRunning;
	private long sleepTime;
	private IJukeboxLogger log;
	private String name;
	private IExecutor executor;
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition condA = lock.newCondition();
	
	@SuppressWarnings("unused")
	private Condition condB = lock.newCondition();

	public JukeboxThread(
			String name, 
			long sleepTime, 
			IJukeboxLogger log,
			IExecutor executor) {
		this.setName(name);
		this.setSleepTime(sleepTime);
		this.setLog(log);
		this.setExecutor(executor);
	}
	

	public IJukeboxLogger getLog() {
		return log;
	}


	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}


	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	
	public long getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(long sleepTime) {
		this.sleepTime = sleepTime;
	}
	

	protected abstract void initialize();
	protected abstract void execute() throws InterruptedException;

	@Override
	public void run() {
		this.setRunning(true);
		
		mainLoop();
	}
	
	protected void mainLoop() {
		this.getLog().Info(String.format("Starting up thread %s",  this.getName()));
		
		initialize();
		while(this.isRunning()) {
			try {
				execute();
				
				if (!this.isRunning)
					break;
				
				lock.lock();
				try {
					if (this.getSleepTime() > 0)
						condA.await(this.getSleepTime(), TimeUnit.MILLISECONDS);
					else if (this.getSleepTime() == 0)
						condA.await();
				}
				finally {
					lock.unlock();
				}
				
				
			} catch (InterruptedException e) {
				this.getLog().Info(String.format("%s thread is shutting down", this.getName()));
			}
		}
		
		this.getLog().Info(String.format("Thread %s ended normally !",  this.getName()));
	}

	
	public void end() {
		this.getLog().Info(String.format("Stopping %s thread ...",  this.getName()));
		this.stop();
		this.setRunning(false);

		// in case we are in the wait loop we need to signal the thread to release the lock
		signal();
	}

	public int getJukeboxPriority() {
		return Thread.NORM_PRIORITY;
	}
	
	protected void signal() {
		lock.lock();
		try {
			condA.signal();
		}
		finally {
			lock.unlock();
		}
	}

	public void stop() {
		// Override in sub class
	}

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public IExecutor getExecutor() {
		return executor;
	}


	public void setExecutor(IExecutor executor) {
		this.executor = executor;
	}

}
