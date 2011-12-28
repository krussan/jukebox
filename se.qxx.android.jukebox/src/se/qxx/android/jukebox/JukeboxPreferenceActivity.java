package se.qxx.android.jukebox;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.text.InputType;

public class JukeboxPreferenceActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		EditTextPreference prefEditText = (EditTextPreference) findPreference("serverPort");
		prefEditText.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
	}
}
