package se.qxx.android.jukebox.widgets;

import java.util.Date;

import se.qxx.android.jukebox.comm.JukeboxConnectionHandler;

public class Seeker implements Runnable {

    private Thread internalThread;
    private boolean isRunning = false;
    private final SeekerListener listener;
    private final JukeboxConnectionHandler connectionHandler;

    public Seeker(SeekerListener listener, JukeboxConnectionHandler connectionHandler) {
        this.listener = listener;
        this.connectionHandler = connectionHandler;
    }

    public void stop() {
        this.isRunning = false;
    }

    private JukeboxConnectionHandler getConnectionHandler() {
        return this.connectionHandler;
    }

    private void getTime() {
        if (this.getConnectionHandler() != null) {
            this.getConnectionHandler().getTime("Dummy", response -> {
                // The time command also returns the name of the currently playing file.
                // If it differs from the model then set the current media
                if (response != null && listener != null)
                    listener.updateSeeker(response.getSeconds(), 0);
            });
        }
    }

    @Override
    public void run() {
        this.isRunning = true;
        while (this.isRunning) {
            try {
                long millis = (new Date()).getTime();

                while ((new Date()).getTime() - millis < 15000 && this.isRunning) {
                    Thread.sleep(1000);
                    if (this.listener != null)
                        this.listener.increaseSeeker(1);
                }

                getTime();
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void start(boolean immediate) {
        if (internalThread != null) {
            if (internalThread.isAlive())
                this.isRunning = true;
            else {
                startNewThread();
            }
        } else {
            startNewThread();
        }

        if (immediate)
            getTime();
    }

    public void start() {
        this.start(false);
    }

    private void startNewThread() {
        internalThread = new Thread(this);
        internalThread.start();
    }

    public void toggle() {
        if (this.isRunning)
            this.stop();
        else
            this.start();
    }
}
