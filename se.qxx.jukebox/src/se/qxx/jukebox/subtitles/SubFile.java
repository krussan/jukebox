package se.qxx.jukebox.subtitles;

import java.io.File;

public class SubFile {

	public enum Rating {
		NotMatched,
		ProbableMatch,
		PositiveMatch,
		ExactMatch
	}
	private File _file;
	private Rating _rating = Rating.NotMatched;
	private String _description;
	
	public SubFile(File f) {
		this._file = f;
	}
	
	public void setRating(Rating rating) {
		this._rating = rating;
	}
	
	public Rating getRating() { return _rating;	}
	
	public File getFile() { return this._file; }
	
	public String getDescription() {
		return this._description;
	}
	
	public void setDescription(String descr) {
		this._description = descr;
	}
}
