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

	            if (isLastItem(position) && position > 0)
                    v = vi.inflate(R.layout.progresslistrow, null);
                else
	                v = vi.inflate(getListItemId(), null);
	        }

	        if (!isLastItem(position)) {
                Movie m = (Movie) this.getItem(position);
                initializeView(v, m);
            }
		}
		catch (Exception e) {
			Logger.Log().e("Error occured while populating list", e);
		}
			
        return v;
	}

	public abstract void initializeView(View v, Object o);
	public abstract int getItemCount();

	@Override
    public int getCount() {
        return getItemCount() + 1; // add one to insert loading footer
    }

    public boolean isLastItem(int position) {
        int count = this.getItemCount();
        return (position >= count);
    }

}
	