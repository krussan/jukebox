package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.JukeboxFragment;
import se.qxx.android.jukebox.adapters.JukeboxFragmentAdapter;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.settings.JukeboxSettings;

public class FlipperListActivity
    extends BaseActivity
    implements JukeboxFragment.JukeboxFragmentHandler {

    ViewPager pager;
    private JukeboxFragmentAdapter adapter;
    private JukeboxSettings settings;
    private JukeboxConnectionHandler connectionHandler;

    @Override
    protected int getSearchContainer() {
        return R.id.rootJukeboxViewPager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new JukeboxSettings(this);
        setupConnectionHandler(false);

		setContentView(R.layout.jukebox_main_wrapper);
		//TOOD: Load main fragment
        initializeView();
    }

    private void initializeView() {
        pager = findViewById(R.id.rootJukeboxViewPager);
        adapter = new JukeboxFragmentAdapter(this.getSupportFragmentManager(), this);
        pager.setAdapter(adapter);
    }


    private void setupConnectionHandler(boolean reAttachCallbacks) {
        if (this.connectionHandler == null) {
            connectionHandler = new JukeboxConnectionHandler(
                    settings.getServerIpAddress(),
                    settings.getServerPort());

            if (reAttachCallbacks) {
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (f instanceof JukeboxConnectionHandler.ConnectorCallbackEventListener)
                        connectionHandler.addCallback((JukeboxConnectionHandler.ConnectorCallbackEventListener) f);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.connectionHandler.stop();
        this.connectionHandler = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupConnectionHandler(true);
    }

    @Override
    public JukeboxConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
    }

}
