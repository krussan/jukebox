package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.subtitles.MkvSubtitleReader;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.ProtoDB;

public class TestExtractMkv {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 1) {
			
			try {
				String filename = args[0];
				String outputPath = args[1];
				MkvSubtitleReader.extractSubs(filename, outputPath);
				
			} catch (Exception e) {
				System.out.println("failed to get information from database");
				System.out.println(e.toString());

			}
		}
		else {
			System.out.println("No arguments");
		}
	}
	
	

}
