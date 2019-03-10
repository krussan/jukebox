package se.qxx.jukebox.junit;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.slf4j.ILoggerFactory;

import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IDownloadChecker;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.watcher.DownloadChecker;
import se.qxx.jukebox.watcher.FileRepresentation;

public class TestDownloadChecker {

	@Mock IExecutor executorMock;
	@Mock IDatabase databaseMock;
	@Mock LoggerFactory loggerFactoryMock;
	@Mock IFilenameChecker filenameCheckerMock;
	@Mock IMediaMetadataHelper mediaMetadataHelperMock;
	
	@Rule public MockitoRule mockitoRule = MockitoJUnit.rule(); 
	
	private DownloadChecker downloadChecker;
	
	@Before
	public void init() throws IOException, JAXBException {
		when(loggerFactoryMock.create(any(Log.LogType.class))).thenReturn(new Log(null, LogType.NONE));
		
		downloadChecker = new DownloadChecker(executorMock, databaseMock, loggerFactoryMock, filenameCheckerMock, mediaMetadataHelperMock);
		downloadChecker.setRunning(true);
	}
	
	@Test
	public void Should_store_new_file_in_map_with_WAIT() {
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);
		
		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.WAIT, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	@Test
	public void Should_store_new_file_in_map_with_INIT() {		
		when(databaseMock.getMediaByFilename(any(String.class))).thenReturn(
				Media.newBuilder()
				.setID(1)
				.setFilename("testfile")
				.setFilepath("/etc/test")
				.setIndex(1)
				.setDownloadComplete(false)
				.build());
		
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);

		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.INIT, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	@Test
	public void Should_set_file_to_changed() {
		when(databaseMock.getMediaByFilename(any(String.class))).thenReturn(
				Media.newBuilder()
				.setID(1)
				.setFilename("testfile")
				.setFilepath("/etc/test")
				.setIndex(1)
				.setDownloadComplete(false)
				.build());
		
		FileRepresentation f1 = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f1);
		
		FileRepresentation f2 = new FileRepresentation("/etc/test", "testfile", 2000, 2000);
		downloadChecker.checkFile(f2);
		
		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.CHANGED, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	public void Should_reset_file_to_init_after_file_changed() {
//		// register file
//		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
//		downloadChecker.checkFile(f);
//		
//		// executer runs and checks all files
//		// state is same as before
//		downloadChecker.checkCachedFiles();
//		
		
	}

	public void Should_reset_file_to_init_when_file_changed_after_completed() {
	}
}