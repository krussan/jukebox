package se.qxx.jukebox.webserver;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.output.StringBuilderWriter;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import se.qxx.jukebox.DB;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

public class TemplateEngine {
	Configuration templateConfig = null;
	private static TemplateEngine _instance = null;
	
	private TemplateEngine() {
		setup();
	}
	
	public static TemplateEngine get() {
		if (_instance == null)
			_instance = new TemplateEngine();
		
		return _instance;
	}
	
	private void setup() {
		templateConfig = new Configuration(Configuration.VERSION_2_3_23);
		templateConfig.setClassForTemplateLoading(getClass(), "/");
		templateConfig.setDefaultEncoding("UTF-8");
		templateConfig.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
		templateConfig.setLogTemplateExceptions(false);
	}
	
	public String listMovies(List<Movie> movies) throws TemplateException, IOException {
		Template template = templateConfig.getTemplate("template.html");
		
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("movies", movies);
		return generateHtml(template, root);
		
	}
	
	public String showMovieHtml(Movie movie) throws TemplateException, IOException {
		Template template = templateConfig.getTemplate("movie.html");
		
		Map<String, Object> root = new HashMap<String, Object>();
		root.put("movie", movie);
		return generateHtml(template, root);
		
	}
	
	private String generateHtml(Template template, Map<String, Object> map) throws TemplateException, IOException{
		StringBuilder sb = new StringBuilder();
		Writer out = new StringBuilderWriter(sb);			
		template.process(map, out);
	
		return sb.toString();
	}


}
