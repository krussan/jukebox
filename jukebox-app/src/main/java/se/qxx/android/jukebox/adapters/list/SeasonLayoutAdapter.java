package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class SeasonLayoutAdapter extends GenericListLayoutAdapter<Season> {

	private List<Season> seasons = new ArrayList<>();

    public List<Season> getSeasons() {
        return seasons;
    }

    public void addSeasons(List<Season> seasons) {
        this.getSeasons().addAll(seasons);
    }

    public void clearSeasons() {
        this.getSeasons().clear();
    }

	public SeasonLayoutAdapter(Context context, Series series) {
		super(context, R.layout.movielistrow);
		this.clearSeasons();
		this.addSeasons(series.getSeasonList());
	}

    public SeasonLayoutAdapter(Context context, List<Season> seasons) {
        super(context, R.layout.movielistrow);
        this.clearSeasons();
        this.addSeasons(seasons);
    }

	@Override
	public void initializeView(View v, Season ss) {
		try {
			if (ss != null) {
				GUITools.setTextOnTextview(R.id.toptext, String.format("Season %s - %s", ss.getSeasonNumber(), ss.getTitle()), v);

				setYear(v, ss);
				hideDownloadAndCompletedIcons(v);
				setupThumbnail(v, ss.getThumbnail());
				setupSubtitles(v, new ArrayList<>());
			}
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
	}

	private void setYear(View v, Season ss) {
		int year = ss.getYear();
		if (year > 0)
            GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(year), v);
        else
            GUITools.hideView(R.id.bottomtext, v);
	}

	@Override
	public int getItemCount() {
		return this.getSeasons().size();
	}

	@Override
	public Season getDataObject(int position) {
		return this.getSeasons().get(position);
	}

	@Override
	public long getObjectId(int position) {
		return this.getDataObject(position).getID();
	}

}
	