package se.qxx.android.jukebox;

import se.qxx.android.jukebox.adapters.MovieFragmentAdapter;
import se.qxx.android.jukebox.model.Model;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;

public class FlipperListActivity extends FragmentActivity 
	implements OnPageChangeListener {
	ViewPager pager;

	protected View getRootView() {
		return findViewById(R.id.rootViewPager);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		JukeboxSettings.init(this);

        pager = (ViewPager)this.getRootView();
        
        MovieFragmentAdapter mfa = new MovieFragmentAdapter(getSupportFragmentManager());
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


}
