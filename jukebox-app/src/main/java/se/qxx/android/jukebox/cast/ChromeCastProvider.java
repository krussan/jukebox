package se.qxx.android.jukebox.cast;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.SurfaceView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.MediaTrack;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.images.WebImage;
import com.google.protobuf.RpcCallback;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import se.qxx.android.jukebox.dialogs.JukeboxConnectionProgressDialog;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;

public class ChromeCastProvider extends CastProvider implements RemoteMediaClient.ProgressListener {

    RemoteMediaClient client;

    @Override
    public void initialize() {

    }

    @Override
    public boolean usesMediaController() {
        return false;
    }


    @NonNull
    private long[] getActiveTracks(List<String> subtitleUris) {
        if (subtitleUris.size() > 0)
            return new long[] {1};
        else
            return new long[] {};
    }

    @Override
    public void surfaceCreated(SurfaceView view) {

    }

    @Override
    public void surfaceDestroyed() {

    }



}
