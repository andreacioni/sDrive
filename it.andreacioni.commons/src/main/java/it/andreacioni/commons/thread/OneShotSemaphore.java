package it.andreacioni.commons.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

public class OneShotSemaphore extends Semaphore {

	/**
	 *
	 */
	private static final long serialVersionUID = 2103385731487738073L;

	private AtomicBoolean used;

	public OneShotSemaphore(boolean fair) {
		super(0, fair);
		used = new AtomicBoolean(false);
	}

	public OneShotSemaphore() {
		super(0, false);
		used = new AtomicBoolean(false);
	}

	public void acquireOneShot() throws InterruptedException {
		if (!used.get())
			acquire();
	}

	public void releaseOneShot() {
		if (!used.get()) {
			release();
			used.set(true);
		}
	}

}
