package se.qxx.jukebox;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

public class ExtensionFileFilter  implements java.io.FileFilter {

	private ArrayList<String> exts = new ArrayList<String>(); 
	
	public ExtensionFileFilter() {
		
	}
	
	public void addExtension(String extension) {
		exts.add(extension);
	}
	
	public boolean accept(File f) {
		if (f.isDirectory()) {
		      return true;

	    } else if (f.isFile()) {
	      Iterator<String> it = exts.iterator();
	      while (it.hasNext()) {
	        if (f.getName().endsWith(it.next()))
	          return true;
	      }
	    }

	    return false;
	}

}
