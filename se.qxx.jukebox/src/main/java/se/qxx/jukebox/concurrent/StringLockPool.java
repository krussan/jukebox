package se.qxx.jukebox.concurrent;

import java.util.LinkedList;
import org.apache.commons.lang3.StringUtils;

public class StringLockPool {

	private LinkedList<StringLock> pool; 
	public StringLockPool() {
		this.setPool(new LinkedList<StringLock>());
	}
	
	public void lock(String lockTitle) {
		StringLock lock = findLock(lockTitle);
		if (lock == null)
			lock = addLock(lockTitle);
		
		lock.lock();
	}

	private StringLock addLock(String lockTitle) {
		StringLock lock = new StringLock(lockTitle.trim());
		this.getPool().add(lock);
		return lock;
	}
	
	public void unlock(String lockTitle) {
		StringLock lock = findLock(lockTitle);
		if (lock != null) {
			lock.unlock();
		}
	}
	
	public boolean tryLock(String lockTitle) {
		StringLock lock = findLock(lockTitle);
		if (lock != null) {
			return lock.tryLock();
		}
		
		return false;
	}
	
	public StringLock findLock(String lockTitle) {
		String trimmed = lockTitle.trim();
		for (StringLock s : this.getPool()) {
			if (StringUtils.equalsIgnoreCase(s.getTitle(), trimmed))
				return s;
		}
		
		return null;
	}

	public LinkedList<StringLock> getPool() {
		return pool;
	}

	private void setPool(LinkedList<StringLock> pool) {
		this.pool = pool;
	}
	
	
}
