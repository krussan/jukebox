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
	private int index;
	
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

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getRatingIndex() {
		switch (this.getRating()) {
		case ExactMatch:
			return 1;
		case PositiveMatch:
			return 2;
		case ProbableMatch:
			return 3;
		case NotMatched:
			return 4;
		case SubsExist:
			return 0;
		}
		
		return 99;
	}
	
	@Override
	public int compareTo(SubFile that) {
		return this.getRatingIndex() - that.getRatingIndex();
	}

}
