package se.qxx.android.jukebox.comm;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Message;
import android.widget.Toast;

public class JukeboxConnectionProgressDialog implements JukeboxResponseListener {
	private Context context;
	private ProgressDialog dialog;
	
	private JukeboxConnectionProgressDialog(Context c, ProgressDialog d) {
		this.dialog = d;
		this.context = c;
	}
	
	@Override
	public void onRequestComplete(Message msg) {
		this.dialog.dismiss();
		Boolean success = msg.getData().getBoolean("success");
		String message = msg.getData().getString("message");
		
		if (!success)
			Toast.makeText(this.context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}

	public static JukeboxConnectionProgressDialog build(Context context, String message) {
		ProgressDialog d = new ProgressDialog(context);		
		JukeboxConnectionProgressDialog jc = new JukeboxConnectionProgressDialog(context, d);
		d.setMessage(message);
		d.show();
		
		return jc;
	}
}
