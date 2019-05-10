package se.qxx.jukebox.webserver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.protobuf.ByteString;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import freemarker.template.TemplateException;
import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.MediaConverterState;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleRequestType;
import se.qxx.jukebox.domain.JukeboxDomain.SubtitleUri;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IDatabase;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IStreamingWebServer;
import se.qxx.jukebox.interfaces.ISubtitleFileWriter;
import se.qxx.jukebox.settings.JukeboxListenerSettings.WebServer.MimeTypeMap.Extension;
import se.qxx.jukebox.tools.Util;
import se.qxx.protodb.model.CaseInsensitiveMap;

public class StreamingWebServer extends NanoHTTPD implements IStreamingWebServer {

	private IDatabase database;
	private IJukeboxLogger log;
	
	private final List<String> acceptedStreamingTypes = Arrays.asList("sub", "stream");
	
	// maps stream name to actual file name
	private Map<String, String> streamingMap = null;
	private Map<String, String> mimeTypeMap = null;
	private Map<String, String> extensionMap = null;
	
	private static Map<SubtitleRequestType, String> subtitleExtension = new ConcurrentHashMap<>();

	/*
	 * registerSubtitle(Subtitle)
	 * registerMedia(Media)
	 * registerFile (internal)
	 * 
	 * stream(mediaID).mp4
	 * sub(subID).vtt
	 * 
	 */
	private String ipAddress;
	private ISubtitleFileWriter subWriter; 
	
	@Inject
	public StreamingWebServer(
			IDatabase database, 
			LoggerFactory loggerFactory,
			ISubtitleFileWriter subWriter,
			@Assisted("webserverport") Integer port) {
		super(port);
		this.setSubWriter(subWriter);
		this.setDatabase(database);
		this.setLog(loggerFactory.create(LogType.WEBSERVER));
		
		streamingMap = new ConcurrentHashMap<String, String>();
		
		setIpAddress();
	}

	public ISubtitleFileWriter getSubWriter() {
		return subWriter;
	}

	public void setSubWriter(ISubtitleFileWriter subWriter) {
		this.subWriter = subWriter;
	}

