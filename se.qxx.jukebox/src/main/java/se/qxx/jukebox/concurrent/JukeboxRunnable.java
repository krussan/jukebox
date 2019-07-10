package se.qxx.jukebox.concurrent;

import se.qxx.jukebox.interfaces.IStoppableRunnable;

public class JukeboxRunnable implements Runnable {
	
	private Runnable runnable;
	private IStoppableRunnable parent;

	public Runnable getRunnable() {
		return runnable;
	}

	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}


	public IStoppableRunnable getParent() {
		return parent;
	}

	public void setParent(IStoppableRunnable parent) {
		this.parent = parent;
	}

	public JukeboxRunnable(Runnable r, IStoppableRunnable parent) {
		this.setRunnable(r);
		this.setParent(parent);
	}

	@Override
	public void run() {
		if (this.getRunnable() != null)
			this.getRunnable().run();
	}

	public void stop() {
		if (this.getParent() != null)
			this.getParent().stop();
	}
	
}
