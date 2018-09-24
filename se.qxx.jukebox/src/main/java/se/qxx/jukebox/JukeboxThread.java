package se.qxx.jukebox;

import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.tools.Util;

public abstract class JukeboxThread extends Thread {
	private boolean isRunning;
	private String threadName;
	private long sleepTime;
	private LogType logType;

	public JukeboxThread(String name, long sleepTime, LogType logType) {
		this.setName(name);
		this.setSleepTime(sleepTime);
		this.setLogType(logType);
	}
	

	public boolean isRunning() {
		return isRunning;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}
	
	
	public String getThreadName() {
		return threadName;
	}


	public void setThreadName(String name) {
		this.threadName = name;
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
	protected abstract void execute();

	@Override
	public void run() {
		this.setRunning(true);
		Util.waitForSettings();
		mainLoop();
	}
	
	protected void mainLoop() {
		initialize();
		while(this.isRunning()) {
			try {
				execute();
				
				synchronized(this) {
					if (this.getSleepTime() > 0)
						this.wait(this.getSleepTime());
					else if (this.getSleepTime() == 0)
						this.wait();
				}
				
				
			} catch (InterruptedException e) {
				Log.Info(String.format("%s thread is shutting down", this.getThreadName()), this.getLogType());
			}
		}
	}

	
	public void end() {
		Log.Info(String.format("Stopping %s thread ...",  this.getThreadName()), this.getLogType());
		this.setRunning(false);
		this.interrupt();
	}




	

}
