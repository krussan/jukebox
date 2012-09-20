package se.qxx.android.jukebox;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class JukeboxSettings {
	private static JukeboxSettings _instance = null;
	private SharedPreferences preferences;
	private SharedPreferences storage;
	
	private final String SERVER_IP_ADDRESS = "serverIpAddress";
	private final String SERVER_PORT = "serverPort";
	private final String CURRENT_MEDIA_PLAYER = "currentMediaPlayer";
	
	public String getServerIpAddress() {
		return preferences.getString(SERVER_IP_ADDRESS, "");
	}

	public void setServerIpAddress(String _serverIpAddress) {
		preferences.edit().putString(SERVER_IP_ADDRESS, _serverIpAddress);
	}
	
	public int getServerPort() {
		return Integer.parseInt(preferences.getString(SERVER_PORT, "2150"));
	}

	public void setServerPort(int _serverPort) {
		preferences.edit().putString(SERVER_PORT, String.valueOf(_serverPort));
	}
	
	public String getCurrentMediaPlayer() {
		return storage.getString(CURRENT_MEDIA_PLAYER, "vlc_main");
	}
	
	public void setCurrentMediaPlayer(String mediaPlayerName) {
		storage.edit().putString(CURRENT_MEDIA_PLAYER, mediaPlayerName);
	}

	private JukeboxSettings(Context c) {
		preferences = PreferenceManager.getDefaultSharedPreferences(c);
		storage = c.getSharedPreferences("jukeboxstorage", 0);
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
