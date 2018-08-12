package se.qxx.jukebox.webserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import com.google.protobuf.ByteString;

import fi.iki.elonen.NanoHTTPD;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.Log;
import se.qxx.jukebox.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.settings.JukeboxListenerSettings.WebServer.MimeTypeMap.Extension;
import se.qxx.jukebox.settings.Settings;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.model.CaseInsensitiveMap;

public class StreamingWebServer extends NanoHTTPD {

	private static StreamingWebServer _instance;
	
	// maps stream name to actual file name
	private Map<String, String> streamingMap = null;
	private Map<String, String> mimeTypeMap = null;
	private Map<String, String> extensionMap = null;
	
	private AtomicInteger streamingIterator;
	
	private Configuration templateConfig = null;
	
	public StreamingWebServer(String host, int port) {
		super(host, port);
		
		streamingIterator = new AtomicInteger();
		streamingMap = new ConcurrentHashMap<String, String>();
		mimeTypeMap = new CaseInsensitiveMap();
		
		for (Extension e : Settings.get().getWebServer().getMimeTypeMap().getExtension() ) {
			mimeTypeMap.put(e.getValue(), e.getMimeType());
		}
	
		for (se.qxx.jukebox.settings.JukeboxListenerSettings.WebServer.ExtensionOverrideMap.Extension e : 
			Settings.get().getWebServer().getExtensionOverrideMap().getExtension()) {
			extensionMap.put(e.getValue(), e.getOverride());
		}
		

		
	}
	
	public String registerFile(String file) {
		int iter = streamingIterator.incrementAndGet();
		
		String extension = getOverrideExtension(file);
		
		String streamingFile = String.format("stream%s.%s", iter, extension);
		streamingMap.put(streamingFile, file);
		
		Log.Info(String.format("Registering file %s :: %s", streamingFile, file), LogType.WEBSERVER);
		
		return getStreamUri(streamingFile);
	}

	private String getOverrideExtension(String file) {
		String extension = FilenameUtils.getExtension(file).toLowerCase();
		
		if (extensionMap.containsKey(extension))
			return extensionMap.get(extension);
		
		return extension;
	}
	
	public void deregisterFile(String streamingFile) {
		streamingMap.remove(streamingFile);
	}
	
