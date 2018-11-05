package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.NowPlayingActivity;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;

public class EpisodeLayoutAdapter extends GenericListLayoutAdapter<Episode> implements View.OnClickListener {

    private List<Episode> episodes = new ArrayList<>();
    private int seasonNumber;

    public List<Episode> getEpisodes() {
		return episodes;
	}

	public void addEpisodes(List<Episode> episodes) {
        this.getEpisodes().addAll(
            sortEpisodes(episodes));
	}

	public void clearEpisodes() {
        this.getEpisodes().clear();
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public EpisodeLayoutAdapter(Context context, int seasonNumber, List<Episode> episodes) {
		super(context, R.layout.episodelistrow);

		this.setSeasonNumber(seasonNumber);
		this.clearEpisodes();
		this.addEpisodes(sortEpisodes(episodes));
	}

    @Override
    public void initializeView(View v, Episode ep) {
        if (ep != null) {
            GUITools.setTextOnTextview(R.id.toptext, String.format("S%sE%s - %s", this.getSeasonNumber(), ep.getEpisodeNumber(), ep.getTitle()), v);
            GUITools.setTextOnTextview(R.id.txtDescription, ep.getStory(), v);

            setupDownloadedAndCompletedIcons(v, ep.getMediaList());
            setupThumbnail(v, ep.getThumbnail());
            setupSubtitles(v, ep.getMedia(0).getSubsList());

            ImageButton btnPlayEpisode = v.findViewById(R.id.btnPlayEpisode);
            btnPlayEpisode.setTag(ep.getID());
            btnPlayEpisode.setOnClickListener(this);

        }

    }

    @Override
	public void onClick(View view) {

        int episodeId = (int) view.getTag();
        Episode e = getItemById(episodeId);

        if (e != null) {
            switch (view.getId()) {
                case R.id.btnPlayEpisode:
                    Intent iPlay = new Intent(this.getContext(), NowPlayingActivity.class);
                    iPlay.putExtra("mode", ViewMode.Episode);
                    iPlay.putExtra("episode", e);
                    iPlay.putExtra("seasonNumber", getSeasonNumber());

                    this.getContext().startActivity(iPlay);
                    break;
            }
        }
	}


    @Override
    public int getItemCount() {
        return this.getEpisodes().size();
    }

    @Override
    public JukeboxDomain.Episode getDataObject(int position) {
        return this.getEpisodes().get(position);
    }

    @Override
    public long getObjectId(int position) {
        return this.getDataObject(position).getID();
    }

    private Episode getItemById(int id) {
        for (Episode e : this.getEpisodes()) {
            if (e.getID() == id)
                return e;
        }
        return null;
    }

    private List<JukeboxDomain.Episode> sortEpisodes(List<JukeboxDomain.Episode> episodes) {
        List<JukeboxDomain.Episode> newList = new ArrayList<>(episodes);

        Collections.sort(newList, (x, y) -> Integer.compare(x.getEpisodeNumber(), y.getEpisodeNumber()));

        return newList;
    }



}
	