package se.qxx.android.jukebox.adapters.support;

import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class SubtitleLayoutAdapter extends BaseAdapter {

	private Context context;
	private List<Subtitle> sortedSubtitles = null;

	public SubtitleLayoutAdapter(Context context, List<Subtitle> subtitles) {
		super();
		this.context = context;
		this.sortedSubtitles = Sorter.sortSubtitlesByRating(subtitles);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		Model.get().getSubtitles();

		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.subtitleitem, null);
	        }
	        Subtitle sub = (Subtitle)this.getItem(position);
	        
	        IncludeSubtitleRating.initialize(sub, v);
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating subtitle list", e);
		}
			
        return v;
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

}
	