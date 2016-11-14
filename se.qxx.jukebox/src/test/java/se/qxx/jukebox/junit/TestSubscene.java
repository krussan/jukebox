package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import se.qxx.jukebox.WebRetriever;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;
import se.qxx.jukebox.subtitles.Subscene;

@RunWith(PowerMockRunner.class)
@PrepareForTest(WebRetriever.class)
public class TestSubscene {

	private SubFinderSettings subFinderSettings = null;
	
	@Before
	public void init() throws IOException, JAXBException {
		Settings.initialize();
		initializeSettings();
	}
	
	private void initializeSettings() {
		for (SubFinder sf : Settings.get().getSubFinders().getSubFinder()) {
			if (StringUtils.equalsIgnoreCase(sf.getClazz(), "se.qxx.jukebox.subtitles.Subscene")) {
				subFinderSettings = sf.getSubFinderSettings();
			}
		}
	}
	
	@Test
	public void test() {
		assertNotNull(subFinderSettings);
		
		PowerMockito.mockStatic(WebRetriever.class);

		Subscene ss = new Subscene(subFinderSettings);
		
	}

}
