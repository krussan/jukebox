package se.qxx.android.jukebox.activities;

import se.qxx.android.jukebox.model.Model;

public enum ViewMode {
    Movie,
    Series,
    Season,
    Episode;

    public static Model.ModelType getModelType(ViewMode mode) {
        if (mode == ViewMode.Movie)
            return Model.ModelType.Movie;
        else if (mode == ViewMode.Series)
            return Model.ModelType.Series;
        else if (mode == ViewMode.Season)
            return Model.ModelType.Season;
        else
            return Model.ModelType.Season;
    }

}
