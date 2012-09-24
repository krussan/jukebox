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
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.Log.LogType;

public class Unpacker {
	public static File unpackFile(File f, String path) {
		File unpackedFile = null;
		Log.Info(String.format("Unpacking file %s", f.getName()), LogType.SUBS);
		
		if(f.getName().endsWith(".rar")) 
			unpackedFile = unrarFile(f, path);
		else if (f.getName().endsWith(".zip"))
			unpackedFile = unzipFile(f, path);
		else
			unpackedFile = moveFile(f, path);

		return unpackedFile;
	}
	
	public static File moveFile(File f, String path) {
		Log.Info(String.format("File is not an archive. Moving to temp folder :: %s", path), LogType.SUBS);
		
		File targetFile = getTargetFile(f.getName(), path);
		
		try {
			FileUtils.copyFile(f, targetFile);
		} catch (IOException e) {
			Log.Error(String.format("Error when copying file %s to %s", f.getName(), targetFile.getName()), LogType.SUBS, e);
		}
		
		return targetFile;
	}
	
	private static File getTargetFile(String filename, String path) {
		String targetName = String.format("%s/%s", path, filename);
		return new File(targetName);
	}
	
	public static List<File> unpackFiles(List<File> files, String path) {
		ArrayList<File> unpackedFiles = new ArrayList<File>();
		for (File f : files) {
			File unpackedFile = unpackFile(f, path);
			if (unpackedFile != null)
				unpackedFiles.add(unpackedFile);
		}
		
		return unpackedFiles;
	}
	
	private static File unrarFile(File f, String path) {
		Log.Info(String.format("File appears to be a RAR archive"), LogType.SUBS);

		String fileList = executeCommand(
			"unrar",
			"lb",
			f.getAbsolutePath());
			
		BufferedReader sr = new BufferedReader(new StringReader(fileList));
		String entryName;
		try {
			while ((entryName = sr.readLine()) != null) {
				Log.Debug(String.format("entryName :: %s", entryName), LogType.SUBS);
				if (StringUtils.endsWithIgnoreCase(entryName, "srt") || StringUtils.endsWithIgnoreCase(entryName, "sub")) {   				
					File targetFile = getTargetFile(entryName, path);
					String outputPath = FilenameUtils.getFullPath(targetFile.getAbsolutePath());
					String outputFilename = outputPath + entryName;
					
					executeCommand(
						"unrar", 
						"e",
						f.getAbsolutePath(),
						outputPath);
					
					return new File(outputFilename);
				}
			}
		} catch (IOException e) {
			Log.Error(String.format("Error when unpacking sub with filename :: %s", f.getName()), Log.LogType.SUBS, e);
		}
		
		return null;
	}

	private static String executeCommand(String command, String... args) {
		String output = StringUtils.EMPTY;
		
		Log.Debug(String.format("Executing command :: %s", command), LogType.SUBS);
		List<String> processArguments = new ArrayList<String>();
		processArguments.add(command);
		processArguments.addAll(Arrays.asList(args));
		
		try {
			ProcessBuilder pb = new ProcessBuilder(processArguments);
			Process p = pb.start();
			
			output = Util.readMessageFromStream(p.getInputStream());

			Log.Debug(String.format("Read %s characters from command stream", output.length()), LogType.SUBS);
			p.waitFor();
			
			Log.Debug(String.format("Process exit value :: %s", p.exitValue()), LogType.SUBS);

			String errorMessage = Util.readMessageFromStream(p.getErrorStream());
			
			if (p.exitValue() != 0) {
				Log.Debug("Error when unpacking archive:", LogType.SUBS);
			}

			Log.Debug(errorMessage, LogType.SUBS);
			
		} catch (IOException e) {			
			Log.Error(String.format("Error while executing command :: %s", command), Log.LogType.SUBS, e);
		} catch (InterruptedException e) {
			Log.Error(String.format("Error while executing command :: %s", command), Log.LogType.SUBS, e);
		}	
		return output;
	}
	
	private static File unzipFile(File f, String path) {
		Log.Info(String.format("File appears to be a ZIP archive"), LogType.SUBS);
		
		try {
			ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(f));
		
        	ZipEntry zipentry = zipinputstream.getNextEntry();
        	while (zipentry != null) 
            { 
                //for each entry to be extracted
                String entryName = zipentry.getName();
                Log.Info(String.format("ZIP entry :: %s", entryName), LogType.SUBS);
                
                int n;
                
                if (StringUtils.endsWithIgnoreCase(entryName, "srt") || StringUtils.endsWithIgnoreCase(entryName, "sub")) {
	                FileOutputStream fileoutputstream;
	                File targetFile = getTargetFile(entryName, path);
	                String outputFilename = targetFile.getAbsolutePath();
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