	public IJukeboxLogger getLog() {
		return log;
	}

	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}

	public IDatabase getDatabase() {
		return database;
	}

	public void setDatabase(IDatabase database) {
		this.database = database;
	}

	private void setIpAddress() {
		this.setIpAddress(Util.findIpAddress());
		this.getLog().Info(String.format("Setting Ip Address :: %s", this.getIpAddress()));
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#initializeMappings()
	 */
	@Override
	public void initializeMappings(ISettings settings) {
		mimeTypeMap = new CaseInsensitiveMap();
		extensionMap = new CaseInsensitiveMap();
		
		for (Extension e : settings.getSettings().getWebServer().getMimeTypeMap().getExtension() ) {
			mimeTypeMap.put(e.getValue(), e.getMimeType());
		}
	
		for (se.qxx.jukebox.settings.JukeboxListenerSettings.WebServer.ExtensionOverrideMap.Extension e :
			settings.getSettings().getWebServer().getExtensionOverrideMap().getExtension()) {
			extensionMap.put(e.getValue(), e.getOverride());
		}
		
		subtitleExtension.put(SubtitleRequestType.WebVTT, "vtt");
		subtitleExtension.put(SubtitleRequestType.SubRip, "srt");
		
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#registerFile(java.lang.String)
	 */
	@Override
	public StreamingFile registerFile(String streamingFilename, String filename) {
		
		streamingMap.put(streamingFilename, filename);
		
		String uri = getStreamUri(streamingFilename);
		this.getLog().Info(String.format("Registering file %s :: %s", streamingFilename, filename));
		this.getLog().Info(String.format("URI :: %s", uri));
				
		return new StreamingFile(uri, getMimeType(uri, filename));
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#registerFile(se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public StreamingFile registerFile(Media md) {
		return registerFile(
				getStreamingUri(md), 
				getStreamingFilename(md));
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#getStreamingFilename(se.qxx.jukebox.domain.JukeboxDomain.Media)
	 */
	@Override
	public String getStreamingFilename(Media md) {
		String filename;
		
		if (md.getConverterState() == MediaConverterState.Completed && !StringUtils.isEmpty(md.getConvertedFileName())) {
			filename = Util.getConvertedFullFilepath(md);
		}
		else {
			filename = Util.getFullFilePath(md);
		}
		
		return filename;
	}

	private String getOverrideExtension(String file) {
		String extension = FilenameUtils.getExtension(file).toLowerCase();
		
		if (extensionMap.containsKey(extension)) {
			this.getLog().Debug(String.format("Overriding extension %s -> %s", extension, extensionMap.get(extension)));
			return extensionMap.get(extension);
		}
		
		return extension;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#deregisterFile(java.lang.String)
	 */
	@Override
	public void deregisterFile(String streamingFile) {
		streamingMap.remove(streamingFile);
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#registerSubtitle(se.qxx.jukebox.domain.JukeboxDomain.Subtitle)
	 */
	@Override
	public StreamingFile registerSubtitle(Subtitle sub, SubtitleRequestType subtitleRequestType) {

		try {
			String streamingFile = getStreamingUri(sub, subtitleRequestType);
			String filename = StringUtils.EMPTY;
			
			if (fileIsRegistered(streamingFile)) {
				filename = streamingMap.get(streamingFile);
			}
			else {
				File tempFile = this.getSubWriter().getTempFile(sub, subtitleExtension.get(subtitleRequestType));
				filename = tempFile.getAbsolutePath();
				
				if (!tempFile.exists())
					this.getSubWriter().writeSubtitleToFileConvert(sub, tempFile);
			}
			
			return registerFile(
					getStreamingUri(sub, subtitleRequestType), 
					filename);
			
		} catch (Exception e) {
			this.getLog().Error("ERROR while parsing and writing subtitle file", e);
		}
		
		return null;
	}
	

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#serve(fi.iki.elonen.NanoHTTPD.IHTTPSession)
	 */
	@Override
	public Response serve(IHTTPSession session) {
		setPriority();
		
        Map<String, String> header = session.getHeaders();
        String uri = session.getUri();
    
        logRequest(session, header, uri);
        
        return respond(Collections.unmodifiableMap(header), session, uri);
    } 

	private void logRequest(IHTTPSession session, Map<String, String> header, String uri) {
		this.getLog().Info(String.format("%s '%s'", session.getMethod(), uri));
	    logHeaders(header);
	}

	private void logHeaders(Map<String, String> header) {
		Iterator<String> e = header.keySet().iterator();
	    while (e.hasNext()) {
	        String value = e.next();
	        this.getLog().Info(String.format("  HDR: '%s' = '%s'", value, header.get(value)));
	    }
	}

	private Response respond(Map<String, String> headers, IHTTPSession session, String uriPath) {
        String uri = getUriWithoutArguments(uriPath);
        
        // This server only serves specific stream uri's  
        this.getLog().Info(String.format("Requesting file :: %s", uri));
        
        if (Iterables.any(acceptedStreamingTypes, (x) -> uri.startsWith(x))) {
            return serveStreamingFile(headers, uri);        	
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

	private Response serveStreamingFile(Map<String, String> headers, String uri) {
		//If stream filename is not in one of the added files return
		//a not found response
		if (!fileIsRegistered(uri))
			return getNotFoundResponse();
		
		// map the streaming uri to the actual file
		String filename = streamingMap.get(uri);
		File f = new File(filename);
		
		this.getLog().Debug(String.format("Serving file :: %s", filename));
		if (!f.exists()) {
			this.getLog().Debug("FILE DOES NOT EXIST");
			return getNotFoundResponse();
		}
		
		String mimeTypeForFile = getMimeType(uri, filename);
		
		Response response = serveFile(headers, f, mimeTypeForFile);
		logResponse(response);
		
		return response != null ? response : getNotFoundResponse();
	}

	/* (non-Javadoc)
	 * @see se.qxx.jukebox.webserver.IStreamingWebServer#getMimeType(java.lang.String, java.lang.String)
	 */
	@Override
	public String getMimeType(String uri, String filename) {
        //use mp4 for now. Default seems to be octet-stream for unknown file types 
        // and that does not fit well with some video players

		String extension = FilenameUtils.getExtension(filename);
		if (mimeTypeMap.containsKey(extension)) {
			this.getLog().Debug(String.format("Overriding mimeType %s -> %s", extension, mimeTypeMap.get(extension)));
			return mimeTypeMap.get(extension);
		}
			

		return getMimeTypeForFile(uri);
	}
	
	private void logResponse(Response response) {
        this.getLog().Debug("----- Response Headers ----");
        this.getLog().Debug(String.format("  Status :: %s", response.getStatus()));
        this.getLog().Debug(String.format("    Content-Type   :: %s", response.getHeader("Content-Type")));
        this.getLog().Debug(String.format("    Content-Length :: %s", response.getHeader("Content-Length")));
        this.getLog().Debug(String.format("    Content-Range  :: %s", response.getHeader("Content-Range")));
        this.getLog().Debug(String.format("    ETag           :: %s", response.getHeader("ETag")));
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
		List<Movie> movies = this.getDatabase().searchMoviesByTitle("", true, true);
		
		try {
			return createResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, TemplateEngine.get().listMovies(movies));
		} catch (IOException | TemplateException e) {
			return createResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
		}
		
	}
	
	private Response serveMovieHtml(int id) {
		Movie m = this.getDatabase().getMovie(id);
		try {
			return createResponse(Response.Status.OK, NanoHTTPD.MIME_HTML, TemplateEngine.get().showMovieHtml(m));
		} catch (TemplateException | IOException e) {
			return createResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, e.getMessage());
		}
	}
	
	private Response serveThumbnail(int id) {
		Movie m = this.getDatabase().getMovie(id);
		
		if (m != null && m.getThumbnail() != null) {
			serveImage(m.getThumbnail());
		}
		
		return emptyResponse(); 
	}

	private Response serveEpisodeThumbnail(int id) {
		Episode ep = this.getDatabase().getEpisode(id);
		
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
		Movie m = this.getDatabase().getMovie(id);

		if (m != null && m.getImage() != null) 
			serveImage(m.getImage());
		
		return emptyResponse();
	}

	private Response serveEpisodeImage(int id) {
		Episode m = this.getDatabase().getEpisode(id);

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
    	this.getLog().Error("FORBIDDEN");;
        return createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: "
            + s);
    }
    
    protected Response getNotFoundResponse() {
    	this.getLog().Error("NOT FOUND");;
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
                	this.getLog().Debug("Response type 1");
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else {
                	this.getLog().Debug("Response type 2");
                	res = getRangedResponse(file, mime, r);
                }
            } else {

                if (headerIfRangeMissingOrMatching && range != null && r.getStartFrom() >= r.getFileLength()) {
                    // return the size of the file
                    // 4xx responses are not trumped by if-none-match
                	this.getLog().Debug("Response type 3");
                    res = newFixedLengthResponse(Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
                    res.addHeader("Content-Range", String.format("bytes */%s", r.getFileLength()));
                } else if (range == null && headerIfNoneMatchPresentAndMatching) {
                    // full-file-fetch request
                    // would return entire file
                    // respond with not-modified
                	this.getLog().Debug("Response type 4");
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else if (!headerIfRangeMissingOrMatching && headerIfNoneMatchPresentAndMatching) {
                    // range request that doesn't match current etag
                    // would return entire (different) file
                    // respond with not-modified
                	this.getLog().Debug("Response type 5");
                    res = newFixedLengthResponse(Status.NOT_MODIFIED, mime, "");
                } else {
                    // supply the file
                	this.getLog().Debug("Response type 6");
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
	
    @Override
	public String getStreamUri(String streamingFile) {
		return String.format("http://%s%s/%s", 
				this.getIpAddress(),  
				this.getListeningPort() == 80 ? "" : String.format(":%s", this.getListeningPort()),
				streamingFile);
	}
	
	private void setPriority() {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public String getIpAddress() {
		return ipAddress;
	}

	@Override
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	@Override
	public Runnable getRunnable() {
		return this.createServerRunnable(SOCKET_READ_TIMEOUT);
	}

	@Override
	public void initialize() {
		try {
			super.start(SOCKET_READ_TIMEOUT, false);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	@Override
	public List<SubtitleUri> getSubtitleUris(Media md, SubtitleRequestType subtitleRequestType) {
		List<SubtitleUri> result = new ArrayList<>();
		
		// loop through the subtitles
		// check if file exist in the streaming map
		for (Subtitle sub : md.getSubsList()) {
			String streamingUri = getStreamingUri(sub, subtitleRequestType);
			
			if (fileIsRegistered(streamingUri)) {
				result.add(SubtitleUri.newBuilder()
					.setSubtitle(sub)
					.setUrl(getStreamUri(streamingUri))
					.build());				
			}
			else {
				result.add(SubtitleUri.newBuilder()
						.setSubtitle(sub)
						.build());	
			}
		}
		return result;
	}

	private String getStreamingUri(Subtitle sub, SubtitleRequestType subtitleRequestType) {
		return getStreamingUri("sub", sub.getID(), subtitleExtension.get(subtitleRequestType)); 
	}
	
	private String getStreamingUri(Media md) {
		String filename = getStreamingFilename(md);
		String streamingFile = getStreamingUri("stream", md.getID(), getOverrideExtension(filename));
		
		return streamingFile;
	}
	
	private String getStreamingUri(String type, int id, String extension) {
		return String.format("%s%s.%s", type, id, extension);
	}
	
	@Override
	public boolean fileIsRegistered(String streamingFile) {
		return streamingMap.containsKey(streamingFile);
	}

	@Override
	public String getRegisteredFile(String streamingFile) {
		return streamingMap.get(streamingFile);
	}
}
