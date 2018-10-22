package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import com.google.protobuf.ByteString;

import freemarker.template.TemplateException;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Rating;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.webserver.TemplateEngine;

public class TestTemplatingEngine {

	@Test
	public void testIndexHtml() throws TemplateException, IOException {
		List<Movie> list = new ArrayList<Movie>();
		
		list.add(Movie.newBuilder()
				.setID(1)
				.setTitle("Movie 1")
				.setIdentifiedTitle("Movie 1")
				.setYear(1999)
				.setType("type1")
				.setImdbUrl("/tt111")
				.build());
		
		list.add(Movie.newBuilder()
				.setID(2)
				.setTitle("Movie 2")
				.setIdentifiedTitle("Movie 2")
				.setYear(2017)
				.setType("type2")
				.setImdbUrl("/tt222")				
				.build());
		
		String actual = TemplateEngine.get().listMovies(list);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("	<head><link rel=\"stylesheet\" href=\"css\"></head>");
		sb.append("	<body>");
		sb.append("		<h1>Jukebox repository<h1>");
		sb.append("		<table>");
		sb.append("			<tr><td><img src=\"thumb1\"/><a href=\"movie1.html\">Movie 1</a></td></tr>");
		sb.append("			<tr><td><img src=\"thumb2\"/><a href=\"movie2.html\">Movie 2</a></td></tr>");
		sb.append("		</table>");
		sb.append("	</body>");
		sb.append("</html>");
		
		assertIgnoreWhitespace(sb.toString(), actual);
	}
	
	@Test
	public void testMovieHtml() throws TemplateException, IOException {
		Movie m = Movie.newBuilder()
		.setID(2)
		.setTitle("Movie 2")
		.setIdentifiedTitle("Movie 2")
		.setYear(2017)
		.setType("type2")
		.setImdbUrl("/tt222")				
		.setRating("6.8")
		.addMedia(Media.newBuilder()
				.setID(1)
				.setFilename("movie2.avi")
				.setFilepath("/video")
				.setIndex(0)
				.setDownloadComplete(false)
				.addSubs(Subtitle.newBuilder()
						.setID(1)
						.setRating(Rating.ExactMatch)
						.setFilename("movie2.srt")
						.setDescription("movie 2 srt")
						.setMediaIndex(0)
						.setLanguage("English")
						.setTextdata(ByteString.EMPTY)
						.build())
				.build())
		.build();
		
		String actual = TemplateEngine.get().showMovieHtml(m);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("	<head><link rel=\"stylesheet\" href=\"css\"></head>");
		sb.append("	<body>");
		sb.append("		<h1>Movie 2</h1>");
		sb.append("		<div id=\"imgMovie\">");
		sb.append("			<img src=\"image2\"/>");
		sb.append("		</div>");
		sb.append("		<div id=\"divMovieInfo\">");
		sb.append("			<ul>");
		sb.append("				<li>2017</li>");
		sb.append("				<li>type2</li>");
		sb.append("				<li>6.8</li>");
		sb.append("				<li><a href=\"http://imdb.com//tt222\">imdb</a></li>");
		sb.append("			</ul>");
		sb.append("		</div>");
		sb.append("		<div id=\"divSubtitles\">");
		sb.append("			<ul>");
		sb.append("				<li>movie2.srt - ExactMatch</li>");
		sb.append("			</ul>");
		sb.append("		</div>");
		sb.append("	</body>");
		sb.append("</html>");
	
		assertIgnoreWhitespace(sb.toString(), actual);
	}

	private String removeWhiteSpaces(String input) {
		Pattern p = Pattern.compile("\\s*", Pattern.DOTALL);
		return p.matcher(input).replaceAll("");
	}
	
	private void assertIgnoreWhitespace(String expected, String actual) {
		assertEquals(removeWhiteSpaces(expected).toLowerCase(), removeWhiteSpaces(actual).toLowerCase());
	}
}
