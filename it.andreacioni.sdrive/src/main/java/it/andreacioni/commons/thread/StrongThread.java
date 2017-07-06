package it.andreacioni.commons.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongThread {

	private Logger LOG = LoggerFactory.getLogger(getClass());

	private Thread runnable;
	private Future<?> future;
	private ExecutorService executorService;

	public StrongThread(Runnable run) {
		runnable = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					run.run();
				} catch (Throwable e) {
					LOG.error("Thread {} died", Thread.currentThread().getName(), e);
				}
			}
		});
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

	public synchronized void stop(boolean mayInterruptIfRunning) {

		if (runnable != null) {
			runnable.interrupt();
			runnable = null;
		}

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
