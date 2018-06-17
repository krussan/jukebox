package se.qxx.android.jukebox.adapters.list;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Series;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

public class SeriesLayoutAdapter extends GenericListLayoutAdapter {

    private List<Series> series;


    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

	public SeriesLayoutAdapter(Context context, List<Series> series) {
		super(context, R.layout.movielistrow);
		this.setSeries(series);
	}

    @Override
    public void initializeView(View v, Object o) {
	    if (o != null && o instanceof Series) {
	        Series s = (Series)o;

            GUITools.setTextOnTextview(R.id.toptext, s.getTitle(), v);
            GUITools.setTextOnTextview(R.id.bottomtext, Integer.toString(s.getYear()), v);

            // If all media has a meta duration then hide the download icon
            GUITools.hideView(R.id.imgDownloading, v);

            if (!s.getThumbnail().isEmpty()) {
                Bitmap image = GUITools.getBitmapFromByteArray(s.getThumbnail().toByteArray());
                Bitmap scaledImage = GUITools.scaleImage(80, image, v.getContext());
                GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
            }
            else {
                GUITools.setImageResourceOnImageView(R.id.imageView1, R.drawable.icon, v);
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.getSeries().size();
    }

    @Override
    public Object getDataObject(int position) {
        return this.getSeries().get(position);
    }

    @Override
    public long getObjectId(int position) {
        return this.getSeries().get(position).getID();
    }

}
	