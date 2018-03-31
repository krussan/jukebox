package se.qxx.android.jukebox.adapters.list;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import se.qxx.android.jukebox.R;
import se.qxx.android.jukebox.activities.IncludeSubtitleRating;
import se.qxx.android.jukebox.model.IModelAdapter;
import se.qxx.android.jukebox.model.Model;
import se.qxx.android.tools.GUITools;
import se.qxx.android.tools.Logger;
import se.qxx.jukebox.domain.JukeboxDomain.Media;
import se.qxx.jukebox.domain.JukeboxDomain.Movie;
import se.qxx.jukebox.domain.JukeboxDomain.Subtitle;
import se.qxx.jukebox.domain.Sorter;

/***
 * Responsible of the list view showing all movies
 */
public abstract class GenericListLayoutAdapter extends BaseAdapter {

	private Context context;
    private int listItemId;
	protected Context getContext() { return context; }
    protected int getListItemId() {
        return listItemId;
    }

    private final int VIEWTYPE_ITEM = 0;
	private final int VIEWTYPE_FOOTER = 1;

	public GenericListLayoutAdapter(Context context, int listItemId) {
		super();
		this.context = context;
		this.listItemId = listItemId;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;

		try {
	        if (v == null) {
	            LayoutInflater vi = (LayoutInflater)this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	            if (getItemViewType(position) == VIEWTYPE_FOOTER)
	                v = layoutProgress(vi);
                else
	                v = vi.inflate(getListItemId(), null);
	        }

	        if (!isLastItem(position)) {
                Object o = this.getItem(position);
                initializeView(v, o);
            }
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}

    private View layoutProgress(LayoutInflater vi) {
        View v = vi.inflate(R.layout.progresslistrow, null);
        View pb = v.findViewById(R.id.pbFooterProgress);

//        if (pb != null) {
//            if (Model.get().isLoading())
//                pb.setVisibility(View.VISIBLE);
//            else
//                pb.setVisibility(View.INVISIBLE);
//        }

        return v;
    }

    public abstract void initializeView(View v, Object o);
	public abstract int getItemCount();
	public abstract Object getDataObject(int position);

	@Override
    public int getCount() {
        return getItemCount() + 1; // add one to insert loading footer
    }

    public boolean isLastItem(int position) {
        int count = this.getItemCount();
        return (position == count);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
	    if (isLastItem(position))
	        return VIEWTYPE_FOOTER;
	    else
	        return VIEWTYPE_ITEM;
    }

    @Override
    public Object getItem(int position) {
	    if (getItemViewType(position) == VIEWTYPE_ITEM)
	        return getDataObject(position);
	    else
	        return null;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == VIEWTYPE_ITEM)
            return position;
        else
            return -1;
    }
}
	