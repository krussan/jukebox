package se.qxx.jukebox.imdb;

import java.net.MalformedURLException;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.interfaces.IIMDBUrlRewrite;

public class IMDBUrlRewrite implements IIMDBUrlRewrite {

	@Override
	public String fixUrl(String url) throws MalformedURLException {
		if (StringUtils.startsWithIgnoreCase(url, "www.imdb.com"))
			url = "https://" + url;

		if (StringUtils.startsWithIgnoreCase(url, "/"))
			url = "https://www.imdb.com" + url;

		if (!StringUtils.startsWithIgnoreCase(url, "https://www.imdb.com"))
			throw new MalformedURLException(
					String.format("A IMDB url must start with https://www.imdb.com. Url was :: %s", url));
		
		return url;
	}

}
