package se.qxx.jukebox.builders;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.domain.JukeboxDomain.Episode;
import se.qxx.jukebox.domain.JukeboxDomain.Identifier;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;
import se.qxx.jukebox.settings.parser.ParserType;

public class ParserMovie {
	private String filename;
	private String movieName = StringUtils.EMPTY;
	private List<String> titles 		= new ArrayList<String>();
	private List<String> types  		= new ArrayList<String>();
	private List<String> formats		= new ArrayList<String>();
	private List<String> languages		= new ArrayList<String>();
	private List<String> others			= new ArrayList<String>();
	private List<String> sounds			= new ArrayList<String>();
	private int episode;
	private int season;
	private int year;
	private String groupName = StringUtils.EMPTY;
	
	public ParserMovie() {
	}
	
	public String getMovieName() {
		return movieName;
	}
	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}
	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public List<String> getTypes() {
		return types;
	}
	public void setTypes(List<String> types) {
		this.types = types;
	}
	public List<String> getFormats() {
		return formats;
	}
	public void setFormats(List<String> formats) {
		this.formats = formats;
	}
	public List<String> getLanguages() {
		return languages;
	}
	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}
	public List<String> getOthers() {
		return others;
	}
	public void setOthers(List<String> others) {
		this.others = others;
	}
	public List<String> getSounds() {
		return sounds;
	}
	public void setSounds(List<String> sounds) {
		this.sounds = sounds;
	}
	
	public void addMovieNameToken(String token) {
		this.setMovieName(String.format("%s %s", this.getMovieName(), token));
	}
	public List<String> getTitles() {
		return titles;
	}
	public void setTitles(List<String> titles) {
		this.titles = titles;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public void pushTitle() {
		if (!StringUtils.isEmpty(this.getMovieName())) {
			this.getTitles().add(StringUtils.trim(this.getMovieName()));
			this.setMovieName(StringUtils.EMPTY);
		}
	}
	
	
	public String toString() {
		String output = this.getFilename() + "\n";
		
		output +=
				"--- NAME\n";		
		output += "------ " + this.getMovieName() + "\n";
		
		
		output +=
				"--- TITLES\n";
		
		for (String item : this.getTitles())
			output += "------ " + item + "\n";
		
		output +="\n";
		output += "--- YEAR :: " + Integer.toString(this.getYear()) + "\n";
		output += "--- SEASON :: " + Integer.toString(this.getSeason()) + "\n";
		output += "--- EPISODE :: " + Integer.toString(this.getEpisode()) + "\n";
		output += "--- TYPES\n";

		for (String item : this.getTypes())
			output += "------ " + item + "\n";
		
		output += "--- FORMATS\n";
		
		for (String item : this.getFormats())
			output += "------ " + item + "\n";
		
		output += "--- LANGUAGES\n";
		
		for (String item : this.getLanguages())
			output += "------ " + item + "\n";
		
		output += "--- SOUNDS\n";
		
		for (String item : this.getSounds())
			output += "------ " + item + "\n";
		
		output += "--- OTHERS\n";		

		for (String item : this.getOthers())
			output += "------ " + item + "\n";

		output +=
				"--- GROUP\n";		
		output += "------ " + this.getGroupName() + "\n";

		output +="\n";
		output +="----------------------------------------------------\n";
		output +="----------------------------------------------------\n";
		
		return output;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public Movie getMovie() {
		Movie.Builder b = Movie.newBuilder();
			
		b.setID(-1)
		 .setGroupName(this.getGroupName())
		 .setTitle(this.getMovieName())
		 .setYear(this.getYear())
		 .setIdentifier(Identifier.Parser)
		 .setIsTvEpisode(this.getSeason() > 0 || this.getEpisode() > 0)
		 .setIdentifierRating(this.getIdentifierRating())
		 .setSubtitleRetreiveResult(0);

		if (this.getSounds().size() > 0)
			b.setSound(this.getSounds().get(0));
		
		if (this.getTypes().size() > 0)
			b.setType(this.getTypes().get(0));
		
		if (this.getFormats().size() > 0)
			b.setFormat(this.getFormats().get(0));
		 
		if (this.getLanguages().size() > 0)
			b.setLanguage(this.getLanguages().get(0));
		
		
		return b.build();		
	}
	
	private int getIdentifierRating() {
		int groupsMatched = 0;
		
		if (!StringUtils.isEmpty(movieName))
			groupsMatched++;
		
		if (this.getTitles().size() > 0)
			groupsMatched++;
		
		if (this.getTypes().size() > 0)
			groupsMatched++;
		
		if (this.getFormats().size() > 0)
			groupsMatched++;

		if (this.getLanguages().size() > 0)
			groupsMatched++;
		
		if (this.getSounds().size() > 0)
			groupsMatched++;

		if (!StringUtils.isEmpty(groupName))
			groupsMatched++;
		
		return Math.round(100 * groupsMatched / 7);
	}
}
