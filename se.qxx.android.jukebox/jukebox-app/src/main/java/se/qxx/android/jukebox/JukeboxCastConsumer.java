package se.qxx.android.jukebox;

import android.app.Activity;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastStatusCodes;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.internal.ApplicationStatus;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.CastException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.NoConnectionException;
import com.google.android.libraries.cast.companionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import org.apache.commons.lang3.StringUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;

/**
 * Created by chris on 5/19/16.
 */
public class JukeboxCastConsumer extends VideoCastConsumerImpl {
    VideoCastManager mCastManager = null;
    Activity parentActivity = null;
    String title = StringUtils.EMPTY;
    String movieUri = null;
    List<String> subtitleUris = null;
    List<Subtitle> subs = null;

    public JukeboxCastConsumer(
            Activity context,
            String title,
            List<Subtitle> subs,
            String movieUri,
            List<String> subtitleUris) {

        mCastManager = VideoCastManager.getInstance();
        parentActivity = context;
        this.title = title;
        this.subs = subs;
        this.movieUri = movieUri;
        this.subtitleUris = subtitleUris;
    }

    @Override
    public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
        super.onApplicationConnected(appMetadata, sessionId, wasLaunched);
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

        if (statusCode == CastStatusCodes.SUCCESS) {
            if (this.subtitleUris.size() > 0)
                mCastManager.setActiveTrackIds(new long[]{1});
        }
        else {

            Toast.makeText(this.parentActivity, String.format("Error loading file: %s", CastStatusCodes.getStatusCodeString(statusCode)), Toast.LENGTH_LONG);
            Logger.Log().e(String.format("onMediaLoadResult :: %s - %s", statusCode, CastStatusCodes.getStatusCodeString(statusCode)));

            if (mCastManager != null) {
                mCastManager.removeVideoCastConsumer(this);
            }
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

    public boolean isConnected() {
        if (mCastManager == null)
            return false;

        return mCastManager.isConnected();
    }


}
