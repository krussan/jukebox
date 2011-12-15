package se.qxx.android.tools;

public class Logger {

	private static final String TAG = "se.qxx.android.jukebox";
	
	private static Logger _logger;
	
	protected Logger() {
	}
	
	static public Logger Log() {
		if (_logger == null)
			_logger = new Logger();
		
		return _logger;
	}
	
	public void i(String message) {
		android.util.Log.i(TAG, message);
	}
	
	public void d(String message) {
		android.util.Log.d(TAG, message);
	}

	public void e(String message) {
		android.util.Log.e(TAG, message);
	}

	public void e(String message, Throwable t) {
		android.util.Log.e(TAG, message, t);
	}

}