	public String registerSubtitle(Subtitle sub) {

		try {
		
			File tempFile = Util.writeSubtitleToTempFileVTT(sub);
			
			return registerFile(tempFile.getAbsolutePath());
			
		} catch (Exception e) {
			Log.Error("Error while parsing and writing subtitle file", LogType.WEBSERVER, e);
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
	    logHeaders(header);
	}

	private void logHeaders(Map<String, String> header) {
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
        
        if (uri.startsWith("stream")) {
            //TODO: If stream filename is not in one of the added files return
            // a not found response
            if (!streamingMap.containsKey(uri))
            	return getNotFoundResponse();
            
            // map the streaming uri to the actual file
            String filename = streamingMap.get(uri);
            File f = new File(filename);
            
            Log.Debug(String.format("Serving file :: %s", filename), LogType.WEBSERVER);
            if (!f.exists()) {
            	Log.Debug("FILE DOES NOT EXIST", LogType.WEBSERVER);
            	return getNotFoundResponse();
            }
            
            String mimeTypeForFile = getMimeType(uri, filename);
            
            Response response = serveFile(headers, f, mimeTypeForFile);
            logResponse(response);
            
            return response != null ? response : getNotFoundResponse();        	
        }

        if (uri.equalsIgnoreCase("index.html")) {
        	return serveRootHtml();
        }

        if (uri.startsWith("movie")) {
        	int id = Integer.parseInt(uri.replaceAll("movie", "").replaceAll(".html", ""));
        	
        	return serveMovieHtml(id);
        }

        if (uri.startsWith("epithumb")) {
        	int id = Integer.parseInt(uri.replaceAll("epithumb", ""));
        	
        	return serveEpisodeThumbnail(id);
        }

        if (uri.startsWith("epiimage")) {
        	int id = Integer.parseInt(uri.replaceAll("epiimage", ""));
        	
        	return serveEpisodeImage(id);
        }
        
        if (uri.startsWith("thumb")) {
        	int id = Integer.parseInt(uri.replaceAll("thumb", ""));
        	
        	return serveThumbnail(id);
        }

        if (uri.startsWith("image")) {
        	int id = Integer.parseInt(uri.replaceAll("image", ""));
        	
        	return serveMovieImage(id);
        }

        if (uri.startsWith("css")) {
        	try {
				return serveCss();
			} catch (IOException e) {
				createResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
			}
        }
        	
        	
        return getForbiddenResponse("Won't serve anything else than registered files for streaming.");

    }

	private String getMimeType(String uri, String filename) {
        //use mp4 for now. Default seems to be octet-stream for unknown file types 
        // and that does not fit well with some video players

		String extension = FilenameUtils.getExtension(filename);
		if (mimeTypeMap.containsKey(extension))
			return mimeTypeMap.get(extension);

		return getMimeTypeForFile(uri);
	}
	
	private void logResponse(Response response) {
        Log.Debug("----- Response Headers ----", LogType.WEBSERVER);
        Log.Debug(String.format("  Status :: %s", response.getStatus()), LogType.WEBSERVER);
        Log.Debug(String.format("    Content-Type   :: %s", response.getHeader("Content-Type")), LogType.WEBSERVER);
        Log.Debug(String.format("    Content-Length :: %s", response.getHeader("Content-Length")), LogType.WEBSERVER);
        Log.Debug(String.format("    Content-Range  :: %s", response.getHeader("Content-Range")), LogType.WEBSERVER);
        Log.Debug(String.format("    ETag           :: %s", response.getHeader("ETag")), LogType.WEBSERVER);
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
	
	private Response serveRootHtml() {
		List<Movie> movies = DB.searchMoviesByTitle("");
		
		try {
			return createResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, TemplateEngine.get().listMovies(movies));
		} catch (IOException | TemplateException e) {
			return createResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
		}
		
	}
	
	private Response serveMovieHtml(int id) {
		Movie m = DB.getMovie(id);
		try {
			return createResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, TemplateEngine.get().showMovieHtml(m));
		} catch (TemplateException | IOException e) {
			return createResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
		}
	}
	
	private Response serveThumbnail(int id) {
		Movie m = DB.getMovie(id);
		
		if (m != null && m.getThumbnail() != null) {
			serveImage(m.getThumbnail());
		}
		
		return emptyResponse(); 
	}

	private Response serveEpisodeThumbnail(int id) {
		Episode ep = DB.getEpisode(id);
		
		if (ep != null && ep.getThumbnail() != null) {
			return serveImage(ep.getThumbnail());
		}
		else {
			return emptyResponse();
		}
	}
	
	private Response emptyResponse() {
		ByteArrayInputStream bis = new ByteArrayInputStream(new byte[0]);
		return createResponse(Response.Status.OK, "image/jpeg", bis);
	}

	private Response serveImage(ByteString bytes) {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes.toByteArray());

