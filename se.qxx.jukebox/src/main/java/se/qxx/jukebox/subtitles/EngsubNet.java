package se.qxx.jukebox.subtitles;

import se.qxx.jukebox.Language;
import se.qxx.jukebox.settings.JukeboxListenerSettings.SubFinders.SubFinder.SubFinderSettings;

public class EngsubNet extends UndertexterSe {

	public EngsubNet(SubFinderSettings subFinderSettings) {
		super("EngsubNet", Language.English, subFinderSettings);
	}
	
}
