package se.qxx.android.jukebox.activities.fragments;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import se.qxx.android.jukebox.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
