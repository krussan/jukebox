package se.qxx.android.jukebox.adapters.viewmode;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.SparseArray;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import org.apache.commons.lang3.StringUtils;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.ViewMode;

public class JukeboxFragmentAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public JukeboxFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
		this.setContext(context);
	}

    private static ViewMode getViewMode(int position) {
        // position 0 in horizontal scroll is movie
        // position 0 is series OR season

        if (position == 0)
            return ViewMode.Movie;
        else
            return ViewMode.Series;
    }

    @Override
	public int getCount() {
        return 2;
	}

	@Override
	public Fragment getItem(int position) {
		return JukeboxFragment.newInstance(getViewMode(position));
	}

    @Override
    public CharSequence getPageTitle(int position) {
	    if (this.getContext() != null) {
            if (position == 0)
                return this.getContext().getResources().getString(R.string.tabMovies);
            else
                return this.getContext().getResources().getString(R.string.tabSeries);
        }

        return StringUtils.EMPTY;
    }

    @Override
    public Parcelable saveState() {
        Bundle bundle = (Bundle) super.saveState();
        bundle.putParcelableArray("states", null); // Never maintain any states from the base class, just null it out
        return bundle;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }


}

