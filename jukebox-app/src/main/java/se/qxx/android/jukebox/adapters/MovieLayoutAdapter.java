package se.qxx.android.jukebox.adapters;

import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.R;
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
            if (m.getMediaCount() > 0) {
                setupSubtitles(v, m.getMedia(0).getSubsList());
                this.setupProgressBar(v, m.getDuration(), m.getMedia(0).getID());
            }
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
        return getDataObject(position).getID();
    }

    public int getMediaId(int position) {
        if (this.getItem(position).getMediaCount() > 0)
            return this.getItem(position).getMedia(0).getID();
        else
            return 0;
    }

    public JukeboxDomain.Media getMedia(int position) {
        if (this.getItem(position).getMediaCount() > 0)
            return this.getItem(position).getMedia(0);
        else
            return null;
    }

}
	