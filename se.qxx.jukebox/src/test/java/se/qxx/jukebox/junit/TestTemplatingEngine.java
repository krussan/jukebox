package se.qxx.jukebox.junit;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import freemarker.template.TemplateException;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.webserver.TemplateEngine;

public class TestTemplatingEngine {

	@Test
	public void test() throws TemplateException, IOException {
		List<Movie> list = new ArrayList<Movie>();
		
		list.add(Movie.newBuilder()
				.setID(1)
				.setTitle("Movie 1")
				.setIdentifiedTitle("Movie 1")
				.build());
		
		list.add(Movie.newBuilder()
				.setID(2)
				.setTitle("Movie 2")
				.setIdentifiedTitle("Movie 2")
				.build());
		
		String actual = TemplateEngine.get().listMovies(list);
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("	<head></head>");
		sb.append("	<body>");
		sb.append("		<h1>Jukebox repository<h1>);");
		sb.append("		<table>");
		sb.append("			<tr><td><a href=\"movie1.html\">Movie 1</a></tr></td>");
		sb.append("			<tr><td><a href=\"movie2.html\">Movie 2</a></tr></td>");
		sb.append("		</table>");
		sb.append("	</body>");
		sb.append("</html>");
		
		assertEquals(removeWhiteSpaces(sb.toString()).toLowerCase(), removeWhiteSpaces(actual).toLowerCase());
	}

	private String removeWhiteSpaces(String input) {
		return input.replace("\\s*", "");
	}
}
