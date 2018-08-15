package se.qxx.android.jukebox;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsBase {
	
	public void putBoolean(SharedPreferences pref, String key, boolean value) {
		Editor e = pref.edit();
		e.putBoolean(key, value);
		e.commit();
	}
	
	public void putString(SharedPreferences pref, String key, String value) {
		Editor e = pref.edit();
		e.putString(key, value);
		e.commit();
	}
	
	public void putLong(SharedPreferences pref, String key, long value) {
		Editor e = pref.edit();
		e.putLong(key, value);
		e.commit();
	}	
	
	public void putFloat(SharedPreferences pref, String key, float value) {
		Editor e = pref.edit();
		e.putFloat(key, value);
		e.commit();
	}	
	
	public void putInt(SharedPreferences pref, String key, int value) {
		Editor e = pref.edit();
		e.putInt(key, value);
		e.commit();
	}	
}