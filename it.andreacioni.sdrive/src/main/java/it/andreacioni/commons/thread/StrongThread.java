package it.andreacioni.commons.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class StrongThread {
	private Runnable runnable;
	private Future<?> future;
	private ExecutorService executorService;

	public StrongThread(Runnable run) {
		runnable = run;
	}

	public void start() {
		if (future != null && !future.isDone())
			throw new IllegalStateException("Thread already started");

		executorService = Executors.newSingleThreadExecutor();
		future = executorService.submit(runnable);
	}

	public void stop() {
		stop(true);
	}

	public boolean isCancelled() {
		return future.isCancelled();
	}

	public boolean isDone() {
		return future.isDone();
	}

	public void stop(boolean mayInterruptIfRunning) {
		if (future != null) {
			future.cancel(mayInterruptIfRunning);
			future = null;
		}

		if (executorService != null) {
			executorService.shutdown();
			executorService.shutdownNow();
			executorService = null;
		}
	}
}
