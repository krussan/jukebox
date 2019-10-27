package se.qxx.jukebox.comm;

import com.google.common.util.concurrent.FutureCallback;

public interface HandlerCallback<T> {
     void run(T response);
}
