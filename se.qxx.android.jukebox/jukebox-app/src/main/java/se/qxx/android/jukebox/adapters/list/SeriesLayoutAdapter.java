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

public class SeriesLayoutAdapter extends GenericListLayoutAdapter {

	public SeriesLayoutAdapter(Context context) {
		super(context, R.layout.movielistrow);
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
        return Model.get().countSeries();
    }

    @Override
    public Object getDataObject(int position) {
        return Model.get().getSeries(position);
    }

}
	