		return createResponse(Response.Status.OK, "image/jpeg", bis);
	}

	private Response serveMovieImage(int id) {
		Movie m = DB.getMovie(id);

		if (m != null && m.getImage() != null) 
			serveImage(m.getImage());
		
		return emptyResponse();
	}

	private Response serveEpisodeImage(int id) {
		Episode m = DB.getEpisode(id);

		if (m != null && m.getImage() != null) 
			serveImage(m.getImage());
		
		return emptyResponse();
	}

	private Response serveCss() throws IOException {
		String css = readResource("style.css");
		return createResponse(Response.Status.OK, "text/css", css);
	}

	private String readResource(String resourceName) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName)));
		StringBuilder sb = new StringBuilder();

		String line;
		while ((line = in.readLine()) != null){
			sb.append(line).append("\n");
		}
		
		return sb.toString();	
	}


    protected Response getForbiddenResponse(String s) {
    	Log.Error("FORBIDDEN", LogType.WEBSERVER);;
        return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: "
            + s);
    }
    
    protected Response getNotFoundResponse() {
    	Log.Error("NOT FOUND", LogType.WEBSERVER);;
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
     * Serves file from homeDir and its' subdirectories (only). Uses only URI,
     * ignores all headers and HTTP parameters.
     */
    Response serveFile(Map<String, String> header, File file, String mime) {
        Response res;

        // Calculate etag
        String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

        try {
            // Support (simple) skipping:
            String range = header.get("range");            
            Range r = Range.parse(range, file.length());
            
            // get if-range header. If present, it must match etag or else we
            // should ignore the range request
            String ifRange = header.get("if-range");
            boolean headerIfRangeMissingOrMatching = (ifRange == null || etag.equals(ifRange));

            String ifNoneMatch = header.get("if-none-match");
            boolean headerIfNoneMatchPresentAndMatching = ifNoneMatch != null && ("*".equals(ifNoneMatch) || ifNoneMatch.equals(etag));

            // Change return code and add Content-Range header when skipping is
            // requested

            if (headerIfRangeMissingOrMatching && range != null && r.getStartFrom() >= 0 && r.getStartFrom() < r.getFileLength()) {
                // range request that matches current etag
                // and the startFrom of the range is satisfiable
                if (headerIfNoneMatchPresentAndMatching) {
                    // range request that matches current etag
                    // and the startFrom of the range is satisfiable
                    // would return range from file
                    // respond with not-modified
                	Log.Debug("Response type 1", LogType.WEBSERVER);
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else {
                	Log.Debug("Response type 2", LogType.WEBSERVER);
                	res = getRangedResponse(file, mime, r);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && r.getStartFrom() >= r.getFileLength()) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                	Log.Debug("Response type 3", LogType.WEBSERVER);
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", String.format("bytes */%s", r.getFileLength()));
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                	Log.Debug("Response type 4", LogType.WEBSERVER);
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified
                	Log.Debug("Response type 5", LogType.WEBSERVER);
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else {
                    // supply the file
                	Log.Debug("Response type 6", LogType.WEBSERVER);
                    res = newFixedFileResponse(file, mime);
                    res.addHeader("Content-Length", "" + r.getFileLength());
                }
            }
        } catch (IOException ioe) {
            res = getForbiddenResponse("Reading file failed.");
        }
        
        // enable CORS
        res.addHeader("ETag", etag);
    	res.addHeader("Access-Control-Allow-Origin", "*");

        return res;
    }

	private Response getRangedResponse(File file, String mime, Range r)
			throws FileNotFoundException, IOException {
		
		FileInputStream fis = getRangedFileStream(file, r);
		fis.skip(r.getStartFrom());

		Response res = NanoHTTPD.newChunkedResponse(Status.PARTIAL_CONTENT, mime, fis);
		//Response res = NanoHTTPD.newFixedLengthResponse(Status.PARTIAL_CONTENT, mime, fis, r.getLength());
		res.addHeader("Accept-Ranges", "bytes");
		res.addHeader("Content-Length", "" + r.getLength());
		res.addHeader("Content-Range", r.getContentRange());
		
		return res;
	}

	private FileInputStream getRangedFileStream(File file, Range r) throws FileNotFoundException {
		final long newLen = r.getLength();
		FileInputStream fis = new FileInputStream(file) {
			@Override
			public int available() throws IOException {
				// TODO Auto-generated method stub
				return (int)newLen;
			}                    	
		};
		return fis;
	}
    
    private Response newFixedFileResponse(File file, String mime) throws FileNotFoundException {
        Response res;
        res = NanoHTTPD.newFixedLengthResponse(Status.OK, mime, new FileInputStream(file), (int) file.length());
        res.addHeader("Accept-Ranges", "bytes");
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
