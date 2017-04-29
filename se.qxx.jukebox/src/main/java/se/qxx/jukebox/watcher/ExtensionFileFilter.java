package se.qxx.jukebox.watcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ExtensionFileFilter  implements java.io.FileFilter {

	private ArrayList<String> exts = new ArrayList<String>(); 
	
	public ExtensionFileFilter() {
		
	}
	
	public void addExtension(String extension) {
		exts.add(extension);
	}
	
	public void addExtensions(List<String> extensions) {
		exts.addAll(extensions);
	}
	
	public boolean accept(File f) {
		if (f.isDirectory()) {
		      return true;

	    } else if (f.isFile()) {
	      Iterator<String> it = exts.iterator();
	      while (it.hasNext()) {
	        if (StringUtils.endsWithIgnoreCase(f.getName(), it.next()))
	          return true;
	      }
	    }

	    return false;
	}

}
