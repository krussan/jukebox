package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.List;

import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.core.Cleaner;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IArguments;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IUtils;
import se.qxx.jukebox.settings.CatalogsTest;
import se.qxx.jukebox.settings.JukeboxListenerSettings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs;
import se.qxx.jukebox.settings.JukeboxListenerSettings.Catalogs.Catalog;
import se.qxx.jukebox.tools.MediaMetadata;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.FileRepresentation;

public class TestCleaner {

	@Mock IExecutor executorMock;
	@Mock IDatabase databaseMock;
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IFilenameChecker filenameCheckerMock;
	@Mock IMediaMetadataHelper mediaMetadataHelperMock;
	@Mock IArguments argumentsMock;
	@Mock ISettings settingsMock;
	@Mock IUtils utilsMock;
	
	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule(); 
	
	private Cleaner cleaner;
		
	@Before
	public void init() throws IOException, JAXBException {
		when(loggerFactoryMock.create(any(Log.LogType.class))).thenReturn(new Log(null, LogType.NONE));
		
		cleaner = new Cleaner(databaseMock, executorMock, argumentsMock, loggerFactoryMock, settingsMock, utilsMock);
	}
	
	public CatalogsTest getCatalog(String path) {
		CatalogsTest c = new CatalogsTest();
		c.setPath(path);
		return c;
	}
	
	@Test
	public void Should_return_true_if_media_path_exist_in_settings_listeners_and_on_disk() {
		when(utilsMock.fileExists("/media/test1")).thenReturn(true);
		
		List<CatalogsTest> list = new ArrayList<>();
		list.add(getCatalog("/media/test1"));
		list.add(getCatalog("/media/fake2"));
		
		Media md = Media.newBuilder()
				.setID(1)
				.setFilename("Filename")
				.setFilepath("/media/test1")
				.setIndex(1)
				.setDownloadComplete(true)
				.build();
		
		boolean actual = cleaner.listenerPathExist(md, list);
		assertEquals(true, actual);
	}
	
	@Test
	public void Should_return_false_if_media_path_dont_exist_in_listeners() {
		List<CatalogsTest> list = new ArrayList<>();
		list.add(getCatalog("/media/fake2"));
		
		Media md = Media.newBuilder()
				.setID(1)
				.setFilename("Filename")
				.setFilepath("/media/test1")
				.setIndex(1)
				.setDownloadComplete(true)
				.build();
		
		boolean actual = cleaner.listenerPathExist(md, list);
		assertEquals(false, actual);
	}

	@Test
	public void Should_return_false_if_listener_path_dont_exist_on_disk() {
		when(utilsMock.fileExists("/media/test1")).thenReturn(false);
		
		List<CatalogsTest> list = new ArrayList<>();
		list.add(getCatalog("/media/test1"));
		list.add(getCatalog("/media/fake2"));
		
		Media md = Media.newBuilder()
				.setID(1)
				.setFilename("Filename")
				.setFilepath("/media/test1")
				.setIndex(1)
				.setDownloadComplete(true)
				.build();
		
		boolean actual = cleaner.listenerPathExist(md, list);
		assertEquals(false, actual);
	}
}