package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import se.qxx.jukebox.SubtitleDownloader;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.settings.Settings;

public class TestSubsDir {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			
			Settings.initialize();	
			
			String filename = args[0];
			
			String filePath = FilenameUtils.getFullPathNoEndSeparator(filename);
			String singleFile = FilenameUtils.getName(filename);

			Media md = Media.newBuilder()
					.setID(-1)
					.setIndex(1)
					.setFilepath(filePath)
					.setFilename(singleFile)
					.build();

			SubtitleDownloader.get().checkMovieDirForSubs(md);
		}
		else {
			System.out.println("No arguments");
		}
	}
}
