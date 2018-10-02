package se.qxx.android.jukebox.adapters.list;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

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

            setupDownloadedAndCompletedIcons(v, m.getMediaList());
            setupThumbnail(v, m.getThumbnail());
            setupSubtitles(v, m.getMedia(0).getSubsList());


        }
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


}
	