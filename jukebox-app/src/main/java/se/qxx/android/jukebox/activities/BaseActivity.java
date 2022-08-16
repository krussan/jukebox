package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.cast.framework.CastContext;
import org.apache.commons.lang3.StringUtils;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.SearchFragment;
import se.qxx.android.jukebox.activities.fragments.JukeboxFragment;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.jukebox.domain.JukeboxDomain;

public abstract class BaseActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener  {

    private SearchFragment searchFragment;
    private boolean searchVisible = false;
    private CountDownTimer cntr;
    private static final int WAITING_TIME = 200;
    private static final int COUNTDOWN_TIMER = 500;
    private CastContext mCastContext;

    @IdRes
    protected abstract int getSearchContainer();

    public boolean isSearchVisible() {
        return searchVisible;
    }

    public void setSearchVisible(boolean searchVisible) {
        this.searchVisible = searchVisible;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChromeCastConfiguration.checkGooglePlayServices(this);
        mCastContext = CastContext.getSharedInstance(this);
    }

    @Override
    public boolean onClose() {
        this.getSupportFragmentManager().popBackStack();
        this.setSearchVisible(false);
        searchFragment = null;
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, this, this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.prefs_menu_item) {
            Intent intentPreferences = new Intent(this, JukeboxPreferenceActivity.class);
            startActivity(intentPreferences);
        }
        return true;
    }


    //TODO: Should overlay the current fragment with a new search fragment
    // that shows both series and movies that matches the query
    // This need to be duplicated in the JukeboxFragment as well
    @Override
    public boolean onQueryTextSubmit(String query) {
        return search(query);
    }

    @Override
    public boolean onQueryTextChange(final String newText) {
        if(cntr != null){
            cntr.cancel();
        }
        cntr = new CountDownTimer(WAITING_TIME, COUNTDOWN_TIMER) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {
                int length = newText.length();

                if (length > 2) {
                    search(newText);
                }
                else if (length == 0) {
                    search(StringUtils.EMPTY);
                }
            }
        };
        cntr.start();
        return false;
    }

    private boolean search(String query) {
        if (!this.isSearchVisible() || searchFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

            searchFragment = SearchFragment.newInstance();
            ft.replace(this.getSearchContainer(), searchFragment);
            ft.addToBackStack(null);
            ft.commit();

            this.setSearchVisible(true);
        }

        searchFragment.search(query);

        return true;
    }
}
