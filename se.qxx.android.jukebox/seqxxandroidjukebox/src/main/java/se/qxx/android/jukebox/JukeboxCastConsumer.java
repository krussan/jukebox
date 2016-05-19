package se.qxx.android.jukebox;

import android.app.Activity;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.internal.ApplicationStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import java.net.URLEncoder;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

/**
 * Created by chris on 5/19/16.
 */
public class JukeboxCastConsumer extends VideoCastConsumerImpl {
    VideoCastManager mCastManager = null;
    Activity parentActivity = null;

    public JukeboxCastConsumer(Activity context) {
        mCastManager = VideoCastManager.getInstance();
        parentActivity = context;
    }

    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
        super.onApplicationConnected(appMetadata, sessionId, wasLaunched);

        if (wasLaunched)
            startCastVideo();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        super.onConnectionFailed(result);

        Logger.Log().e(result.getErrorMessage());
    }

    @Override
    public void onApplicationConnectionFailed(int errorCode) {
        super.onApplicationConnectionFailed(errorCode);

        Logger.Log().e(String.format("onApplicationConnectionFailed: %s", errorCode));
    }

    @Override
    public void onConnected() {
        super.onConnected();

        Logger.Log().i("onConnected");
    }

    @Override
    public void onFailed(int resourceId, int statusCode) {
        super.onFailed(resourceId, statusCode);

        Logger.Log().e(String.format("onFailed :: %s - %s", resourceId, statusCode));
    }

    @Override
    public void onMediaLoadResult(int statusCode) {
        super.onMediaLoadResult(statusCode);

        if (statusCode != CastStatusCodes.SUCCESS) {
            Logger.Log().e(String.format("onMediaLoadResult :: %s - %s", statusCode, CastStatusCodes.getStatusCodeString(statusCode)));

            if (mCastManager != null)
                mCastManager.disconnect();
        }
    }

    @Override
    public void onDataMessageReceived(String message) {
        super.onDataMessageReceived(message);

        Logger.Log().i(String.format("onDataMessageReceived :: %s", message));
    }

    @Override
    public void onApplicationStatusChanged(String appStatus) {
        super.onApplicationStatusChanged(appStatus);

        Logger.Log().i(String.format("onApplicationStatusChanged :: %s", appStatus));
    }

    @Override
    public void onDataMessageSendFailed(int errorCode) {
        super.onDataMessageSendFailed(errorCode);

        Logger.Log().e(String.format("onDataMessageSendFailed :: %s", errorCode));
    }

    protected void startCastVideo() {
        if (mCastManager != null) {
            JukeboxDomain.Movie m = Model.get().getCurrentMovie();

            String file = String.format("%s/%s", m.getMedia(0).getFilepath(), m.getMedia(0).getFilename());
            file = file.substring(0, 1).equals("/") ? file = file.substring(1) : file;
            file = file.substring(0, 2).equals("c/") ? file = file.substring(2) : file;


            String uri = String.format("file://192.168.1.120/%s", file);
            
            MediaMetadata md = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
            md.putString(MediaMetadata.KEY_TITLE, m.getTitle());

            MediaInfo mi = new MediaInfo.Builder(uri)
                    .setMetadata(md)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/mp4")
                    .build();

            mCastManager.startVideoCastControllerActivity(this.parentActivity, mi, 0, true);
        }
    }

}
