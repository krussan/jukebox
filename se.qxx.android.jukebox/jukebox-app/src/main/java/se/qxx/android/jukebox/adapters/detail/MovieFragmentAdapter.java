package se.qxx.android.jukebox.adapters.detail;

import se.qxx.android.jukebox.model.Model;
import se.qxx.jukebox.domain.JukeboxDomain;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/***
 * Responsible of the detailed movie view
 */
public class MovieFragmentAdapter extends FragmentStatePagerAdapter {

    private List<JukeboxDomain.Movie> movies = new ArrayList<>();

    public List<JukeboxDomain.Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<JukeboxDomain.Movie> movies) {
        this.movies = movies;
    }

	public MovieFragmentAdapter(FragmentManager fm, List<JukeboxDomain.Movie> movieList) {
		super(fm);
		this.setMovies(movieList);
	}

    @Override
    public int getCount() {
        return movies.size();
    }

    @Override
    public Fragment getItem(int position) {
        return MovieFragment.newInstance(this.getMovies().get(position));
    }


}
