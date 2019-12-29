package se.qxx.jukebox.core;

import java.util.Random;

import com.google.inject.Inject;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IRandomWaiter;

public class RandomWaiter implements IRandomWaiter {

	private IJukeboxLogger log;

	@Inject
	public RandomWaiter(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.MAIN));
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	@Override
	public void sleep(int minWaitSeconds, int maxWaitSeconds) {
		try {
			Random r = new Random();
			int n = r.nextInt((maxWaitSeconds - minWaitSeconds) * 1000 + 1) + minWaitSeconds * 1000;
			
			this.getLog().Info(String.format("Sleeping for %s seconds", n));
			// sleep randomly to avoid detection (from 10 sec to 30 sec)
			Thread.sleep(n);
			
		} catch (InterruptedException e) {
			this.getLog().Error("Random waiter interrupted", e);
		}
		
	}

}
