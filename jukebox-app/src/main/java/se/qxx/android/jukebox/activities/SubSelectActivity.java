package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.cast.ChromeCastConfiguration;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.adapters.support.SubtitleLayoutAdapter;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;

public class SubSelectActivity extends AppCompatActivity implements OnDismissListener {
    private CastContext mCastContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subtitlepicker);
        mCastContext = CastContext.getSharedInstance(this);

        initializeView();
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		ChromeCastConfiguration.createMenu(this, getMenuInflater(), menu);

		return true;
	}

	private void initializeView() {
	    Media md = getMedia();
	    View rootView = GUITools.getRootView(this);

	    GUITools.setTextOnTextview(R.id.lblSubpickerFilename, md.getFilename(), rootView);

	    initializeSubtitles();
	}

    protected void initializeSubtitles() {
        final JukeboxConnectionHandler jh = new JukeboxConnectionHandler(
                JukeboxSettings.get().getServerIpAddress(),
                JukeboxSettings.get().getServerPort());

        final ListView v = (ListView)findViewById(R.id.listSubtitlePicker);

        jh.listSubtitles(
            this.getMedia(),
				(response) -> {
            		runOnUiThread(() -> {
						SubtitleLayoutAdapter adapter =
							new SubtitleLayoutAdapter(this, getMedia().getID(), response.getSubtitleList());
                        v.setOnItemClickListener(adapter);
                        v.setAdapter(adapter);
					});
				});

    }

	@Override
	public void onDismiss(DialogInterface dialog) {
		this.finish();
	}

	public Media getMedia() {
        Bundle b = getIntent().getExtras();

        if (b != null) {
            return (Media)b.getSerializable("media");
        }

        return null;
    }
	
}
