package se.qxx.jukebox.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.JAXBException;

import se.qxx.jukebox.builders.MovieBuilder;
import se.qxx.jukebox.domain.MovieOrSeries;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder;
import se.qxx.jukebox.subtitles.Subscene;

public class TestSubscene {

	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 1) {
			
			Settings.initialize();	
			Settings.get().getSubFinders().setSubsPath(args[1]);
			
			try {
				for (SubFinder f : Settings.get().getSubFinders().getSubFinder()) {
					if (f.getClazz().endsWith("Subscene")) {
						
						Subscene s = new Subscene(f.getSubFinderSettings());
						
						File ff = new File(args[0]);
						
						if (ff.exists()) {
							
							
							MovieOrSeries mos = MovieBuilder.identify(ff.getAbsolutePath(), ff.getName());
							ArrayList<String> languages = new ArrayList<String>();
							languages.add("Eng");
							languages.add("Swe");
							
							s.findSubtitles(mos, languages);
							
						}
						
					}
				}
				
				
				
			} catch (Exception e) {
				System.out.println("failed to get subtitles");
				System.out.println(e.toString());

			}
		}
		else {
			System.out.println("No arguments");
		}
	}
	

}
