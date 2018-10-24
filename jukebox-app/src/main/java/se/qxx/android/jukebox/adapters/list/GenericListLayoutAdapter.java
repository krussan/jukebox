package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.protobuf.ByteString;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.model.IModelAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;

/***
 * Responsible of the list view showing all movies
 */
public abstract class GenericListLayoutAdapter<T> extends BaseAdapter {

	private Context context;
    private int listItemId;
    // the serverListSize is the total number of items on the server side,
    // which should be returned from the web request results
    protected int serverListSize = -1;

	protected Context getContext() { return context; }
    protected int getListItemId() {
        return listItemId;
    }

    private final int VIEWTYPE_ITEM = 0;
	private final int VIEWTYPE_FOOTER = 1;

    public void setServerListSize(int serverListSize){
        this.serverListSize = serverListSize;
    }

	public GenericListLayoutAdapter(Context context, int listItemId) {
		super();
		this.context = context;
		this.listItemId = listItemId;
	}

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEWTYPE_ITEM;
    }

    /**
     *  returns the correct view
     */
    @Override
    public  View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (getItemViewType(position) == VIEWTYPE_FOOTER) {
            // display the last row
            return layoutProgress(vi);
        }
        else {
            View v = vi.inflate(getListItemId(), null);
            initializeView(v, this.getItem(position));
            return v;
        }
    };

    private View layoutProgress(LayoutInflater vi) {
        View v = vi.inflate(R.layout.progresslistrow, null);
        View pb = v.findViewById(R.id.pbFooterProgress);

        if (pb != null) {
            if (Model.get().isLoading())
                pb.setVisibility(View.VISIBLE);
            else
                pb.setVisibility(View.INVISIBLE);
        }

        return v;
    }

    public abstract void initializeView(View v, T o);
	public abstract int getItemCount();
	public abstract T getDataObject(int position);
	public abstract long getObjectId(int position);

	@Override
    public int getCount() {
        return getItemCount() + 1; // add one to insert loading footer
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return (position >= this.getItemCount()) ? VIEWTYPE_FOOTER
                : VIEWTYPE_ITEM;
    }

    @Override
    public T getItem(int position) {
	    if (getItemViewType(position) == VIEWTYPE_ITEM)
	        return getDataObject(position);
	    else
	        return null;
    }


    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == VIEWTYPE_ITEM)
            return getObjectId(position);
        else
            return -1;
    }

    protected void setupThumbnail(View v, ByteString image) {
        if (image.isEmpty()) {
            Drawable drawable = ResourcesCompat.getDrawable(v.getResources(), R.drawable.movie2, null);
            scaleBitmap(v, ((BitmapDrawable) drawable).getBitmap());
        }
        else {
            Bitmap bitmap = GUITools.getBitmapFromByteArray(image.toByteArray());
            scaleBitmap(v, bitmap);
        }
    }

    private void scaleBitmap(View v, Bitmap bitmap) {
        Bitmap scaledImage = GUITools.scaleImage(80, bitmap, v.getContext());
        GUITools.setImageOnImageView(R.id.imageView1, scaledImage, v);
    }

    protected void hideDownloadAndCompletedIcons(View v) {
	    GUITools.hideView(R.id.imgWatched, v);
	    GUITools.hideView(R.id.imgConverted, v);
    }

    protected void setupDownloadedAndCompletedIcons(View v, List<Media> mediaList) {
        // set this to false for now until we have a way of identifying watched items
        GUITools.hideView(R.id.imgWatched, v);

        // If all media has a meta duration then hide the download icon
        boolean downloadFinished = true;
        boolean conversionFinished = true;
        for (Media md : mediaList) {
            if (!md.getDownloadComplete())
                downloadFinished = false;
            if (md.getConverterState() != JukeboxDomain.MediaConverterState.Completed && md.getConverterState() != JukeboxDomain.MediaConverterState.NotNeeded)
                conversionFinished = false;

        }
        if (downloadFinished)
            GUITools.hideView(R.id.imgDownloading, v);
        if (!conversionFinished)
            GUITools.hideView(R.id.imgConverted, v);
    }

    protected void setupSubtitles(View v, List<Subtitle> subtitleList) {
        List<Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(subtitleList);
        if (sortedSubtitles.size() > 0)
            IncludeSubtitleRating.initialize(sortedSubtitles.get(0), v);
        else
            IncludeSubtitleRating.hideAll(v);
    }

}