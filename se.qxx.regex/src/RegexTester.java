import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.code.regexp.*;

public class RegexTester {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			showHelp();
		}
		else {
			System.out.println("0 :: " + args[0]);
			System.out.println("1 :: " + args[1]);
			System.out.println("2 :: " + args[2]);
						
			System.out.println("Arguments ok");
			try {
				if (args[0].equals("named"))
					execute(readFile(args[1]), readFile(args[2]));
				else if (args[0].equals("standard"))
					executeStandard(readFile(args[1]), readFile(args[2]));
				else
					showHelp();
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
		}
	}

	
	public static void execute(String regex, String input) throws IOException {

		printInput(regex, input);
		
		NamedPattern p = NamedPattern.compile(regex);
		NamedMatcher m = p.matcher(input);
		
		int c = 0;
		while (m.find()) {
			c++;
			System.out.println("find number :: " + String.valueOf(c));
			for (String group : p.groupNames()) {
				System.out.println("Group :: " + group);
				System.out.println(m.group(group));
				/*
				if (m.matches()) {
					System.out.println(m.group(group));
				}
				else {
					System.out.println("NO MATCH");
				}
				System.out.println("-----------------------------");
				*/
			}		
		}
		
	}

	public static void executeStandard(String regex, String input) throws IOException {

		printInput(regex, input);
		
		Pattern p = Pattern.compile(regex, Pattern.MULTILINE);
		Matcher m = p.matcher(input);

		int c = 0;
		while (m.find()) {
			c++;
			System.out.println("find number :: " + String.valueOf(c));
			for (int i = 0; i < m.groupCount(); i++) {
				System.out.println("Group :: " + i);
				System.out.println(m.group(i));
				/*if (m.matches()) {
					System.out.println(m.group(i));
				}
				else {
					System.out.println("NO MATCH");
				}*/
				System.out.println("-----------------------------");
			}		
		}
	}

	private static void printInput(String regex, String input) {
		//System.out.println("----------- INPUT ------------)");
		//System.out.println(input);
		
		System.out.println("");
		System.out.println("----------- REGEX ------------)");
		System.out.println(regex);
		System.out.println("");
	}

	public static String readFile(String filename) throws IOException {
		FileReader fr = new FileReader(filename);
		BufferedReader br = new BufferedReader(fr);
		String line;
		String input = "";
		while ((line = br.readLine()) != null) {
			input += line;
		}
		
		return input;
	}
	
	public static void showHelp() {
		System.out.println("");
		System.out.println("regextester named <regex-file> <input-file>");
		System.out.println("regextester standard <regex-file> <input-file>");
		System.out.println("");
	}
}
