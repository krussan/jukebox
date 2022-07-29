package se.qxx.jukebox.imdb;

import com.google.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

import javax.inject.Singleton;

import se.qxx.jukebox.core.Log;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IIMDBGalleryHelper;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.ISettings;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.tools.WebResult;

@Singleton
public class IMDBGalleryHelper implements IIMDBGalleryHelper {

    private ISettings settings;
    private IWebRetriever webRetriever;
    private IJukeboxLogger log;

    public ISettings getSettings() {
        return settings;
    }

    public void setSettings(ISettings settings) {
        this.settings = settings;
    }

    public IWebRetriever getWebRetriever() {
        return webRetriever;
    }

    public void setWebRetriever(IWebRetriever webRetriever) {
        this.webRetriever = webRetriever;
    }

    public IJukeboxLogger getLog() {
        return log;
    }

    public void setLog(IJukeboxLogger log) {
        this.log = log;
    }

    @Inject
    public IMDBGalleryHelper(ISettings settings,
                             IWebRetriever webRetriever,
                             LoggerFactory loggerFactory) {
        this.setSettings(settings);
        this.setWebRetriever(webRetriever);
        this.setLog(loggerFactory.create(Log.LogType.IMDB));
    }

    public String getGalleryImageUrl(String galleryImageUrl) {
        try {
            WebResult webResult = this.getWebRetriever().getWebResult(galleryImageUrl);
            Document imgDoc = Jsoup.parse(webResult.getResult());
            Elements elm = imgDoc.select("img.hXPlvk");

            if (elm.size() > 0)
                return StringEscapeUtils.escapeHtml4(elm.get(0).attr("src"));
        }
        catch (IOException ioex) {
            this.getLog().Error("Error when getting image url", ioex);
        }

        return StringUtils.EMPTY;
    }

}
