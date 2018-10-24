package se.qxx.android.jukebox.adapters.list;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public class SeriesLayoutAdapter extends GenericListLayoutAdapter<Series> {

    private List<Series> series = new ArrayList<>();

    public List<Series> getSeries() {
        return series;
    }

    public void addSeries(List<Series> series) {
        this.getSeries().addAll(series);
    }

    public void clearSeries() {
        this.getSeries().clear();
    }

	public SeriesLayoutAdapter(Context context, List<Series> series) {
		super(context, R.layout.movielistrow);
		this.clearSeries();
		this.addSeries(series);
	}

    @Override
    public void initializeView(View v, Series s) {
	    if (s != null) {
            GUITools.setTextOnTextview(R.id.toptext, s.getTitle(), v);

            setYear(v, s);
            hideDownloadAndCompletedIcons(v);
            setupThumbnail(v, s.getThumbnail());
            setupSubtitles(v, new ArrayList<>());
        }
    }

    private void setYear(View v, Series s) {
        int year = s.getYear();
        if (year > 0)
            GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(year), v);
        else
            GUITools.hideView(R.id.bottomtext, v);
    }

    @Override
    public int getItemCount() {
        return this.getSeries().size();
    }

    @Override
    public Series getDataObject(int position) {
        return this.getSeries().get(position);
    }

    @Override
    public long getObjectId(int position) {
        return this.getSeries().get(position).getID();
    }

}
	