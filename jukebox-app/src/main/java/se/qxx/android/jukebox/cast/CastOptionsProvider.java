package se.qxx.android.jukebox.cast;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.MediaIntentReceiver;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.Arrays;
import java.util.List;

import se.qxx.android.jukebox.activities.NowPlayingActivity;

/**
 * Created by vagrant on 4/7/18.
 */

public class CastOptionsProvider implements OptionsProvider {
    public static final String JUKEBOX_APP_ID = "EC008005";
    public static final String JUKEBOX_DEBUG_APP_ID = "C702B5E4";

    @Override
    public CastOptions getCastOptions(Context context) {
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setTargetActivityClassName(NowPlayingActivity.class.getName())
                .setActions(Arrays.asList(
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING,
                        MediaIntentReceiver.ACTION_DISCONNECT), new int[]{1, 2})
                //.setTargetActivityClassName(ExpandedControlsActivity.class.getName())
                .build();

        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .build();

        return new CastOptions.Builder()
                //.setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .setReceiverApplicationId(JUKEBOX_APP_ID)
                .setCastMediaOptions(mediaOptions)
                .build();

    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }
}
