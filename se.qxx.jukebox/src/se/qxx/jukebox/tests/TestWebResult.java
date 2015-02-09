package se.qxx.jukebox.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Util;
import se.qxx.jukebox.WebResult;
import se.qxx.jukebox.WebRetriever;
import se.qxx.jukebox.builders.FilenameBuilder;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.imdb.ImdbSettings;

public class TestWebResult {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			WebResult r = WebRetriever.getWebResult(args[0]);
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("weboutput.txt"), "UTF-8"));
			bw.write(r.getResult());
			bw.close();
			
			System.out.println("Web request made successfully. Saved to weboutput.txt");
		}
		else {
			System.out.println("No arguments");
		}
	}
	

}
