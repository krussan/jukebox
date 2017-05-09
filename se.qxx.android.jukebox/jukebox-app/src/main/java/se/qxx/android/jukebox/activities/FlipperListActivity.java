package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.ChromeCastConfiguration;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

public class FlipperListActivity extends AppCompatActivity {
	ViewPager pager;

	protected View getRootView() {
		return findViewById(R.id.rootJukeboxViewPager);
	}
		
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		JukeboxSettings.init(this);
		ChromeCastConfiguration.initialize(this);

		setContentView(R.layout.jukebox_main_wrapper);
        pager = (ViewPager)this.getRootView();

        String mode = "main";
        if (getIntent() != null && getIntent().getExtras() != null)
            mode = getIntent().getExtras().getString("mode", "main");

        JukeboxFragmentAdapter mfa = new JukeboxFragmentAdapter(getSupportFragmentManager(), mode);

        pager.setAdapter(mfa);
        pager.setCurrentItem(Model.get().getCurrentMovieIndex());
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(getMenuInflater(), menu);

		return true;
	}

	@Override
	public void onResume() {
		super.onResume();

		ChromeCastConfiguration.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		ChromeCastConfiguration.onPause();
	}

	public void onButtonClicked(View v) {
		int id = v.getId();
		GUITools.vibrate(28, v.getContext());
		
		switch (id) {
			case R.id.btnPlay:
				Intent iPlay = new Intent(v.getContext(), NowPlayingActivity.class);
				iPlay.putExtra("mode", "main");
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
