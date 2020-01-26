package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.text.InputType;

import androidx.appcompat.app.AppCompatActivity;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.fragments.SettingsFragment;

public class JukeboxPreferenceActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.settings_container, new SettingsFragment())
				.commit();
	}
}
