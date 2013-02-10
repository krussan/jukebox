package se.qxx.android.jukebox.comm;

import android.app.ProgressDialog;
import android.content.Context;

public class JukeboxConnectionProgressDialog implements JukeboxResponseListener {

	ProgressDialog dialog;
	
	private JukeboxConnectionProgressDialog(ProgressDialog d) {
		this.dialog = d;
	}
	
	@Override
	public void onRequestComplete() {
	
	}

	public static JukeboxConnectionProgressDialog build(Context context, String message) {
		ProgressDialog d = new ProgressDialog(context);		
		JukeboxConnectionProgressDialog jc = new JukeboxConnectionProgressDialog(d);
		d.setMessage(message);
		d.show();
		
		return jc;
	}
}
