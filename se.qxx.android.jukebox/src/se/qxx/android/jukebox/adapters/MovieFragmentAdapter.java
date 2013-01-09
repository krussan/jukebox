package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.model.Model;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MovieFragmentAdapter extends FragmentStatePagerAdapter {

	public MovieFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		return Model.get().countMovies();
	}
	
	@Override
	public Fragment getItem(int position) {
		return MovieFragment.newInstance(position);
	}
	
}
