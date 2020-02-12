package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.view.View;
import org.apache.commons.lang3.StringUtils;
import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.MovieOrSeries;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/***
 * Responsible of the list view showing all movies
 */
public class MosLayoutAdapter extends GenericListLayoutAdapter<MovieOrSeries> {

    private List<MovieOrSeries> movieOrSeries = new ArrayList<>();
    public List<MovieOrSeries> getMovieOrSeries() {
        return movieOrSeries;
    }

    public void addMovieOrSeries(List<MovieOrSeries> movieOrSeries) {
        this.getMovieOrSeries().addAll(movieOrSeries);
    }

    public void clearMovieOrSeries() {
        this.getMovieOrSeries().clear();
    }

	public MosLayoutAdapter(Context context, List<MovieOrSeries> movieOrSeries) {
		super(context, R.layout.movielistrow);
		this.clearMovieOrSeries();
		this.addMovieOrSeries(movieOrSeries);
    }

	@Override
	public void initializeView(View v, MovieOrSeries mos) {
		if (mos != null) {
            GUITools.setTextOnTextview(R.id.toptext, mos.getTitle(), v);
            GUITools.setTextOnTextview(R.id.bottomtext, mos.getYear() > 0 ? Integer.toString(mos.getYear()) : StringUtils.EMPTY, v);
            GUITools.setTextOnTextview(R.id.txtRating, mos.getRating(), v);
            GUITools.hideView(R.id.txtListTitle, v);

            setupDownloadedAndCompletedIcons(v, mos.getMediaList());
            setupThumbnail(v, mos.getThumbnail());
		}
	}

    @Override
    public int getItemCount() {
        return this.getMovieOrSeries().size();
    }

    @Override
    public MovieOrSeries getDataObject(int position) {
        return this.getMovieOrSeries().get(position);
    }

    @Override
    public long getObjectId(int position) {
        return getDataObject(position).getID();
    }

    public void addMovies(List<Movie> movies) {
        this.getMovieOrSeries().addAll(movies.stream().map(MovieOrSeries::new).collect(Collectors.toList()));
    }

    public void addSeries(List<JukeboxDomain.Series> series) {
        this.getMovieOrSeries().addAll(series.stream().map(MovieOrSeries::new).collect(Collectors.toList()));
    }
}
	