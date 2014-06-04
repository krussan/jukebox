package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;

import se.qxx.jukebox.ExtensionFileFilter;
import se.qxx.jukebox.Util;
import se.qxx.jukebox.builders.ParserBuilder;
import se.qxx.jukebox.builders.ParserMovie;
import se.qxx.jukebox.settings.Settings;

public class TestParserBuilder {
	public static void main(String[] args) throws IOException, JAXBException {
		
		Settings.initialize();
		
		if (args.length > 0) {
			ExtensionFileFilter eff = new ExtensionFileFilter();
			eff.addExtensions(Util.getExtensions());
			
			List<File> list = Util.getFileListing(new File(args[0]), eff);
			
			for (File f : list) {
				ParserBuilder b = new ParserBuilder();
				ParserMovie pm = b.extractMovieParser(f.getParent(), FilenameUtils.getBaseName(f.getName()));
				
				System.out.println(pm.toString());
			}
		}
	}
	
}
