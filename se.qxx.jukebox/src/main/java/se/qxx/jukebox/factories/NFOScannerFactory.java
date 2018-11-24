package se.qxx.jukebox.factories;

import java.io.File;

import se.qxx.jukebox.interfaces.INFOScanner;

public interface NFOScannerFactory {
	INFOScanner create(File file);
}
