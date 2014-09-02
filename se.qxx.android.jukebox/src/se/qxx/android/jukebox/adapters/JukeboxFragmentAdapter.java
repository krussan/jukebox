package se.qxx.android.jukebox.adapters;

import se.qxx.android.jukebox.model.Model;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class JukeboxFragmentAdapter extends FragmentStatePagerAdapter {

	public JukeboxFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public Fragment getItem(int position) {
		return JukeboxFragment.newInstance(position);
	}
	
}
