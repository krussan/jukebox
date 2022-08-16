package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.R;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MovieMediaLayoutAdapter extends BaseAdapter {

	private Context context;
	private Movie movie;

	public MovieMediaLayoutAdapter(Context context, Movie movie) {
		super();
		this.setMovie(movie);
		this.context = context;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}



	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView; 

		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            v = vi.inflate(R.layout.medialistrow, null);
	        }

			Media md = (Media)this.getItem(position);
	        if (md != null) 
	        	GUITools.setTextOnTextview(R.id.txtFilename, String.format("Filename :: %s", md.getFilename()), v);
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating media list", e);
		}
			
        return v;
	}

	@Override
	public int getCount() {
		return this.movie.getMediaCount();
	}

	@Override
	public Object getItem(int position) {
		return this.movie.getMedia(position);
	}

	@Override
	public long getItemId(int position) {
		return this.movie.getMedia(position).getID();
	}

}
	