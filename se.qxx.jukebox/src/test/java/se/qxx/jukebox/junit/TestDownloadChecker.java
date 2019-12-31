package se.qxx.jukebox.junit;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import se.qxx.jukebox.converter.FileChangedState;
import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IExecutor;
import se.qxx.jukebox.interfaces.IFilenameChecker;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;
import se.qxx.jukebox.tools.MediaMetadata;
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
		registerDatabaseMock(false);
		
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);

		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.INIT, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	@Test
	public void Should_set_file_to_changed() {
		registerDatabaseMock(false);
		
		FileRepresentation f1 = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f1);
		
		FileRepresentation f2 = new FileRepresentation("/etc/test", "testfile", 2000, 2000);
		downloadChecker.checkFile(f2);
		
		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.CHANGED, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	@Test
	public void Should_reset_file_to_init_after_file_changed() {
		registerDatabaseMock(false);
		registerMediaHelperMock();
		
		// register file
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);
		
		// executer runs and checks all files
		// state is same as before
		downloadChecker.checkCachedFiles();
		
		// file has changed after "done"
		downloadChecker.checkFile(f);
		
		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.CHANGED, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}

	@Test
	public void Should_change_state_to_init_when_file_registers_in_db() {
		// mock two consecutive calls
		when(databaseMock.getMediaByFilename(any(String.class), any(Boolean.class)))
		.thenReturn(null)
		.thenReturn(
				Media.newBuilder()
				.setID(1)
				.setFilename("testfile")
				.setFilepath("/etc/test")
				.setIndex(1)
				.setDownloadComplete(false)
				.build());
		
		// register file. File not present. should go to WAIT
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);

		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.WAIT, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
		// executor checks files again. now it should be present in db
		downloadChecker.checkCachedFiles();
		
		assertEquals(FileChangedState.INIT, downloadChecker.getFiles().get("/etc/test/testfile").getState());
		
	}
	
	@Test
	public void Should_set_state_to_DONE_if_file_was_not_changed() {
		registerDatabaseMock(false);
		registerMediaHelperMock();
		
		// register file. -> INIT
		FileRepresentation f = new FileRepresentation("/etc/test", "testfile", 1000, 1000);
		downloadChecker.checkFile(f);

		// executor runs. Nothing happened. Set state to DONE
		downloadChecker.checkCachedFiles();

		assertEquals(1, downloadChecker.getFiles().size());
		assertEquals(true, downloadChecker.getFiles().containsKey("/etc/test/testfile"));
		assertEquals(FileChangedState.DONE, downloadChecker.getFiles().get("/etc/test/testfile").getState());

	}
	
	private void registerDatabaseMock(boolean downloadComplete) {
		when(databaseMock.getMediaByFilename(any(String.class), any(Boolean.class))).thenReturn(
				Media.newBuilder()
				.setID(1)
				.setFilename("testfile")
				.setFilepath("/etc/test")
				.setIndex(1)
				.setDownloadComplete(downloadComplete)
				.build());
	}
	
	private void registerMediaHelperMock() {
		when(mediaMetadataHelperMock.getMediaMetadata(any(Media.class)))
		.thenReturn(new MediaMetadata(1000, "24.4"));
	}
}