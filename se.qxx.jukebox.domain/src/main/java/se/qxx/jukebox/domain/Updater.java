package se.qxx.jukebox.domain;

import com.google.protobuf.AbstractMessage.Builder;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;

public class Updater<T extends Message> {

	private T _instance;
	boolean updated = false;
	
	public Updater(T instance) {
		this._instance = instance;
	}
	
	public void set(T newInstance) {
		this.updated = true;
		this._instance = newInstance;
	}
	
	public Updater<T> returnInstance(T newInstance) {
		this.set(newInstance);
		return this;
	}
	
	public T get() {
		return this._instance;
	}
}
