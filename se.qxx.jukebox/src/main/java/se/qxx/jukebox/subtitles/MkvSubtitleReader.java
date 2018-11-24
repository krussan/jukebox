package se.qxx.jukebox.subtitles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;

import com.google.protobuf.ByteString;
import com.matthewn4444.ebml.Attachments;
import com.matthewn4444.ebml.EBMLReader;
import com.matthewn4444.ebml.subtitles.Caption;
import com.matthewn4444.ebml.subtitles.Subtitles;

import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.model.SubtitleText;
import fr.noop.subtitle.model.SubtitleWriter;
import fr.noop.subtitle.srt.SrtCue;
import fr.noop.subtitle.srt.SrtObject;
import fr.noop.subtitle.srt.SrtWriter;
import fr.noop.subtitle.util.SubtitlePlainText;
import fr.noop.subtitle.util.SubtitleTextLine;
import fr.noop.subtitle.util.SubtitleTimeCode;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;

public class MkvSubtitleReader implements IMkvSubtitleReader {
	
	private ISubtitleFileWriter subFileWriter;
	private IJukeboxLogger log;
	
	public MkvSubtitleReader(ISubtitleFileWriter subFileWriter, LoggerFactory loggerFactory) {
		this.setSubFileWriter(subFileWriter);
		this.setLog(loggerFactory.create(LogType.SUBS));
	}
	
	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public ISubtitleFileWriter getSubFileWriter() {
		return subFileWriter;
	}
	public void setSubFileWriter(ISubtitleFileWriter subFileWriter) {
		this.subFileWriter = subFileWriter;
	}
	
	@Override
	public void extractSubs(String filename, String outputPath) throws FileNotFoundException, IOException, SubtitleParsingException {
		List<Subtitle> subs = extractSubs(filename);
		for (Subtitle s : subs) {
			File destinationFile = new File(String.format("%s/%s", outputPath, s.getFilename()));
			this.getSubFileWriter().writeSubtitleToFile(s, destinationFile);
		}
	}
	
	@Override
	public List<Subtitle> extractSubs(String filename) {
		// Please run the code in a thread or AsyncTask (in Android)
		// You must follow the order of function calls to read the subtitles and
		// attachments successfully
		EBMLReader reader = null;
		List<Subtitle> result = new ArrayList<Subtitle>();
		
		try {
		    reader = new EBMLReader(filename);

		    // Check to see if this is a valid MKV file
		    // The header contains information for where all the segments are located
		    if (!reader.readHeader())
	    		return result;

		    // Read the tracks. This contains the details of video, audio and subtitles
		    // in this file
		    reader.readTracks();

		    readAttachements(reader);

		    // Check if there are any subtitles in this file
		    int numSubtitles = reader.getSubtitles().size();
		    if (numSubtitles == 0)
		    	return result;

		    // You need this to find the clusters scattered across the file to find
		    // video, audio and subtitle data
		    reader.readCues();

		    // OPTIONAL: You can read the header of the subtitle if it is ASS/SSA format
		    List<String> languages = readHeaders(reader);

		    readCueFrames(reader);
		    readSubs(reader, languages, result);
		    
		} catch (IOException e) {
			this.getLog().Error("Error when getting subs from mkv", e);
		} finally {
		    try {
		        // Remember to close this!
		        reader.close();
		    } catch (Exception e) {}
		}
		
		return result;
	}

	private void readSubs(EBMLReader reader, List<String> languages, List<Subtitle> result) throws IOException {
		// OPTIONAL: we get the subtitle data that was just read
		for (int i = 0; i < reader.getSubtitles().size(); i++) {
		    List<Caption> subtitles = reader.getSubtitles().get(i).readUnreadSubtitles();
		    
		    String language = "Unknown";
		    if (i < languages.size())
		    	language = languages.get(i);	
		    
		    if (StringUtils.isEmpty(language))
		    	language = "Unknown";
		    
		    // Do want you like with partial read of subtitles, you can technically
		    // write the subtitles to file here
		    
		    //convert to srt here?
		    SrtObject srt = getSubtitlesAsSrt(subtitles);

			result.add(getSubtitle(srt, i, language));
			
		}
	}

	private void readCueFrames(EBMLReader reader) throws IOException {
		// Read all the subtitles from the file each from cue index.
		// Once a cue is parsed, it is cached, so if you read the same cue again,
		// it will not waste time.
		// Performance-wise, this will take some time because it needs to read
		// most of the file.
		for (int i = 0; i < reader.getCuesCount();i++) {
		    reader.readSubtitlesInCueFrame(i);
		}
	}

	private List<String> readHeaders(EBMLReader reader) {
		List<String> languages = new ArrayList<String>();
		for (int i = 0; i < reader.getSubtitles().size(); i++) {
			Subtitles ss = reader.getSubtitles().get(i);
			if (ss != null && ss instanceof Subtitles)
				languages.add(ss.getLanguage());
		}
		
		return languages;
	}

	private Subtitle getSubtitle(SrtObject srt, int i, String language) throws IOException {
		SubtitleWriter writer = new SrtWriter("utf-8");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(srt, baos);
		
		baos.flush();
		baos.close();
		
		String filename = String.format("MkvFile-%s-%s.srt", i, language);
		
		return Subtitle.newBuilder()
			.setID(-1)
			.setRating(Rating.ExactMatch)
			.setTextdata(ByteString.copyFrom(baos.toByteArray()))
			.setLanguage(language)
			.setDescription(filename)
			.setFilename(filename)
			.setMediaIndex(1)
			.build();
	}

	private void readAttachements(EBMLReader reader)
			throws IOException, FileNotFoundException {
		// Extract the attachments: fonts, images etc
		// This function takes a couple of milliseconds usually less than 500ms
		reader.readAttachments();
		List<Attachments.FileAttachment> attachments = reader.getAttachments();

		// Write each attachment to file
		if (attachments != null) {
		    for (Attachments.FileAttachment attachment : attachments) {
		        //File filepath = new File(String.format("%s/%s", outputPath, attachment.getName()));
		        //FileOutputStream fos = new FileOutputStream(filepath);
		        
	            // This will now allocate and copy data
	            attachment.getData();
	            //fos.write(buffer);
		        
		    }
		}
	}

	private SrtObject getSubtitlesAsSrt(List<Caption> subtitles) {
		SrtObject srt = new SrtObject();
		for (Caption cap : subtitles) {
			SrtCue cue = new SrtCue();
			String capline = cap.getFormattedVTT();
			String[] lines = capline.split("\n");
			
			for (String line : lines) {
		    	if (!StringUtils.isBlank(line)) {
			    	SubtitleTextLine textLine = new SubtitleTextLine();
			    	SubtitleText text = new SubtitlePlainText(line);

			    	textLine.addText(text);
		    		cue.addLine(textLine);
		    	}
			}
			
			cue.setStartTime(new SubtitleTimeCode(cap.getStartTime().getTime()));
			cue.setEndTime(new SubtitleTimeCode(cap.getEndTime().getTime()));
			
			if (cue.getLines().size() > 0)
				srt.addCue(cue);
		}
		return srt;
	}
}
