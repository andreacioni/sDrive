package it.andreacioni.sdrive.gui;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.sdrive.SDrive;

public class MasterPasswordManager {

	private static final ReentrantLock lock = new ReentrantLock();

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	private SDrive sDrive;

	public MasterPasswordManager(SDrive sDrive) {
		this.sDrive = sDrive;
	}

	public boolean prepareUpload(JDialog progressDialog) throws IOException {
		boolean ret = false;
		String s = null;

		lock.lock();
		if (!sDrive.isPasswordLoaded()) {
			LOG.info("Insert password to unlock file");
			if (sDrive.checkFirstStart()) {
				LOG.info("First start password asking");
				s = askForFirstPassword(progressDialog);
			} else {
				s = askForStdPassword(progressDialog);
			}

			if (s != null && !s.isEmpty()) {
				sDrive.setPassword(s);
				ret = true;
			}
		} else
			ret = true;
		lock.unlock();
		return ret;
	}

	private String askForFirstPassword(JDialog progressDialog) {
		String ret = null, s1, s2;

		do {
			s1 = "";
			s2 = "";
			while ((s1 != null && s1.isEmpty()) || (s2 != null && s2.isEmpty())) {
				s1 = askForPassword(progressDialog,
						"Insert a password for secure archive. You MUST remember it unlock the archive!");
				if (s1 == null)
					return null;

				s2 = askForPassword(progressDialog, "Please re-type the previous password");
				if (s2 == null)
					return null;
			}

			if (s1.equals(s2)) {
				ret = s1;
			} else {
				JOptionPane.showMessageDialog(progressDialog, "Two password doesn't match!", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} while (ret == null);

		return ret;
	}

	private String askForStdPassword(JDialog progressDialog) {
		return askForPassword(progressDialog, "What is the password of the secure archive? Please type it here below");
	}

	private String askForPassword(JDialog progressDialog, String message) {
		String ret = null;

		JPanel panel = new JPanel();
		BoxLayout layout = new BoxLayout(panel, BoxLayout.PAGE_AXIS);
		panel.setLayout(layout);
		JPasswordField pass = new JPasswordField();
		pass.requestFocus();
		panel.add(new JLabel(message));
		panel.add(pass);
		String[] options = new String[] { "OK", "Cancel" };
		int option = JOptionPane.showOptionDialog(progressDialog, panel, "Insert password", JOptionPane.NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, null);

		if (option == 0) {
			ret = new String(pass.getPassword());
		}

		return ret;
	}
}
