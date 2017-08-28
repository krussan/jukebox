package se.qxx.android.jukebox.model;

import java.util.List;

import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class ModelRatedSubtitleAdapter extends BaseAdapter {

	private Media media = null;
	private List<Subtitle> sortedSubtitles = null;
	
	public ModelRatedSubtitleAdapter(List<Subtitle> subtitles) {
		this.media = media;
		sortedSubtitles = Sorter.sortSubtitlesByRating(subtitles);
	}
	
	@Override
	public int getCount() {
		return this.sortedSubtitles.size();
	}
	
	@Override
	public Object getItem(int position) {
		return this.sortedSubtitles.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return this.sortedSubtitles.get(position).hashCode();
	}
	
	@Override
	public abstract View getView(int position, View convertView, ViewGroup parent);
}
