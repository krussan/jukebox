package se.qxx.jukebox.tests;

import java.io.File;
import java.util.List;

import se.qxx.jukebox.builders.NFOLine;
import se.qxx.jukebox.builders.NFOScanner;

public class TestNfoScanner {
	public static void main(String[] args) {
	    String FileName = "example.nfo";
	    System.out.println(String.format("Nr of args\t\t::%s", args.length));
	    if (args.length > 0)
	        FileName = args[0];

	    NFOScanner scanner = new NFOScanner(new File(FileName));
	    List<NFOLine> lines = scanner.scan();
	    
	    for (int i = 0;i<lines.size();i++) {
	    	System.out.println(String.format("%s : %s\t%s\t\t%s", i, lines.get(i).getType().toString(), lines.get(i).getValue(), lines.get(i).getLine()));
	    }
	}
}
