package se.qxx.jukebox.interfaces;

import java.io.IOException;

public interface IWakeOnLan {

	void sendPacket(String ipAddress, String macAddress) throws IOException;

}