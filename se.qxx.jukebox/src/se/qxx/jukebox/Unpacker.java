package se.qxx.jukebox;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Unpacker {
	public static File unpackFile(File f) {
		File unpackedFile = null;
		if(f.getName().endsWith(".rar")) 
			unpackedFile = unrarFile(f);
		else if (f.getName().endsWith(".zip"))
			unpackedFile = unzipFile(f);
		else
			unpackedFile = f;

		return unpackedFile;
	}
	
	public static List<File> unpackFiles(List<File> files) {
		ArrayList<File> unpackedFiles = new ArrayList<File>();
		for (File f : files) {
			File unpackedFile = unpackFile(f);
			if (unpackedFile != null)
				unpackedFiles.add(unpackedFile);
		}
		
		return unpackedFiles;
	}
	
	private static File unrarFile(File f) {
		String fileList = executeCommand(String.format("unrar lb %s", f.getName()));
		BufferedReader sr = new BufferedReader(new StringReader(fileList));
		String entryName;
		try {
			while ((entryName = sr.readLine()) != null) {
				if (entryName.endsWith("srt") || entryName.endsWith("sub")) {   				
					String outputFilename = Util.getTempSubsName(entryName);
					executeCommand(String.format("unrar e %s %s", f.getName(), outputFilename));
					
					return new File(outputFilename);
				}
			}
		} catch (IOException e) {
			
			Log.Error(String.format("Error when unpacking sub with filename :: %s", f.getName()), Log.LogType.SUBS, e);
		}
		
		return null;
	}

	private static String executeCommand(String command) {
		StringBuilder sb = new StringBuilder();
		
		try {
			Process p = Runtime.getRuntime().exec(command);
			DataInputStream is = new DataInputStream(p.getInputStream());

			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String input;
			while ((input = rd.readLine()) != null) {
			    sb.append(input);
			}
			
		} catch (IOException e) {
			
			Log.Error(String.format("Error while executing command :: %s", command), Log.LogType.MAIN, e);
		}	
		return sb.toString();
	}
	
	private static File unzipFile(File f) {
		try {
			ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(f));
		
        	ZipEntry zipentry = zipinputstream.getNextEntry();
        	while (zipentry != null) 
            { 
                //for each entry to be extracted
                String entryName = zipentry.getName();
                System.out.println("entryname "+entryName);
                int n;
                
                if (entryName.endsWith("srt") || entryName.endsWith("sub")) {
	                FileOutputStream fileoutputstream;
	                String outputFilename = Util.getTempSubsName(entryName);
	                fileoutputstream = new FileOutputStream(outputFilename);             
	
	            	byte[] buf = new byte[1024];
	            	
	                while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
	                    fileoutputstream.write(buf, 0, n);
	
	                fileoutputstream.close(); 
	                zipinputstream.closeEntry();
	                zipinputstream.close();
	                
	                return new File(outputFilename);
                }
	            
                zipinputstream.closeEntry();
	            zipentry = zipinputstream.getNextEntry();
        		
            }//while

            zipinputstream.close();
		} catch (IOException e) {
			
			Log.Error(String.format("Error when unpacking sub with filename :: %s", f.getName()), Log.LogType.SUBS, e);
		} 
		return null;
	}

	
	
}
