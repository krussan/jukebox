package se.qxx.android.jukebox.comm;

public interface HandlerCallback<T> {
    void run(T response);
}
