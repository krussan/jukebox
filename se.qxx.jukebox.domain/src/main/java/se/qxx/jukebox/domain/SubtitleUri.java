package se.qxx.jukebox.domain;

import com.sun.jndi.toolkit.url.Uri;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class SubtitleUri {
    private JukeboxDomain.Subtitle subtitle;
    private URI uri;

    public JukeboxDomain.Subtitle getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(JukeboxDomain.Subtitle subtitle) {
        this.subtitle = subtitle;
    }

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public SubtitleUri(JukeboxDomain.Subtitle subtitle, URI uri) {
        this.uri = uri;
        this.subtitle = subtitle;
    }

    public static List<SubtitleUri> createFromSubtitleList(List<JukeboxDomain.Subtitle> subtitles, List<String> uris) {
        List<SubtitleUri> result = new ArrayList<>();
        if (subtitles.size() == uris.size()) {

            for (int i = 0; i < subtitles.size(); i++) {
                result.add(new SubtitleUri(subtitles.get(i), URI.create(uris.get(i))));
            }

        }
        return result;
    }
}
