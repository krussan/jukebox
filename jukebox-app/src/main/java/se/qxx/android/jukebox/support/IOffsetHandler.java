package se.qxx.android.jukebox.support;

import se.qxx.android.jukebox.activities.ViewMode;
import se.qxx.jukebox.domain.JukeboxDomain;

public interface IOffsetHandler {
    void setOffset(int offset);
    ViewMode getMode();
    JukeboxDomain.Season getSeason();
    JukeboxDomain.Series getSeries();
}
