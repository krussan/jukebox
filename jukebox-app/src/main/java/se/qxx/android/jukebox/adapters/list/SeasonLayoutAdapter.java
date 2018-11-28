package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
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
        this.getSeasons().addAll(
			sortSeasons(seasons));
    }

    public void clearSeasons() {
        this.getSeasons().clear();
    }

    public SeasonLayoutAdapter(Context context, List<Season> seasons) {
        super(context, R.layout.movielistrow);
        this.clearSeasons();
        this.addSeasons(sortSeasons(seasons));
    }

	@Override
	public void initializeView(View v, Season ss) {
		try {
			if (ss != null) {
			    String label = String.format("Season %s%s",
                        ss.getSeasonNumber(),
                        StringUtils.isEmpty(ss.getTitle()) ? "" : " - " + ss.getTitle());

				GUITools.setTextOnTextview(R.id.toptext, label, v);
				GUITools.setTextOnTextview(R.id.bottomtext, ss.getYear() > 0 ? Integer.toString(ss.getYear()) : StringUtils.EMPTY, v);
				GUITools.setTextOnTextview(R.id.txtRating, StringUtils.EMPTY, v);

				hideDownloadAndCompletedIcons(v);
				setupThumbnail(v, ss.getThumbnail());
				setupSubtitles(v, new ArrayList<>());
			}
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
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

	private List<JukeboxDomain.Season> sortSeasons(List<JukeboxDomain.Season> seasons) {
		List<JukeboxDomain.Season> newList = new ArrayList<>(seasons);

		Collections.sort(newList, (x, y) -> Integer.compare(x.getSeasonNumber(), y.getSeasonNumber()));

		return newList;
	}

}
	