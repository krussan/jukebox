package se.qxx.jukebox.tests;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.interfaces.ISubtitleDownloader;

public class TestSubsDir {

	private ISubtitleDownloader subDownloader;

	@Inject
	public TestSubsDir(ISubtitleDownloader subDownloader) {
		this.subDownloader = subDownloader;
		
	}
	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			Injector injector = Binder.setupBindings(args);
			TestSubsDir prog = injector.getInstance(TestSubsDir.class);
			prog.execute(args[0]);
		}
		else {
			System.out.println("No arguments");
		}
	}
	
	public void execute(String filename) {
		String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
		String singleFile = FilenameUtils.getName(filename);

		Media md = Media.newBuilder()
				.setID(-1)
				.setIndex(1)
				.setFilepath(filePath)
				.setFilename(singleFile)
				.build();

		subDownloader.checkMovieDirForSubs(md);

	}
}
