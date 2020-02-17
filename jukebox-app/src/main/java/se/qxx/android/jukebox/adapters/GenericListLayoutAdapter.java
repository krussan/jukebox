package se.qxx.android.jukebox.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import androidx.core.content.res.ResourcesCompat;
import com.google.protobuf.ByteString;
import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.settings.CacheData;
import se.qxx.android.tools.GUITools;
import se.qxx.jukebox.domain.JukeboxDomain;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;

import java.util.List;

/***
 * Responsible of the list view showing all movies
 */
public abstract class GenericListLayoutAdapter<T> extends BaseAdapter {

	private Context context;
    private int listItemId;
    // the serverListSize is the total number of items on the server side,
    // which should be returned from the web request results
    protected int serverListSize = -1;
    protected boolean isLoading;
    private CacheData cacheData;

	protected Context getContext() { return context; }
    protected int getListItemId() {
        return listItemId;
    }

    private final int VIEWTYPE_ITEM = 0;
	private final int VIEWTYPE_FOOTER = 1;

	public void setLoading(boolean isLoading) {
	    this.isLoading = isLoading;
    }

    public void setServerListSize(int serverListSize){
        this.serverListSize = serverListSize;
    }

	public GenericListLayoutAdapter(Context context, int listItemId) {
		super();
		this.context = context;
		this.listItemId = listItemId;
        this.setCacheData(new CacheData(context));
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
            View v = vi.inflate(getListItemId(), parent, false);
            initializeView(v, this.getItem(position));
            return v;
        }
    };

    private View layoutProgress(LayoutInflater vi) {
        View v = vi.inflate(R.layout.progresslistrow, null);
        View pb = v.findViewById(R.id.pbFooterProgress);

        if (pb != null) {
            if (this.isLoading)
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
            //scaleBitmap(v, ((BitmapDrawable) drawable).getBitmap());
            GUITools.setImageOnImageView(R.id.imageView1, ((BitmapDrawable) drawable).getBitmap(), v);
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
	    v.findViewById(R.id.imgWatched).setVisibility(View.GONE);
        v.findViewById(R.id.imgConverted).setVisibility(View.GONE);
        v.findViewById(R.id.imgConverting).setVisibility(View.GONE);
        v.findViewById(R.id.imgSub).setVisibility(View.GONE);
        v.findViewById(R.id.imgDownloading).setVisibility(View.GONE);
    }

    protected void setupDownloadedAndCompletedIcons(View v, List<Media> mediaList) {
        // set this to false for now until we have a way of identifying watched items
        v.findViewById(R.id.imgWatched).setVisibility(View.GONE);

        // If all media has a meta duration then hide the download icon
        boolean downloadFinished = true;
        boolean conversionFinished = true;
        boolean converting = false;

        JukeboxDomain.MediaConverterState state = JukeboxDomain.MediaConverterState.Completed;

        for (Media md : mediaList) {
            if (!md.getDownloadComplete())
                downloadFinished = false;

            if (md.getConverterState() == JukeboxDomain.MediaConverterState.Converting || md.getConverterState() == JukeboxDomain.MediaConverterState.Queued)
                converting = true;

            if (md.getConverterState() != JukeboxDomain.MediaConverterState.Completed && md.getConverterState() != JukeboxDomain.MediaConverterState.NotNeeded)
                conversionFinished = false;
        }

        if (downloadFinished)
            v.findViewById(R.id.imgDownloading).setVisibility(View.GONE);

        if (!conversionFinished || converting) {
            v.findViewById(R.id.imgConverted).setVisibility(View.GONE);
        }

        if (!converting) {
            v.findViewById(R.id.imgConverting).setVisibility(View.GONE);
        }

    }

    protected void setupSubtitles(View v, List<Subtitle> subtitleList) {
        List<Subtitle> sortedSubtitles = Sorter.sortSubtitlesByRating(subtitleList);

        if (sortedSubtitles.size() > 0)
            IncludeSubtitleRating.initialize(sortedSubtitles.get(0), v);
        else
            IncludeSubtitleRating.initialize(null, v);

    }

    public CacheData getCacheData() {
        return cacheData;
    }

    public void setCacheData(CacheData cacheData) {
        this.cacheData = cacheData;
    }

    protected void setupProgressBar(View v, int duration, int mediaId) {
        ProgressBar progressWatched = v.findViewById(R.id.progressWatched);
        if (progressWatched != null) {
            int progress = 0;
            if (duration > 0) {
                progress = (int) (100f * (float) this.getCacheData().getMediaState(mediaId) / (float) (duration * 60));
            }

            if (progress > 0)
                progressWatched.setProgress(progress);
            else
                progressWatched.setVisibility(View.INVISIBLE);
        }

    }
}