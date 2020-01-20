package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.NowPlayingActivity;
import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Episode;

public class EpisodeLayoutAdapter extends GenericListLayoutAdapter<Episode> {

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
            String label = String.format("E%s - %s",
                    StringUtils.leftPad(Integer.toString(ep.getEpisodeNumber()), 2, '0'),
                    ep.getTitle());

            GUITools.setTextOnTextview(R.id.toptext, label, v);
            GUITools.setTextOnTextview(R.id.txtDescription, ep.getStory(), v);

            setupDownloadedAndCompletedIcons(v, ep.getMediaList());
            setupThumbnail(v, ep.getThumbnail());

            if (ep.getMediaCount() > 0) {
                setupSubtitles(v, ep.getMedia(0).getSubsList());
                this.setupProgressBar(v, ep.getDuration(), ep.getMedia(0).getID());
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

        Collections.sort(newList, Comparator.comparingInt(Episode::getEpisodeNumber));

        return newList;
    }

}
	