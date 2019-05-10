package se.qxx.jukebox.interfaces;

import java.util.List;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleUri;
import se.qxx.jukebox.webserver.StreamingFile;

public interface IStreamingWebServer {

	public String getIpAddress();
	public void setIpAddress(String ipAddress);
	public void initializeMappings(ISettings settings);
	
	public StreamingFile registerFile(String streamingFilename, String filename);
	public StreamingFile registerFile(Media md);
	public StreamingFile registerSubtitle(Subtitle sub, SubtitleRequestType subtitleRequestType);
	
	public List<SubtitleUri> getSubtitleUris(Media md, SubtitleRequestType subtitleRequestType);
	
	public String getStreamingFilename(Media md);
	public void deregisterFile(String streamingFile);

	public Response serve(IHTTPSession session);
	public String getMimeType(String uri, String filename);
	
	public Runnable getRunnable();
	String getStreamUri(String streamingFile);
	public void initialize();

	public boolean fileIsRegistered(String streamingFile);
	public String getRegisteredFile(String streamingFile);

	int getListeningPort();
	
}