package it.andreacioni.commons.logging.appender;

import javax.swing.JOptionPane;

import org.apache.commons.io.output.NullOutputStream;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;

public class SwingPopupAppender extends OutputStreamAppender<ILoggingEvent> {

	public SwingPopupAppender() {
		setOutputStream(new NullOutputStream());
	}

	@Override
	protected void append(ILoggingEvent logEvent) {
		JOptionPane.showMessageDialog(null, parseData(logEvent), logEvent.getLevel().levelStr,
				getMessageTypeFromLevel(logEvent.getLevel()));
	}

	private int getMessageTypeFromLevel(Level level) {
		switch (level.levelInt) {
		case Level.WARN_INT:
			return JOptionPane.WARNING_MESSAGE;
		case Level.ERROR_INT:
			return JOptionPane.ERROR_MESSAGE;
		default:
			return JOptionPane.INFORMATION_MESSAGE;
		}
	}

	private String parseData(ILoggingEvent logEvent) {
		return new String(encoder.encode(logEvent));
	}

}
