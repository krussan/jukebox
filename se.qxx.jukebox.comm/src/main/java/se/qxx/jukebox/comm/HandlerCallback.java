package se.qxx.jukebox.comm;

public interface HandlerCallback<T> {
    void run(T response);
}
