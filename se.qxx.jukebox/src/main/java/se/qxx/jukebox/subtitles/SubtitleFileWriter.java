package se.qxx.jukebox.subtitles;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import fr.noop.subtitle.model.SubtitleObject;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.model.SubtitleWriter;
import fr.noop.subtitle.srt.SrtParser;
import fr.noop.subtitle.vtt.VttWriter;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;

@Singleton
public class SubtitleFileWriter implements ISubtitleFileWriter {

	private IJukeboxLogger log;
	
	@Inject
	public SubtitleFileWriter(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.WEBSERVER));
	}
	
	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}


	@Override
	public File getTempFile(Subtitle sub, String extension) {
		File tempDir = FileUtils.getTempDirectory();
		
		return new File(String.format("%s/%s.%s", 
				tempDir.getAbsolutePath(), 
				FilenameUtils.removeExtension(sub.getFilename())),
				extension);
	}
	
	@Override
	public File writeSubtitleToFile(Subtitle sub, File destinationFile) throws IOException, SubtitleParsingException, FileNotFoundException {
		this.getLog().Info(String.format("Writing sub to file :: %s", destinationFile.getAbsolutePath()));
		BOMInputStream bom = new BOMInputStream(new ByteArrayInputStream(sub.getTextdata().toByteArray()));
		
		IOUtils.copy(bom, new FileOutputStream(destinationFile));
		
		return destinationFile;
	}
	

	@Override
	public File writeSubtitleToFileVTT(Subtitle sub, File destinationFile) throws IOException, SubtitleParsingException, FileNotFoundException {
		this.getLog().Info(String.format("Writing sub to file :: %s", destinationFile.getAbsolutePath()));

		BOMInputStream bom = new BOMInputStream(new ByteArrayInputStream(sub.getTextdata().toByteArray()));
		
		//TODO: change this based on extension
		SrtParser parser = new SrtParser("UTF-8");
		SubtitleObject srt = parser.parse(bom);
		
		SubtitleWriter writer = new VttWriter("utf-8");
		
		FileOutputStream fos = new FileOutputStream(destinationFile);
		writer.write(srt, fos);
		
		fos.flush();
		fos.close();
		
		return destinationFile;
	}

}
