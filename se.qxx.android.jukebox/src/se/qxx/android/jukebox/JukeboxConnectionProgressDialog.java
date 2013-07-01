package se.qxx.android.jukebox;

import se.qxx.jukebox.comm.client.JukeboxConnectionMessage;
import se.qxx.jukebox.comm.client.JukeboxResponseListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.widget.Toast;

public class JukeboxConnectionProgressDialog implements JukeboxResponseListener {
	private Context context;
	private ProgressDialog dialog;
	
	private JukeboxConnectionProgressDialog(Context c, ProgressDialog d) {
		this.dialog = d;
		this.context = c;
	}

	public static JukeboxConnectionProgressDialog build(Context context, String message) {
		ProgressDialog d = new ProgressDialog(context);		
		JukeboxConnectionProgressDialog jc = new JukeboxConnectionProgressDialog(context, d);
		d.setMessage(message);
		d.show();
		
		return jc;
	}

	@Override
	public void onRequestComplete(JukeboxConnectionMessage message) {
		this.dialog.dismiss();
		Boolean success = message.result();
		String msg = message.getMessage();
		
		if (!success)
			Toast.makeText(this.context.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
		
	}
}
