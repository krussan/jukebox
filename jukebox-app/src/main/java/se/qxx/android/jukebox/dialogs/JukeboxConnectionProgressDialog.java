package se.qxx.android.jukebox.dialogs;

import se.qxx.android.jukebox.comm.JukeboxConnectionMessage;
import se.qxx.android.jukebox.comm.JukeboxResponseListener;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public class JukeboxConnectionProgressDialog implements JukeboxResponseListener {
	private Activity activity;
	private ProgressDialog dialog;
	
	private JukeboxConnectionProgressDialog(Activity a, ProgressDialog d) {
		this.dialog = d;
		this.activity = a;
	}

	public static JukeboxConnectionProgressDialog build(Activity a, String message) {
		ProgressDialog d = new ProgressDialog(a);		
		JukeboxConnectionProgressDialog jc = new JukeboxConnectionProgressDialog(a, d);
		d.setMessage(message);
		d.show();
		
		return jc;
	}
	
	final Handler mHandler = new Handler();	

	@Override
	public void onRequestComplete(JukeboxConnectionMessage message) {
		this.dialog.dismiss();
		Boolean success = message.result();
		final String msg = message.getMessage();
		final Context c = this.activity.getApplicationContext();
				
		if (!success) {
			this.activity.runOnUiThread(() -> Toast.makeText(c, msg, Toast.LENGTH_LONG).show());
		}		
	}

	public void close() {
		this.dialog.dismiss();
	}
}
