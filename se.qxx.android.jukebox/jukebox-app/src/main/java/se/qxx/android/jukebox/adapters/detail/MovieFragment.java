package se.qxx.android.jukebox.adapters.detail;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.MovieMediaLayoutAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.protobuf.InvalidProtocolBufferException;

public class MovieFragment extends ListFragment {
	public static MovieFragment newInstance(Movie m) {
		Bundle b = new Bundle();
		MovieFragment mf = new MovieFragment();

		b.putSerializable("movie", m);
		mf.setArguments(b);
		
		return mf;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.movieitem, container, false);
		initializeView(v);
		return v;
	}
	
	private void initializeView(View v) {
		Movie m = this.getMovie();
		if (m != null) {
			if (!m.getThumbnail().isEmpty()) {
				Bitmap bm = GUITools.getBitmapFromByteArray(m.getThumbnail().toByteArray());
				Bitmap scaledImage = GUITools.scaleImage(120, bm, v.getContext());
				GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
			}

			int duration = m.getDuration();
			int hours = duration / 60;
			int minutes = duration % 60;

			GUITools.setTextOnTextview(R.id.textViewTitle, m.getTitle(), v);
			GUITools.setTextOnTextview(R.id.textViewYear, Integer.toString(m.getYear()), v);
			GUITools.setTextOnTextview(R.id.textViewStory, m.getStory(), v);
			GUITools.setTextOnTextview(R.id.textViewGenre, String.format("Genre :: %s", StringUtils.join(m.getGenreList(), " / ")), v);
			GUITools.setTextOnTextview(R.id.textViewDirector, String.format("Director :: %s", m.getDirector()), v);
			GUITools.setTextOnTextview(R.id.textViewDuration, String.format("Duration :: %s h %s m", hours, minutes) , v);
			GUITools.setTextOnTextview(R.id.textViewRating, String.format("Rating :: %s / 10", m.getRating()), v);
			//GUITools.setTextOnTextview(R.id.textViewFilename, String.format("Filename :: %s", m.getFilename()), v);

			MovieMediaLayoutAdapter adapter = new MovieMediaLayoutAdapter(v.getContext(), m);
			ListView listView = (ListView)v.findViewById(R.id.listViewFilename);
			listView.setAdapter(adapter);

			//detector = new SimpleGestureFilter(this, this);
		}

	}

	private Movie getMovie() {
		Bundle b = getArguments();

		if (b!= null) {
			return (Movie)b.getSerializable("movie");
		}

		return null;
	}
	
}
