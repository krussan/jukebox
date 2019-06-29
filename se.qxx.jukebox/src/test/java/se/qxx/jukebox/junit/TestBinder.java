package se.qxx.jukebox.junit;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.core.Main;
import se.qxx.jukebox.core.Starter;

public class TestBinder {

	@Test
	public void testBinder() {
		Injector injector = Binder.setupBindings(new String[] {});
		Starter starter = injector.getInstance(Starter.class);
		Main main = injector.getInstance(Main.class);
		
		assertNotNull(starter);;
		assertNotNull(main);
	}
}
