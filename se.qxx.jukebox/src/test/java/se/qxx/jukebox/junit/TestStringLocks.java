package se.qxx.jukebox.junit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Test;

import se.qxx.jukebox.concurrent.StringLockPool;

public class TestStringLocks {

	@Test
	public void TestThreadIsBlocking() throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(2);
		StringLockPool pool = new StringLockPool();
		
		FutureTask<Boolean> task1 = new FutureTask<>(() -> {
			try {
				pool.lock("LOCK1");
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				return false;
			}
			finally {
				pool.unlock("LOCK1");
			}
			
			return true;
		});
		
		FutureTask<Boolean> task2 = new FutureTask<>(() -> {
			boolean t = pool.tryLock("lock1");
			return t;
		});
		
		executor.submit(task1);
		executor.submit(task2);
		
		boolean tryLock = task2.get();
		task1.cancel(true);
		
		assertEquals(false, tryLock);
		
	}
	
}
