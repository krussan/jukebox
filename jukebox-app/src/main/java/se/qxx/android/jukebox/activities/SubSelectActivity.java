package se.qxx.android.jukebox.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.google.android.gms.cast.framework.CastContext;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.SubtitleLayoutAdapter;
import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;

public class SubSelectActivity extends AppCompatActivity  implements SubtitleLayoutAdapter.SubtitleSelectedListener{
    private CastContext mCastContext;
    private JukeboxSettings settings;

    public enum SubSelectMode {
        Stay,
        Return
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subtitlepicker);
        mCastContext = CastContext.getSharedInstance(this);
        settings = new JukeboxSettings(this);
        //initializeView();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu, settings.getCurrentMediaPlayer());

		return true;
	}


	public Media getMedia() {
        Bundle b = getIntent().getExtras();

        if (b != null) {
            return (Media)b.getSerializable("media");
        }


        return null;
    }

    public SubSelectMode getSubSelectMode() {
        Bundle b = getIntent().getExtras();
        if (b != null)
            return (SubSelectMode)b.getSerializable("subSelectMode");

        return SubSelectMode.Stay;
    }

    @Override
    public void onSubtitleSelected(JukeboxDomain.SubtitleUri subtitleUri) {
        Intent i = new Intent();
        i.putExtra("SubSelected", subtitleUri);
        setResult(RESULT_OK, i);

        if (this.getSubSelectMode() == SubSelectMode.Return)
            this.finish();
    }
}
