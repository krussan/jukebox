package se.qxx.android.jukebox.activities;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.text.InputType;

import se.qxx.android.jukebox.R;

public class JukeboxPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
	}

	public static class MyPreferenceFragment extends PreferenceFragment
	{
		@Override
		public void onCreate(final Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.preferences);
		}
	}
}
