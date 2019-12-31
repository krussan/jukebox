package se.qxx.jukebox.interfaces;

import java.io.File;
import java.io.IOException;

import se.qxx.jukebox.tools.WebResult;

public interface IWebRetriever {

	WebResult getWebResult(String urlString) throws IOException;
	WebResult postWebResult(String baseUrl, String query) throws IOException;
	File getWebFile(String urlString, String savePath) throws IOException;
	byte[] getWebFileData(String urlString) throws IOException;

}