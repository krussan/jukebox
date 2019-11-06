package se.qxx.android.jukebox.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;

public class JukeboxSettings extends SettingsBase {
	private SharedPreferences preferences;
	private SharedPreferences storage;
	
	private final String SERVER_IP_ADDRESS = "serverIpAddress";
	private final String SERVER_PORT = "serverPort";
	private final String CURRENT_MEDIA_PLAYER = "currentMediaPlayer";
	private final String MEDIA_PLAYER_IS_ON = "mediaPlayerIsOn";
	
	public String getServerIpAddress() {
		return preferences.getString(SERVER_IP_ADDRESS, "");
	}

	public void setServerIpAddress(String _serverIpAddress) {
		this.putString(preferences, SERVER_IP_ADDRESS, _serverIpAddress);
	}

	public boolean isCurrentMediaPlayerOn() {
		return storage.getBoolean(MEDIA_PLAYER_IS_ON, false);
	}

	public void setIsCurrentMediaPlayerOn(boolean value) {
		this.putBoolean(storage, MEDIA_PLAYER_IS_ON, value);
	}

	public int getServerPort() {
		return Integer.parseInt(preferences.getString(SERVER_PORT, "2150"));
	}

	public void setServerPort(int _serverPort) {
		this.putInt(preferences, SERVER_PORT, _serverPort);
	}
	
	public String getCurrentMediaPlayer() {
		return storage.getString(CURRENT_MEDIA_PLAYER, "vlc_main");
	}
	
	public void setCurrentMediaPlayer(String mediaPlayerName) {
		this.putString(storage, CURRENT_MEDIA_PLAYER, mediaPlayerName);
	}

	public JukeboxSettings(Context c) {
		preferences = PreferenceManager.getDefaultSharedPreferences(c);
		storage = c.getSharedPreferences("jukeboxstorage", 0);
	}
			
//	public static JukeboxSettings init(Context c) {
//		if (_instance == null)
//			_instance = new JukeboxSettings(c);
//
//		return _instance;
//	}

//	public static JukeboxSettings get(){
//		return _instance;
//	}

}
