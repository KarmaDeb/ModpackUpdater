package ml.karmaconfigs.modpackupdater.utils;

import java.util.concurrent.CompletableFuture;

public final class AsyncScheduler {

    private final Runnable task;

    /**
     * Initialize the async scheduler
     *
     * @param run the task to run
     */
    public AsyncScheduler(final Runnable run) {
        task = run;
    }

    /**
     * Run the task asynchronous
     */
    public final void run() {
        CompletableFuture.runAsync(task);
    }
}
