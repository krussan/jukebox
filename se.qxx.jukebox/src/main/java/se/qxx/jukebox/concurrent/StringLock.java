package se.qxx.jukebox.concurrent;

import java.util.concurrent.locks.ReentrantLock;

public class StringLock extends ReentrantLock {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7286552886494455999L;
	private String title;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public StringLock(String title) {
		this.setTitle(title);
	}
}
