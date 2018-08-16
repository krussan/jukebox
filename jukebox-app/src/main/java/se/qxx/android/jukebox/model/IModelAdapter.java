package se.qxx.android.jukebox.model;

/**
 * Created by vagrant on 3/20/18.
 */

public interface IModelAdapter {
    public int getCount();
    public Object getItem(int position);
    public long getItemId(int position);

}
