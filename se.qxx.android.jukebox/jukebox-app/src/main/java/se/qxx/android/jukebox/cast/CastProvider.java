package se.qxx.android.jukebox.cast;

import android.app.Activity;

import com.google.protobuf.RpcCallback;

import se.qxx.android.jukebox.comm.OnListSubtitlesCompleteHandler;
import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.jukebox.widgets.SeekerListener;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.comm.client.JukeboxConnectionHandler;
import se.qxx.jukebox.domain.JukeboxDomain;

public abstract class CastProvider {

    private Activity parentContext;
    private JukeboxConnectionProgressDialog dialog;
    private JukeboxConnectionHandler comm;
    private String title;
    private SeekerListener seekerListener;

    public Activity getParentContext() {
        return this.parentContext;
    }

    public JukeboxConnectionProgressDialog getDialog() {
        return dialog;
    }

    public JukeboxConnectionHandler getJukeboxConnectionHandler() {
        return comm;
    }

    public String getTitle() {
        return title;
    }

    public SeekerListener getSeekerListener() {
        return seekerListener;
    }

    protected void setTitle(String title) {
        this.title = title;
    }

    public CastProvider(Activity parentContext, JukeboxConnectionHandler comm, JukeboxConnectionProgressDialog dialog, SeekerListener seekerListener) {
        this.parentContext = parentContext;
        this.dialog = dialog;
        this.comm = comm;
        this.seekerListener = seekerListener;
    }

    public static CastProvider getCaster(Activity parentContext, JukeboxConnectionHandler comm, JukeboxConnectionProgressDialog dialog, SeekerListener listener) {
        switch (ChromeCastConfiguration.getCastType()) {
            case ChromeCast:
                return new ChromeCastProvider(parentContext, comm, dialog, listener);
            case Local:
                return new LocalCastProvider(parentContext, comm, dialog, listener);
            case JukeboxCast:
                return new JukeboxCastProvider(parentContext, comm, dialog, listener);
        }

        return null;
    }

    protected void initializeSubtitles() {
        // update the subtitles out of sync
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.Log().d("Request --- ListSubtitles");
                comm.listSubtitles(Model.get().getCurrentMedia(), new OnListSubtitlesCompleteHandler());
            }
        });
        t.start();

    }

    public abstract void initialize(String title);
    public abstract void seek(long position);
    public abstract RpcCallback<JukeboxDomain.JukeboxResponseStartMovie> getCallback();
}
