package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Toast;

public class FlipperListActivity extends FragmentActivity 
	implements OnPageChangeListener {
	ViewPager pager;

	protected View getRootView() {
		return findViewById(R.id.rootJukeboxViewPager);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jukebox_main_wrapper);
        
		JukeboxSettings.init(this);

        pager = (ViewPager)this.getRootView();
        
        JukeboxFragmentAdapter mfa = new JukeboxFragmentAdapter(getSupportFragmentManager());
        pager.setAdapter(mfa);
        
        pager.setCurrentItem(Model.get().getCurrentMovieIndex());    
        pager.setOnPageChangeListener(this);
    }
        	

	@Override
	public void onPageScrollStateChanged(int arg0) {	
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@Override
	public void onPageSelected(int arg0) {
		//Model.get().setCurrentMovie(arg0);
	}

	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, v.getContext());
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(v.getContext(), NowPlayingActivity.class);
				startActivity(iPlay);
				break;	
			case R.id.btnViewInfo:
				String url = Model.get().getCurrentMovie().getImdbUrl();
				if (url != null && url.length() > 0) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
					startActivity(browserIntent);
				}
				else {
					Toast.makeText(v.getContext(), "No IMDB link available", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
		}
	}
	
}
