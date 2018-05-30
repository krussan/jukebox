package se.qxx.android.jukebox.adapters.viewmode;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;

public class JukeboxFragmentAdapter extends FragmentStatePagerAdapter {

	private ViewMode mode = ViewMode.Movie;
    private Context context;

	private ViewMode getMode() {
		return mode;
	}

    public void setMode(ViewMode mode) {
        this.mode = mode;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JukeboxFragmentAdapter(FragmentManager fm, ViewMode mode, Context context) {
		super(fm);
		this.setMode(mode);
		this.setContext(context);
	}

	@Override
	public int getCount() {
		return 2;
	}

	@Override
	public Fragment getItem(int position) {
		return JukeboxFragment.newInstance(position, this.getMode());
	}

    @Override
    public CharSequence getPageTitle(int position) {
	    if (this.getContext() != null) {
            if (position == 0)
                return this.getContext().getResources().getString(R.string.tabMovies);
            else {
                if (this.getMode() == ViewMode.Season)
                    return this.getContext().getResources().getString(R.string.tabSeason);
                else
                    return this.getContext().getResources().getString(R.string.tabSeries);
            }
        }

        return StringUtils.EMPTY;
    }
}
