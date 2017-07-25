package it.andreacioni.sdrive.test;

import org.junit.Test;

import it.andreacioni.commons.swing.JProgressDialog;

public class JProgressDialogTest {

	@Test
	public void test() {
		JProgressDialog dialog = new JProgressDialog(null, "Hello", "ABCDEFGHILMNOPQRS", 0, 0);
		dialog.showDialog();
	}

}
