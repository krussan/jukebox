package se.qxx.android.jukebox;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class JukeboxActivityBase extends Activity {
	
	protected final View getRootView() {
		return ((ViewGroup)findViewById(android.R.id.content)).getChildAt(0);		
	}
	
	protected final Context getCurrentContext() {
		return this.getRootView().getContext();
	}
	
}
