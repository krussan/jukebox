package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

/***
 * Responsible of the list view showing all movies
 */
public class MovieLayoutAdapter extends GenericListLayoutAdapter<Movie> {

    private List<Movie> movies = new ArrayList<>();
    public List<Movie> getMovies() {
        return movies;
    }
    private CacheData cacheData;

    public void addMovies(List<Movie> movies) {
        this.getMovies().addAll(movies);
    }

    public void clearMovies() {
        this.getMovies().clear();
    }

	public MovieLayoutAdapter(Context context, List<Movie> movies) {
		super(context, R.layout.movielistrow);
		this.clearMovies();
		this.addMovies(movies);
        cacheData = new CacheData(context);

    }

	@Override
	public void initializeView(View v, Movie m) {
		if (m != null) {
            GUITools.setTextOnTextview(R.id.toptext, m.getTitle(), v);
            GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(m.getYear()), v);
            GUITools.setTextOnTextview(R.id.txtRating, m.getRating(), v);
            GUITools.hideView(R.id.txtListTitle, v);

            setupDownloadedAndCompletedIcons(v, m.getMediaList());
            setupThumbnail(v, m.getThumbnail());
            setupSubtitles(v, m.getMedia(0).getSubsList());

            int duration = m.getDuration();
            ProgressBar progressWatched = v.findViewById(R.id.progressWatched);
            int progress = 0;
            if (duration > 0) {
                progress = (int)(100f * (float)cacheData.getMediaState(m.getMedia(0).getID()) / (float)(duration * 60));
            }

            if (progress > 0)
                progressWatched.setProgress(progress);
            else
                progressWatched.setVisibility(View.INVISIBLE);

        }
	}

    private void setYear(View v, JukeboxDomain.Movie m) {
        int year = m.getYear();
        if (year > 0)
            GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(year), v);
        else
            GUITools.hideView(R.id.bottomtext, v);
    }


    @Override
    public int getItemCount() {
        return this.getMovies().size();
    }

    @Override
    public Movie getDataObject(int position) {
        return this.getMovies().get(position);
    }

    @Override
    public long getObjectId(int position) {
        return ((Movie)getDataObject(position)).getID();
    }

    public int getMediaId(int position) {
        if (this.getItem(position).getMediaCount() > 0)
            return this.getItem(position).getMedia(0).getID();
        else
            return 0;
    }

}
	