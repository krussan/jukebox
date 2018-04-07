package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;

import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.settings.JukeboxSettings;
import se.qxx.android.jukebox.R;

/**
 * Created by chris on 2/18/17.
 */

public class ChromeCastConfiguration {

   /* public static CastConfiguration get() {
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
    }*/

    /*public static void initialize(Activity activity) {
        if (isChromeCastActive()) {
            ChromeCastConfiguration.checkGooglePlayServices(activity);

            CastConfiguration options = ChromeCastConfiguration.get();

            VideoCastManager.initialize(activity, options);
        }
    }*/

    /**
     * A utility method to validate that the appropriate version of the Google Play Services is
     * available on the device. If not, it will open a dialog to address the issue. The dialog
     * displays a localized message about the error and upon user confirmation (by tapping on
     * dialog) will direct them to the Play Store if Google Play services is out of date or
     * missing, or to system settings if Google Play services is disabled on the device.
     */
    public static boolean checkGooglePlayServices(final Activity activity) {
        final int googlePlayServicesCheck = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            default:
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, googlePlayServicesCheck, 0);

                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        activity.finish();
                    }
                });
                dialog.show();
        }
        return false;
    }

    public static boolean isChromeCastActive() {
        return StringUtils.equalsIgnoreCase("Chromecast", JukeboxSettings.get().getCurrentMediaPlayer());
    }

    public static void createMenu(Context context, MenuInflater inflater, Menu menu) {
        if (isChromeCastActive()) {
            inflater.inflate(R.menu.cast, menu);
            CastButtonFactory.setUpMediaRouteButton(context.getApplicationContext(),
                    menu,
                    R.id.media_route_menu_item);
        }
    }

    public static TextTrackStyle getTextStyle() {
        TextTrackStyle style = new TextTrackStyle();
        style.setBackgroundColor(Color.parseColor("#00FFFFFF"));
        style.setFontGenericFamily(TextTrackStyle.FONT_FAMILY_SANS_SERIF);
        style.setFontScale(0.9f);
        style.setForegroundColor(Color.parseColor("#FFFFF000"));
        style.setEdgeType(TextTrackStyle.EDGE_TYPE_OUTLINE);
        style.setEdgeColor(Color.rgb(0,0,0));
        style.setFontStyle(TextTrackStyle.FONT_STYLE_ITALIC);

        return style;
    }

    public static RemoteMediaClient getRemoteMediaClient(Context context) {
        CastSession castSession = CastContext.getSharedInstance(context)
                .getSessionManager()
                .getCurrentCastSession();

        return castSession.getRemoteMediaClient();
    }
}
