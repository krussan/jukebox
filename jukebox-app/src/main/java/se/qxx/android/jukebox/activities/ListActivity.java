package se.qxx.android.jukebox.activities;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.JukeboxFragment;
import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;
import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ListActivity
        extends BaseActivity
        implements JukeboxFragment.JukeboxFragmentHandler {

    private JukeboxSettings settings;
    private JukeboxConnectionHandler connectionHandler;

    public ViewMode getMode() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            ViewMode mode = (ViewMode) b.getSerializable("mode");
            if (mode != null)
                return mode;
        }

        return ViewMode.Season;
    }

    public JukeboxDomain.Series getSeries() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            Object o = b.getSerializable("series");
            if (o != null)
                return (JukeboxDomain.Series)o;
        }

        return null;
    }
    public JukeboxDomain.Season getSeason() {
        Bundle b = getIntent().getExtras();
        if (b != null) {
            Object o = b.getSerializable("season");
            if (o != null)
                return (JukeboxDomain.Season)o;
        }

        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        settings = new JukeboxSettings(this);
        setupConnectionHandler(false);

        setContentView(R.layout.jukebox_list_wrapper);

        initializeView();
    }

    private void initializeView() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        JukeboxFragment fragment = JukeboxFragment.newInstance(this.getMode(), this.getSeries(), this.getSeason());
        ft.add(R.id.rootJukeboxListWrapper, fragment);
        ft.commit();
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


