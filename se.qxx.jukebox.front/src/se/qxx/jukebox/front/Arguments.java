package se.qxx.jukebox.front;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Arguments {
	private static Arguments _instance = null;
	private CommandLine cmdLine;
	private Options options;
	
	private Arguments(CommandLine line, Options opt) {
		this.setCmdLine(line);
		this.setOptions(opt);
	}
	
	public static Arguments get() {
		if (_instance == null)
			throw new NullPointerException();
		
		return _instance;		
	}
	public static CommandLine cmd() {
		if (_instance == null)
			throw new NullPointerException();
		
		return _instance.getCmdLine();
	}


	public static boolean parseCLI(String[] args)  {
		Options opt = setupOptions();
		
		CommandLineParser parser = new org.apache.commons.cli.GnuParser();
		try {
			_instance = new Arguments(parser.parse(opt, args), opt);
			
			return true;
		} catch (ParseException e) {
			// oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + e.getMessage() );
	        
	        return false;
		}	
	}

	public void printHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "runFront", options );
	}
	
	@SuppressWarnings("static-access")
	private static Options setupOptions() {
		Options opt = new Options();
		
		opt.addOption(new Option("help", "prints this message"));
		opt.addOption(OptionBuilder
				.withArgName("props-file")
				.hasArg()
				.withDescription("Sets the property file to be used for properties")
				.create("props"));
		
		opt.addOption(OptionBuilder
				.withArgName("vlc-path")
				.hasArg()
				.withDescription("Sets the path where vlc libraries are found")
				.withLongOpt("vlc-path")
				.create("lib"));
		
		opt.addOption(OptionBuilder
				.withArgName("front-port")
				.hasArg()
				.withDescription("Sets the jukebox front connection port for receiving playback calls")
				.withLongOpt("front-port")
				.create("p"));
		
		opt.addOption(OptionBuilder
				.withArgName("jukebox-server-port")
				.hasArg()
				.withDescription("Sets the jukebox server connection port")
				.withLongOpt("server-port")
				.create("sp"));
		
		opt.addOption(OptionBuilder
				.withArgName("jukebox-server-address")
				.hasArg()
				.withDescription("Sets the ip address of the jukebox server connection")
				.withLongOpt("server-ip")
				.create("sip"));
	
		opt.addOption(OptionBuilder
				.withArgName("keymap")
				.hasArg()
				.withDescription("Sets the currently active keymap for T9 input")
				.withLongOpt("keymap")
				.create("k"));
	
		return opt;
	}
	
	private CommandLine getCmdLine() {
		return this.cmdLine;
	}
	
	private void setCmdLine(CommandLine cmdLine) {
		this.cmdLine = cmdLine;
	}
	
	private void setOptions(Options opt) {
		this.options = opt;
	}
}
