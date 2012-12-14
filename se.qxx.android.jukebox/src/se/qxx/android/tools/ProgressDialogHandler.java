package se.qxx.android.tools;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

public class ProgressDialogHandler extends Handler {
	private ProgressDialog _dialog;
	private Context _context;
	public ProgressDialogHandler(Context c, ProgressDialog d) {
		_dialog = d;
		_context = c;
	}
	
	@Override
	public void handleMessage(Message msg) {
		super.handleMessage(msg);
		
		_dialog.dismiss();
		Boolean success = msg.getData().getBoolean("success");
		String message = msg.getData().getString("message");
		
		if (!success)
			Toast.makeText(this._context.getApplicationContext(), message, Toast.LENGTH_LONG).show();
	}
}
