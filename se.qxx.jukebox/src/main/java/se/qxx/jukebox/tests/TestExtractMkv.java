package se.qxx.jukebox.tests;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import se.qxx.jukebox.subtitles.MkvSubtitleReader;

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
