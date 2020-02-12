package se.qxx.android.jukebox.cast;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.MenuItem;
import androidx.appcompat.widget.SearchView;
import com.google.android.gms.cast.TextTrackStyle;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.apache.commons.lang3.StringUtils;

import se.qxx.android.jukebox.R;

/**
 * Created by chris on 2/18/17.
 */

public class ChromeCastConfiguration {

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

                dialog.setOnCancelListener(dialogInterface -> activity.finish());
                dialog.show();
        }
        return false;
    }

    public static JukeboxCastType getCastType() {
        if (isChromecastConnected())
            return JukeboxCastType.ChromeCast;
        else
            return JukeboxCastType.Local;
    }

    public static void createMenu(Context context,
                                  MenuInflater inflater,
                                  Menu menu,
                                  SearchView.OnQueryTextListener queryTextListener,
                                  SearchView.OnCloseListener closeListener) {
        inflater.inflate(R.menu.cast, menu);
        CastButtonFactory.setUpMediaRouteButton(context.getApplicationContext(),
                menu,
                R.id.media_route_menu_item);

        MenuItem searchItem = menu.findItem(R.id.search_menu_item);
        if (searchItem != null) {
            SearchView searchView = (SearchView) searchItem.getActionView();
            if (queryTextListener != null)
                searchView.setOnQueryTextListener(queryTextListener);

            if (closeListener != null)
                searchView.setOnCloseListener(closeListener);
        }
    }

    public static TextTrackStyle getTextStyle() {
        TextTrackStyle style = new TextTrackStyle();

        style.setBackgroundColor(Color.parseColor("#00000000"));
        style.setFontGenericFamily(TextTrackStyle.FONT_FAMILY_SANS_SERIF);
        style.setFontScale(0.9f);
        style.setForegroundColor(Color.parseColor("#FFEFEFEF"));
        //style.setEdgeType(TextTrackStyle.EDGE_TYPE_OUTLINE);
        style.setEdgeColor(Color.rgb(0,0,0));
        //style.setFontStyle(TextTrackStyle.FONT_STYLE_ITALIC);


        return style;
    }

    public static RemoteMediaClient getRemoteMediaClient(Context context) {
        CastSession castSession = CastContext.getSharedInstance(context)
                .getSessionManager()
                .getCurrentCastSession();

        if (castSession == null)
            return null;

        return castSession.getRemoteMediaClient();
    }

    public static boolean isChromecastConnected() {
        CastSession castSession = CastContext.getSharedInstance()
                .getSessionManager()
                .getCurrentCastSession();

        return castSession != null;
    }

}
