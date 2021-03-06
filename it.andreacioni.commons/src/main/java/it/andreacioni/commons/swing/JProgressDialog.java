package it.andreacioni.commons.swing;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.Box;
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

	private static final int PROGRESS_DIALOG_WIDTH = 380;

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
		// pack();
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
		setDefaultWindowListener();
		setModal(true);
		setLocationRelativeTo(motherFrame);

		JPanel panel = new JPanel(new GridBagLayout());

		panel.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));

		if (text != null)
			label.setText(text);
		progressBar.setIndeterminate(indeterminate);

		panel.add(label, createConstraints(0));
		panel.add(Box.createVerticalStrut(3), createConstraints(1));
		panel.add(progressBar, createConstraints(2));

		add(panel);

		pack();

		setSize(new Dimension(PROGRESS_DIALOG_WIDTH, getHeight()));
		setPreferredSize(new Dimension(PROGRESS_DIALOG_WIDTH, getHeight()));
		setMaximumSize(new Dimension(PROGRESS_DIALOG_WIDTH, getHeight()));
	}

	private GridBagConstraints createConstraints(int ylevel) {
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.LINE_START;
		c.gridy = ylevel;
		c.weightx = 3;
		return c;
	}

	private void setDefaultWindowListener() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				closeDialog();
			}
		});

	}

}
