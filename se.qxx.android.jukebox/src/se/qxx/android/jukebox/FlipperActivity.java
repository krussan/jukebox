package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MovieFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Toast;

public class FlipperActivity extends FragmentActivity implements OnPageChangeListener, OnLongClickListener {
	ViewPager pager;

	protected View getRootView() {
		return findViewById(R.id.rootViewPager);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.itemwrapper);
        pager = (ViewPager)this.getRootView();
        
        MovieFragmentAdapter mfa = new MovieFragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(mfa);
        
        pager.setCurrentItem(Model.get().getCurrentMovieIndex());    
        pager.setOnPageChangeListener(this);
        
        this.getRootView().setOnLongClickListener(this);
    }
        	
	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, this);
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(this, NowPlayingActivity.class);
				startActivity(iPlay);
				break;	
			case R.id.btnViewInfo:
				String url = Model.get().getCurrentMovie().getImdbUrl();
				if (url != null && url.length() > 0) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
				else {
					Toast.makeText(this, "No IMDB link available", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {	
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		Model.get().setCurrentMovie(arg0);
	}

	@Override
	public boolean onLongClick(View v) {
		ActionDialog d = new ActionDialog(this, Model.get().getCurrentMovie());
		d.show();
		return false;
	}
}
