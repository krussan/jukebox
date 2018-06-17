package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.NowPlayingActivity;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Season;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;

public class EpisodeLayoutAdapter extends BaseAdapter implements View.OnClickListener {

	private Context context;
    private List<Episode> episodes;
    private int seasonNumber;

    public List<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(List<Episode> episodes) {
		this.episodes = episodes;
	}

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public EpisodeLayoutAdapter(Context context, int seasonNumber, List<Episode> episodes) {
		super();
		this.context = context;
		this.setSeasonNumber(seasonNumber);
		this.setEpisodes(episodes);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		try {

            if (v == null) {
                LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.episodelistrow, parent);
            }

            if (v != null) {
                Episode ep = (Episode) this.getItem(position);
                if (ep != null) {
                    GUITools.setTextOnTextview(R.id.toptext, String.format("S%sE%s - %s", this.getSeasonNumber(), ep.getEpisodeNumber(), ep.getTitle()), v);
                    GUITools.setTextOnTextview(R.id.txtDescription, ep.getStory(), v);

                    if (ep.getThumbnail().isEmpty()) {
                        GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
                    } else {
                        Bitmap image = GUITools.getBitmapFromByteArray(ep.getThumbnail().toByteArray());
                        Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
                        GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
                    }

                    ImageButton btnPlayEpisode = v.findViewById(R.id.btnPlayEpisode);
                    btnPlayEpisode.setTag(position);
                    btnPlayEpisode.setOnClickListener(this);

                }
            }
        }
        catch(Exception e){
            Logger.Log().e("Error occured while populating list", e);
        }


        return v;
	}

	@Override
	public void onClick(View view) {

        int position = (int) view.getTag();

		switch (view.getId()) {
			case R.id.btnPlayEpisode:
				Intent iPlay = new Intent(this.context, NowPlayingActivity.class);
                iPlay.putExtra("mode", ViewMode.Episode);
                iPlay.putExtra("episode", getEpisodes().get(position));

				context.startActivity(iPlay);
				break;
		}
	}


	@Override
	public int getCount() {
		return this.getEpisodes().size();
	}

	@Override
	public Object getItem(int position) {
		return this.getEpisodes().get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}



}
	