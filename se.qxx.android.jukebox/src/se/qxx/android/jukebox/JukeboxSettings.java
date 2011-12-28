package se.qxx.android.jukebox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JukeboxSettings {
	private static JukeboxSettings _instance = null;
	SharedPreferences preferences;
	
	public String get_serverIpAddress() {
		return preferences.getString("serverIpAddress", "");
	}

	public void set_serverIpAddress(String _serverIpAddress) {
		preferences.edit().putString("serverIpAddress", _serverIpAddress);
	}
	
	public int get_serverPort() {
		return Integer.parseInt(preferences.getString("serverPort", "2150"));
	}

	public void set_serverPort(int _serverPort) {
		preferences.edit().putString("serverPort", String.valueOf(_serverPort));
	}

	private JukeboxSettings(Context c) {
		preferences = PreferenceManager.getDefaultSharedPreferences(c);
	}
			
	public static JukeboxSettings init(Context c) {
		if (_instance == null)
			_instance = new JukeboxSettings(c);
		
		return _instance;
	}
	
	public static JukeboxSettings get(){
		return _instance;
	}
			
}
