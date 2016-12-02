package se.qxx.jukebox.webserver;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import fi.iki.elonen.InternalRewrite;
import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.WebServerPlugin;
import fi.iki.elonen.util.ServerRunner;
import fr.noop.subtitle.model.SubtitleObject;
import fr.noop.subtitle.model.SubtitleParser;
import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.model.SubtitleWriter;
import fr.noop.subtitle.srt.SrtObject;
import fr.noop.subtitle.srt.SrtParser;
import fr.noop.subtitle.vtt.VttWriter;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

public class StreamingWebServer extends NanoHTTPD {

	private static StreamingWebServer _instance;
	
	
	// maps stream name to actual file name
	private Map<String, String> streamingMap = null;
	private AtomicInteger streamingIterator;
	
	public StreamingWebServer(String host, int port) {
		super(host, port);
		
		streamingIterator = new AtomicInteger();
		streamingMap = new ConcurrentHashMap<String, String>();
	}
	
	public String registerFile(String file) {
		int iter = streamingIterator.incrementAndGet();
		
		String extension = FilenameUtils.getExtension(file);
		
		String streamingFile = String.format("stream%s.%s", iter, extension);
		streamingMap.put(streamingFile, file);
		
		Log.Info(String.format("Registering file %s", streamingFile), LogType.WEBSERVER);
		
		return getStreamUri(streamingFile);
	}
	
	public void deregisterFile(String streamingFile) {
		streamingMap.remove(streamingFile);
	}
	
	public String registerSubtitle(Subtitle sub) {

		try {
		
			ByteArrayInputStream bais = new ByteArrayInputStream(sub.getTextdata().toByteArray());
			
			//TODO: change this based on extension
			SrtParser parser = new SrtParser("utf-8");
			SubtitleObject srt = parser.parse(bais);
			
			SubtitleWriter writer = new VttWriter("utf-8");
			
			File tempDir = FileUtils.getTempDirectory();
			File tempFile = new File(String.format("%s/%s.vtt", tempDir.getAbsolutePath(), FilenameUtils.removeExtension(sub.getFilename())));
			
			FileOutputStream fos = new FileOutputStream(tempFile);
			
			writer.write(srt, fos);
			
			fos.close();
			
			return registerFile(tempFile.getAbsolutePath());
			
		} catch (IOException | SubtitleParsingException e) {
			Log.Error("Error while parsing and writing subtitle file", LogType.SUBS, e);
		}
		
		return StringUtils.EMPTY;
	}

	public Response serve(IHTTPSession session) {
		
        Map<String, String> header = session.getHeaders();
        String uri = session.getUri();
    
        logRequest(session, header, uri);
        
        return respond(Collections.unmodifiableMap(header), session, uri);
    } 

	private void logRequest(IHTTPSession session, Map<String, String> header, String uri) {
		Log.Info(String.format("%s '%s'", session.getMethod(), uri), LogType.WEBSERVER);
	    Iterator<String> e = header.keySet().iterator();
	    while (e.hasNext()) {
	        String value = e.next();
	        Log.Info(String.format("  HDR: '%s' = '%s'", value, header.get(value)), LogType.WEBSERVER);
	    }
	}

	private Response respond(Map<String, String> headers, IHTTPSession session, String uri) {
        uri = getUriWithoutArguments(uri);
        
        // This server only serves specific stream uri's  
        Log.Info(String.format("Requesting file :: %s", uri), LogType.WEBSERVER);
        
        if (!uri.startsWith("stream") || (!uri.endsWith(".mp4") && !uri.endsWith(".srt"))) 
        	return getForbiddenResponse("Won't serve anything else than registered files for streaming.");

        //TODO: If stream filename is not in one of the added files return
        // a not found response
        if (!streamingMap.containsKey(uri))
        	return getNotFoundResponse();
        
        // map the streaming uri to the actual file
        File f = new File(streamingMap.get(uri));
        
        if (!f.exists())
        	return getNotFoundResponse();
        
        String mimeTypeForFile = getMimeTypeForFile(uri);

        Response response = serveFile(uri, headers, f, mimeTypeForFile);
        return response != null ? response : getNotFoundResponse();
    }

	private String getUriWithoutArguments(String uri) {
		// Remove URL arguments
        uri = uri.trim().replace(File.separatorChar, '/');
        if (StringUtils.startsWith(uri, "/"))
        	uri = uri.substring(1);
        
        if (uri.indexOf('?') >= 0) {
            uri = uri.substring(0, uri.indexOf('?'));
        }
		return uri;
	}
	

    protected Response getForbiddenResponse(String s) {
        return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: "
            + s);
    }
    
    protected Response getNotFoundResponse() {
        return createResponse(Response.Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT,
            "Error 404, file not found.");
    }
    
	
    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, InputStream data) {
    	Response res = NanoHTTPD.newChunkedResponse(status, mimeType, data);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = NanoHTTPD.newFixedLengthResponse(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
    
    /**
     * Serves file from homeDir and its' subdirectories (only). Uses only URI, ignores all headers and HTTP parameters.
     */
    private Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());
            // Support (simple) skipping:
            long startFrom = 0;
            long endAt = -1;
            String range = header.get("range");
            if (range != null) {
                if (range.startsWith("bytes=")) {
                    range = range.substring("bytes=".length());
                    int minus = range.indexOf('-');
                    try {
                        if (minus > 0) {
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            // Change return code and add Content-Range header when skipping is requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
                    res.addHeader("ETag", etag);
                } else {
                    if (endAt < 0) {
                        endAt = fileLen - 1;
                    }
                    long newLen = endAt - startFrom + 1;
                    if (newLen < 0) {
                        newLen = 0;
                    }
                    final long dataLen = newLen;
                    FileInputStream fis = new FileInputStream(file) {
                        @Override
                        public int available() throws IOException {
                            return (int) dataLen;
                        }
                    };
                    fis.skip(startFrom);
                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }
        return res;
    }

	public static void setup(String host, int port) {
		_instance = new StreamingWebServer(host, port);

		try {
			_instance.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static StreamingWebServer get() {
		if (_instance == null)
			setup("127.0.0.1", 8080);
		
		return _instance;
	}

	private String getStreamUri(String streamingFile) {
		String uri = streamingFile;
		String ipAddress = "127.0.0.1";
		try {
			ipAddress = Inet4Address.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.Error("Unknown host while getting ip number", LogType.WEBSERVER, e);
		}
		
		return String.format("http://%s%s/%s", 
				ipAddress,  
				this.getListeningPort() == 80 ? "" : String.format(":%s", this.getListeningPort()),
				streamingFile);
	}
	

}
