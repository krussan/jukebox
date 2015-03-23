package se.qxx.jukebox.front;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Arguments {
	private static Arguments _instance = null;
	private CommandLine cmdLine;
	
	private Arguments(CommandLine line) {
		this.setCmdLine(line);
	}
	
	public static CommandLine get() {
		if (_instance == null)
			throw new NullPointerException();
		
		return _instance.getCmdLine();
	}


	public static boolean parseCLI(String[] args)  {
		Options opt = setupOptions();
		
		CommandLineParser parser = new org.apache.commons.cli.GnuParser();
		try {
			Arguments arg = 
			CommandLine line = parser.parse(opt, args);
			
			
			return true;
		} catch (ParseException e) {
			// oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
	        
	        return false;
		}
		
	}

	private static Options setupOptions() {
		Options opt = new Options();
		
		opt.addOption(new Option("help", "prints this message"));
		
		return opt;
	}
	
	private CommandLine getCmdLine() {
		return this.cmdLine;
	}
	
	private void setCmdLine(CommandLine cmdLine) {
		this.cmdLine = cmdLine;
	}
}
