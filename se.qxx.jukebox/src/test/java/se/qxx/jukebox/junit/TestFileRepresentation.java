package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;
import org.modelmapper.internal.util.Assert;

import se.qxx.jukebox.watcher.FileRepresentation;

public class TestFileRepresentation {

	@Test
	public void TestQueueContains() {
		ConcurrentLinkedQueue<FileRepresentation> queue = new ConcurrentLinkedQueue<>();
		FileRepresentation f1 = new FileRepresentation("/aa/bb/cc", "dd", 123, 123);
		FileRepresentation f2 = new FileRepresentation("/aa/bb/cc", "dd", 444, 555);
		
		queue.add(f1);
		
		Assert.isTrue(queue.contains(f2));
		
	}
	
	@Test
	public void TestEquals() {
		ConcurrentLinkedQueue<FileRepresentation> queue = new ConcurrentLinkedQueue<>();
		FileRepresentation f1 = new FileRepresentation("/aa/bb/cc", "dd", 123, 123);
		FileRepresentation f2 = new FileRepresentation("/aa/bb/cc", "dd", 444, 555);
		
		Assert.isTrue(f1.equals(f2));
		
	}
}
