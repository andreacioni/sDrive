package it.andreacioni.sdrive.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingPopupAppenderTest {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Test
	public void test() {
		logger.info("Hello world!");
		logger.error("Hello world!");
		logger.warn("Hello world!");
		logger.trace("Hello world!");
	}

	@Test
	public void testWithException() {
		try {
			throw new NullPointerException("Is null!!!!");
		} catch (Exception e) {
			logger.error("Exception", e);
		}
	}
}
