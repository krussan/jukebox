package se.qxx.android.jukebox.adapters.viewmode;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class JukeboxFragmentAdapter extends FragmentStatePagerAdapter {

	private String _mode = "Main";
	private String getMode() {
		return _mode;
	}

	public JukeboxFragmentAdapter(FragmentManager fm, String mode) {
		super(fm);
		this._mode = mode;
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Fragment getItem(int position) {
		return JukeboxFragment.newInstance(position, this.getMode());
	}
	
}
