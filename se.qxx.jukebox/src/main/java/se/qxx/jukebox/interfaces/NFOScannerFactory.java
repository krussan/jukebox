package se.qxx.jukebox.interfaces;

import java.io.File;

import se.qxx.jukebox.builders.NFOScanner;

public interface NFOScannerFactory {
	NFOScanner create(File file);
}
