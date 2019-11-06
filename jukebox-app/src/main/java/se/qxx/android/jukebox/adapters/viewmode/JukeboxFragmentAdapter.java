package se.qxx.android.jukebox.adapters.viewmode;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import org.apache.commons.lang3.StringUtils;
import se.qxx.android.jukebox.R;

public class JukeboxFragmentAdapter extends FragmentStatePagerAdapter {

    private Context context;

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

	@Override
	public int getCount() {
        return 2;
	}

	@Override
	public Fragment getItem(int position) {
		return JukeboxFragment.newInstance(position);
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

}
