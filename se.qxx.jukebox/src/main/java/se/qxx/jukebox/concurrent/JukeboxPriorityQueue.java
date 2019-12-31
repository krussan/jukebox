package se.qxx.jukebox.concurrent;

import com.google.inject.Singleton;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class JukeboxPriorityQueue extends PriorityBlockingQueue<Runnable> {
    public JukeboxPriorityQueue() {
        super(1, (r1, r2) -> {
            if (r1 instanceof JukeboxThread && r2 instanceof JukeboxThread) {
                return Integer.compare(
                        ((JukeboxThread)r1).getJukeboxPriority(),
                        ((JukeboxThread)r2).getJukeboxPriority());
            }

            // equal if ordinary runnables
            return 0;
        });
    }
}
