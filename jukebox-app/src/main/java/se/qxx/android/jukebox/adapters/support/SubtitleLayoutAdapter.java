package se.qxx.android.jukebox.adapters.support;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.google.android.gms.cast.framework.media.RemoteMediaClient;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;

public class SubtitleLayoutAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

	private Context context;
	private int mediaId;
	private List<Subtitle> sortedSubtitles = null;
	private int selectedSubId = 0;

	public SubtitleLayoutAdapter(Context context, int mediaId, List<Subtitle> subtitles) {
		super();
		this.context = context;
		this.mediaId = mediaId;
		this.sortedSubtitles = Sorter.sortSubtitlesByRating(subtitles);

		if (this.sortedSubtitles.size() > 0)
		    this.selectedSubId = this.sortedSubtitles.get(0).getID();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.subtitleitem, null);
	        }
	        Subtitle sub = (Subtitle)this.getItem(position);

	        if (sub.getID() != this.selectedSubId)
	            v.findViewById(R.id.imgSelected).setVisibility(View.GONE);

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

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		final Subtitle sub = (Subtitle) arg0.getItemAtPosition(arg2);

		this.selectedSubId = sub.getID();

		Logger.Log().d(String.format("Setting subtitle to %s", sub.getDescription()));


		if (ChromeCastConfiguration.isChromeCastActive()) {
			RemoteMediaClient client = ChromeCastConfiguration.getRemoteMediaClient(this.context);

			if (client != null) {
				client.setActiveMediaTracks(new long[]{(long) arg2});
			}
		} else {
			final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
					JukeboxSettings.get().getServerIpAddress(),
					JukeboxSettings.get().getServerPort());

			Thread t = new Thread(() ->
					jh.setSubtitle(
							JukeboxSettings.get().getCurrentMediaPlayer(),
							this.mediaId,
							sub));
			t.run();

		}

		notifyDataSetInvalidated();
	}

}