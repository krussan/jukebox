package se.qxx.android.jukebox;

import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by chris on 2/18/17.
 */

public class ChromeCastConfiguration {

    public static CastConfiguration get() {
        return new CastConfiguration.Builder(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .enableAutoReconnect()
                .enableCaptionManagement()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .enableDebug()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .build();
    }

    public static void initialize(Activity activity) {
        if (isChromeCastActive()) {
            BaseCastManager.checkGooglePlayServices(activity);

            CastConfiguration options = ChromeCastConfiguration.get();

            VideoCastManager.initialize(activity, options);
        }
    }

    public static boolean isChromeCastActive() {
        return StringUtils.equalsIgnoreCase("Chromecast", JukeboxSettings.get().getCurrentMediaPlayer());
    }

    public static void createMenu(MenuInflater inflater, Menu menu) {
        if (isChromeCastActive()) {
            inflater.inflate(R.menu.cast, menu);
            VideoCastManager mCastManager = VideoCastManager.getInstance();

            if (mCastManager != null)
                mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        }
    }

    public static void onResume() {
        if (isChromeCastActive()) {
            VideoCastManager mCastManager = VideoCastManager.getInstance();

            if (mCastManager != null)
                mCastManager.incrementUiCounter();
        }
    }

    public static void onPause() {
        if (isChromeCastActive()) {
            VideoCastManager.getInstance().decrementUiCounter();
        }
    }
}
