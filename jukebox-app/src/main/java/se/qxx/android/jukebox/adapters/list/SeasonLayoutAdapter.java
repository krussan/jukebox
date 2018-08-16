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
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

public class SeasonLayoutAdapter extends GenericListLayoutAdapter<Season> {

	private Context context;
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
		this.context = context;
		this.clearSeasons();
		this.addSeasons(series.getSeasonList());
	}

    public SeasonLayoutAdapter(Context context, List<Season> seasons) {
        super(context, R.layout.movielistrow);
        this.context = context;
        this.clearSeasons();
        this.addSeasons(seasons);
    }

	@Override
	public void initializeView(View v, Season o) {
		try {

			if (v == null) {
				LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.movielistrow, null);
			}
			Context context = v.getContext();

			if (o != null) {
				GUITools.setTextOnTextview(R.id.toptext, String.format("Season %s - %s", o.getSeasonNumber(), o.getTitle()), v);
				GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(o.getYear()), v);
//	        	GUITools.setTextOnTextview(R.id.txtRating, m.getRating(), v);

				// If all media has a meta duration then hide the download icon
				GUITools.hideView(R.id.imgDownloading, v);

				if (o.getThumbnail().isEmpty()) {
					GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
				}
				else {
					Bitmap image = GUITools.getBitmapFromByteArray(o.getThumbnail().toByteArray());
					Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
					GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
				}
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

}
	