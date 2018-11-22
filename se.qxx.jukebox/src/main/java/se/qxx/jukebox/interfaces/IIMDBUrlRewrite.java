package se.qxx.jukebox.interfaces;

import java.net.MalformedURLException;

public interface IIMDBUrlRewrite {
	String fixUrl(String url) throws MalformedURLException;
}
