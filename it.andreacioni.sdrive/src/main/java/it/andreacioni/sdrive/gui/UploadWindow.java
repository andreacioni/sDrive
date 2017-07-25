package it.andreacioni.sdrive.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.andreacioni.commons.swing.JProgressDialog;
import it.andreacioni.commons.swing.ProgressCallback;
import it.andreacioni.commons.thread.StrongThread;
import it.andreacioni.commons.utils.ImageUtils;
import it.andreacioni.sdrive.SDrive;

public class UploadWindow extends JFrame {

	/**
	 *
	 */
	private static final long serialVersionUID = -2968256448103259604L;

	private static final Color DD_COLOR = new Color(154, 154, 154);

	private static final int PROGRESS_DIALOG_WIDTH = 380;

	private static final int WIDTH = 400;

	private static final int HEIGHT = 400;

	private final Logger LOG = LoggerFactory.getLogger(getClass());

	public UploadWindow(SDrive sDrive) {

		setTitle("sDrive");
		setIconImage(ImageUtils.createImage("icon.png"));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setEnabled(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		UploadPanel panel = new UploadPanel(sDrive);

		add(panel);
	}

	private class UploadPanel extends JPanel {
		/**
		 *
		 */
		private static final long serialVersionUID = -7958340744297357525L;

		private SDrive sDrive;

		public UploadPanel(SDrive sDrive) {
			this.sDrive = sDrive;
			setLayout(new BorderLayout());
			setBorder(BorderFactory.createEmptyBorder(2, 10, 10, 10));
			add(new TopInfoPanel(), BorderLayout.PAGE_START);
			add(new BottomInfoPanel(), BorderLayout.PAGE_END);
			add(new DragAndDropAreaPanel(), BorderLayout.CENTER);
		}

		public class TopInfoPanel extends JPanel {

			/**
			 *
			 */
			private static final long serialVersionUID = -8488193495376279656L;

			public TopInfoPanel() {
			}
		}

		private class BottomInfoPanel extends JPanel {

			/**
			 *
			 */
			private static final long serialVersionUID = 658744024156732618L;

			public BottomInfoPanel() {
				JLabel label = new JLabel("sDrive (Secure Drive) - Andrea Cioni - 2017");
				label.setFont(new Font("Arial", Font.ITALIC, 10));
				add(label);
			}

		}

		private class DragAndDropAreaPanel extends JPanel {

			/**
			 *
			 */
			private static final long serialVersionUID = 1238744024156111618L;

			public DragAndDropAreaPanel() {
				setLayout(new GridBagLayout());
				setBorder(BorderFactory.createDashedBorder(DD_COLOR, 5, 10, 2, true));

				JLabel label = new JLabel("Drop your files here");
				label.setFont(new Font("Arial", Font.BOLD, 20));
				label.setForeground(DD_COLOR);
				add(label);

				setTransferHandler(new CustomTransferHandler());
			}

		}

		private class CustomTransferHandler extends TransferHandler {
			/**
			 *
			 */
			private static final long serialVersionUID = -2903576723728044023L;

			@Override
			public boolean canImport(TransferHandler.TransferSupport support) {
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					return false;
				} else {
					boolean copySupported = (COPY & support.getSourceDropActions()) == COPY;

					if (!copySupported) {
						return false;
					}

					support.setDropAction(COPY);

					return true;
				}

			}

			@SuppressWarnings("unchecked")
			@Override
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support)) {
					return false;
				}

				Transferable t = support.getTransferable();
				try {
					final List<File> filesList = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					new Thread(new Runnable() {
						@Override
						public void run() {
							launchUploadThread(filesList);
						}
					}).start();
				} catch (UnsupportedFlavorException | IOException e) {
					LOG.error("", e);
					return false;
				}

				return true;
			}

			private void launchUploadThread(List<File> filesList) {
				final JProgressDialog progressDialog = new JProgressDialog(UploadWindow.this, "Progress", "Starting...",
						0, 0);
				final StrongThread thread = new StrongThread(new Runnable() {

					@Override
					public void run() {
						try {
							if (prepareUpload(progressDialog)) {
								LOG.debug("Uploading files: {}", filesList);
								sDrive.uploadFiles(filesList, new ProgressCallback<String>() {
									@Override
									public void progressUpdate(String progress) {
										progressDialog.setText(progress);
									}
								});
								LOG.info("Uploading done!");
								JOptionPane.showMessageDialog(progressDialog, "Upload done!", "Info",
										JOptionPane.INFORMATION_MESSAGE);
							} else {
								LOG.error("No password supplied, cannot upload");
							}
						} catch (IOException e) {
							LOG.error("Upload failed", e);

						} finally {
							LOG.debug("Closing dialog");
							progressDialog.closeDialog();
						}

					}
				});

				thread.start();

				progressDialog.setSize(PROGRESS_DIALOG_WIDTH, progressDialog.getHeight());
				progressDialog.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosing(WindowEvent ev) {
						LOG.debug("Cancelling operation...");
						thread.stop();
						LOG.warn("Operation cancelled");
					}
				});
				progressDialog.showDialog();
			}

			private synchronized boolean prepareUpload(JDialog progressDialog) throws IOException {
				boolean ret = false;
				String s = null;

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
				return askForPassword(progressDialog,
						"What is the password of the secure archive? Please type it here below");
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
				int option = JOptionPane.showOptionDialog(progressDialog, panel, "Insert password",
						JOptionPane.NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, null);

				if (option == 0) {
					ret = new String(pass.getPassword());
				}

				return ret;
			}
		}

	}

}
