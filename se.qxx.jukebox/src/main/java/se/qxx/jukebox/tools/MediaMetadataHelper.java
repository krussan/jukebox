package se.qxx.jukebox.tools;

import java.io.FileNotFoundException;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Singleton;

import net.sourceforge.filebot.mediainfo.MediaInfo;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IMediaMetadataHelper;

@Singleton
public class MediaMetadataHelper implements IMediaMetadataHelper  {

	private IJukeboxLogger log;
	
	public MediaMetadataHelper(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.FIND));
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	@Override
	public MediaMetadata getMediaMetadata(Media md) {
		String fullFilePath = Util.getFullFilePath(md);
		this.getLog().Debug(String.format("Finding media meta data for file %s", md.getFilename()));

		try {
			MediaMetadata mm = getMediaMetadata(fullFilePath);
			return mm;
							
		} catch (Exception e) {
    		this.getLog().Error(String.format("Error when retreiving media info from file %s", fullFilePath), e);
		}

		return null;		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IMediaMetadataHelper#getMediaMetadata(java.lang.String)
	 */
	@Override
	public MediaMetadata getMediaMetadata(String fullFilePath) throws FileNotFoundException {
	    MediaInfo MI = new MediaInfo();
	    
	    MediaMetadata md = new MediaMetadata();
	    
	    if (MI.Open(fullFilePath)>0) {
	    	try {
			    String duration = MI.Get(MediaInfo.StreamKind.General, 0, "Duration");
			    if (StringUtils.isNumeric(duration))
			    	md.setDuration(Long.parseLong(duration));
			    
			    md.setFramerate(MI.Get(MediaInfo.StreamKind.Video, 0, "FrameRate"));
			    
			    int numberOfTextStreams = MI.Count_Get(MediaInfo.StreamKind.Text);
			    for (int i=0; i< numberOfTextStreams; i++) {
			    	md.addSubtitle(MI.Get(MediaInfo.StreamKind.Text, i, "Language"));
			    }
	    	}
	    	catch (Exception e) {
	    		this.getLog().Error(String.format("Error when retreiving media info from file %s", fullFilePath), e);
	    	}
	    }
	    else {
	    	throw new FileNotFoundException();
	    }
	    
	    return md;
	}
	
	@Override
	public Media addMediaMetadata(Media md) {
		MediaMetadata mm = getMediaMetadata(md);
		if (mm == null)
			return md;
		
		return Media.newBuilder(md)
				.setMetaDuration(mm.getDurationSeconds())
				.setMetaFramerate(mm.getFramerate())
				.build();
	}

}
