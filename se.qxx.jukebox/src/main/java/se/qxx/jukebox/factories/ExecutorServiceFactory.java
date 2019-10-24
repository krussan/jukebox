package se.qxx.jukebox.factories;

import com.google.inject.assistedinject.Assisted;
import org.slf4j.ILoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

public interface ExecutorServiceFactory {
    ExecutorService create(
            @Assisted("ThreadPoolSize") int threadPoolSize,
            @Assisted("Queue") BlockingQueue<Runnable> queue);
}
