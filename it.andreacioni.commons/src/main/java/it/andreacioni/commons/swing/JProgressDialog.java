package it.andreacioni.commons.swing;

import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

public class JProgressDialog extends JDialog {

	/**
	 *
	 */
	private static final long serialVersionUID = -1987000903605227613L;

	private JProgressBar progressBar;
	private JFrame motherFrame;
	private JLabel label;

	private Semaphore semaphore;

	private boolean result = false, cancelled = false;

	public JProgressDialog(JFrame frame, String title, String text, int min, int max) {
		super(frame, title, true);
		motherFrame = frame;
		semaphore = new Semaphore(0);
		progressBar = new JProgressBar(min, max);
		label = new JLabel("", SwingConstants.LEFT);

		createProgressUI(text, min == max);
	}

	public void setText(String text) {
		label.setText(text);
	}

	public void setProgress(int progress) {
		progressBar.setValue(progress);
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public boolean showDialog() {
		semaphore.release();
		setVisible(true);
		return result;
	}

	public void closeDialog() {
		try {
			semaphore.acquire(semaphore.availablePermits());
			semaphore.release();
		} catch (InterruptedException e) {
		}
		setVisible(false);
		dispose();

	}

	public void closeDialog(boolean res) {
		result = res;
		closeDialog();
	}

	private void createProgressUI(String text, boolean indeterminate) {
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setModal(true);
		setLocationRelativeTo(motherFrame);

		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);
		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		if (text != null)
			label.setText(text);

		progressBar.setIndeterminate(indeterminate);

		panel.add(label);
		panel.add(Box.createVerticalStrut(3));
		panel.add(progressBar);

		add(panel);

		pack();
	}

}
