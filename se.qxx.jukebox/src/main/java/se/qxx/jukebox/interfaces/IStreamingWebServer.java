package se.qxx.jukebox.interfaces;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.webserver.StreamingFile;

public interface IStreamingWebServer {

	public String getIpAddress();
	public void setIpAddress(String ipAddress);
	public void initializeMappings(ISettings settings);
	public StreamingFile registerFile(String filename);
	public StreamingFile registerFile(Media md);
	public String getStreamingFilename(Media md);
	public void deregisterFile(String streamingFile);
	public StreamingFile registerSubtitle(Subtitle sub);
	public Response serve(IHTTPSession session);
	public String getMimeType(String uri, String filename);
	public Runnable getRunnable();
	String getStreamUri(String streamingFile);
	public void initialize();

}