package se.qxx.jukebox.servercomm;

import java.io.IOException;

public interface IWakeOnLan {

	void sendPacket(String ipAddress, String macAddress) throws IOException;

}