package it.andreacioni.commons.swing;

public interface ProgressCallback<T> {
	public void progressUpdate(T progress);
}
