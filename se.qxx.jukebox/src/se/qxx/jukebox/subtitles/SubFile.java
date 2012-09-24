package se.qxx.jukebox.subtitles;

import java.io.File;

public class SubFile implements Comparable<SubFile> {

	public enum Rating {
		NotMatched,
		ProbableMatch,
		PositiveMatch,
		ExactMatch,
		SubsExist
	}
	private String _url;
	private File _file;
	private Rating _rating = Rating.NotMatched;
	private String _description;
	
	public SubFile(String url, String description) {
		this._url = url;
		this._description = description;
	}
	
	public SubFile(File f) {
		this._file = f;
	}
	
	public void setRating(Rating rating) {
		this._rating = rating;
	}
	
	public Rating getRating() { return _rating;	}
	
	public File getFile() { return this._file; }
	public void setFile(File f) {this._file = f;}
	
	public String getDescription() {
		return this._description;
	}
	
	public void setDescription(String descr) {
		this._description = descr;
	}

	public String getUrl() {
		return _url;
	}

	public void setUrl(String _url) {
		this._url = _url;
	}

	@Override
	public int compareTo(SubFile that) {
		if (this._rating == Rating.ExactMatch) return -1;
		if (that._rating == Rating.ExactMatch) return 1;
		
		if (this._rating == Rating.PositiveMatch && (that._rating == Rating.ProbableMatch || that._rating == Rating.NotMatched))
			return -1;
		if (that._rating == Rating.PositiveMatch && (this._rating == Rating.ProbableMatch || this._rating == Rating.NotMatched))
			return 1;
		if (this._rating == Rating.ProbableMatch && that._rating == Rating.NotMatched)
			return -1;
		if (that._rating == Rating.ProbableMatch && this._rating == Rating.NotMatched)
			return 1;
		
		return this._description.compareTo(that._description);
	}
}
