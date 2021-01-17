package se.qxx.jukebox.webserver;

import com.google.inject.Inject;
import fi.iki.elonen.NanoHTTPD;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class WebServerAsyncRunner implements NanoHTTPD.AsyncRunner {
    private ExecutorService executorService;
    private final List<NanoHTTPD.ClientHandler> running =
        Collections.synchronizedList(new ArrayList<>());

    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Inject
    public WebServerAsyncRunner(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void closeAll() {
        // copy of the list for concurrency
        for (NanoHTTPD.ClientHandler clientHandler : new ArrayList<>(this.running)) {
            clientHandler.close();
        }
    }

    @Override
    public void closed(NanoHTTPD.ClientHandler clientHandler) {
        this.running.remove(clientHandler);
    }

    @Override
    public void exec(NanoHTTPD.ClientHandler clientHandler) {
        this.getExecutorService().submit(clientHandler);
        this.running.add(clientHandler);
    }
}
