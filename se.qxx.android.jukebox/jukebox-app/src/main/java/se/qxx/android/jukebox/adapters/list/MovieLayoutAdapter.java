package se.qxx.android.jukebox.adapters.list;

import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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

            // If all media has a meta duration then hide the download icon
            boolean downloadFinished = true;
            for (Media md : m.getMediaList()) {
                if (md.getMetaDuration() == 0)
                    downloadFinished = false;
            }
            if (downloadFinished)
                GUITools.hideView(R.id.imgDownloading, v);

            if (m.getThumbnail().isEmpty()) {
                GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
            }
            else {
                Bitmap image = GUITools.getBitmapFromByteArray(m.getThumbnail().toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
                GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
            }

            List<Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(m.getMedia(0).getSubsList());
            if (sortedSubtitles.size() > 0)
                IncludeSubtitleRating.initialize(sortedSubtitles.get(0), v);

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
	