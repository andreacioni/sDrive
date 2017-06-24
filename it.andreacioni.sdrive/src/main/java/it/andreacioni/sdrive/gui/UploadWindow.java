package it.andreacioni.sdrive.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.TransferHandler;

import it.andreacioni.sdrive.utils.ImageUtils;

public class UploadWindow extends JFrame {
	
	private static final int WIDTH = 400;
	
	private static final int HEIGHT = 400;
	
	public UploadWindow() {
		setTitle("sDrive");
		setIconImage(ImageUtils.createImage("folder.gif", "title icon"));
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setSize(new Dimension(WIDTH, HEIGHT));
		setMaximumSize(new Dimension(WIDTH, HEIGHT));
		setEnabled(true);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		UploadPanel panel = new UploadPanel();
		
		add(panel);
	}

	private class UploadPanel extends JPanel {
		/**
		 * 
		 */
		private static final long serialVersionUID = -7958340744297357525L;
		
		private TransferHandler handler;
		
		public UploadPanel() {
			setBackground(Color.BLACK);
			prepareDragAndDropArea();
		}

		private void prepareDragAndDropArea() {
			handler = new CustomTransferHandler();
			setTransferHandler(handler);
		}
		
		private class CustomTransferHandler extends TransferHandler {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2903576723728044023L;

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

	        public boolean importData(TransferHandler.TransferSupport support) {
	            if (!canImport(support)) {
	                return false;
	            }
	            
	            Transferable t = support.getTransferable();

	            try {
	                List<File> l = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
	            } catch (UnsupportedFlavorException e) {
	            	e.printStackTrace();
	                return false;
	            } catch (IOException e) {
	            	e.printStackTrace();
	                return false;
	            }

	            return true;
	        }
		}
	}
	
	
}
