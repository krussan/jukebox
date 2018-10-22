package se.qxx.jukebox;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.tools.Util;

public abstract class JukeboxThread extends Thread {
	private boolean isRunning;
	private long sleepTime;
	private LogType logType;
	
	private ReentrantLock lock = new ReentrantLock();
	private Condition condA = lock.newCondition();
	private Condition condB = lock.newCondition();

	public JukeboxThread(String name, long sleepTime, LogType logType) {
		super(name);
		this.setSleepTime(sleepTime);
		this.setLogType(logType);
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
	
	public LogType getLogType() {
		return logType;
	}

	public void setLogType(LogType logType) {
		this.logType = logType;
	}

	protected abstract void initialize();
	protected abstract void execute() throws InterruptedException;

	@Override
	public void run() {
		this.setRunning(true);
		Util.waitForSettings();
		mainLoop();
	}
	
	protected void mainLoop() {
		Log.Info(String.format("Starting up thread %s",  this.getName()), this.getLogType());
		
		initialize();
		this.setPriority(this.getJukeboxPriority());
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
						condA.await(this.getSleepTime(), TimeUnit.MILLISECONDS);
				}
				finally {
					lock.unlock();
				}
				
				
			} catch (InterruptedException e) {
				Log.Info(String.format("%s thread is shutting down", this.getName()), this.getLogType());
			}
		}
		
		Log.Info(String.format("Thread %s ended normally !",  this.getName()), this.getLogType());
	}

	
	public void end() {
		Log.Info(String.format("Stopping %s thread ...",  this.getName()), this.getLogType());
		this.setRunning(false);
		this.interrupt();
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

}
