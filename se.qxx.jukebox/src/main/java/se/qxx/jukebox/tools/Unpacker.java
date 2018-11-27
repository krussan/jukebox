package se.qxx.jukebox.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import se.qxx.jukebox.core.Log.LogType;
import se.qxx.jukebox.factories.LoggerFactory;
import se.qxx.jukebox.interfaces.IJukeboxLogger;
import se.qxx.jukebox.interfaces.IUnpacker;

public class Unpacker implements IUnpacker {
	
	private IJukeboxLogger log;
	
	@Inject
	public Unpacker(LoggerFactory loggerFactory) {
		this.setLog(loggerFactory.create(LogType.SUBS));
	}
	public IJukeboxLogger getLog() {
		return log;
	}
	public void setLog(IJukeboxLogger log) {
		this.log = log;
	}
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IUnpacker#unpackFiles(java.io.File, java.lang.String)
	 */
	@Override
	public List<File> unpackFiles(File f, String path) {
		List<File> unpackedFiles;
		this.getLog().Info(String.format("Unpacking file %s", f.getName()));
		
		if(f.getName().endsWith(".rar")) 
			unpackedFiles = unrarFile(f, path);
		else if (f.getName().endsWith(".zip"))
			unpackedFiles = unzipFile(f, path);
		else
			unpackedFiles = moveFile(f, path);

		return unpackedFiles;
	}
	
	/* (non-Javadoc)
	 * @see se.qxx.jukebox.tools.IUnpacker#moveFile(java.io.File, java.lang.String)
	 */
	@Override
	public List<File> moveFile(File f, String path) {
		this.getLog().Info(String.format("File is not an archive. Moving to temp folder :: %s", path));
		
		File targetFile = getTargetFile(f.getName(), path);
		
		try {
			FileUtils.copyFile(f, targetFile);
		} catch (IOException e) {
			this.getLog().Error(String.format("Error when copying file %s to %s", f.getName(), targetFile.getName()), e);
		}

		ArrayList<File> unpackedFiles = new ArrayList<File>();
		unpackedFiles.add(targetFile);
		
		return unpackedFiles;
	}
	
	private File getTargetFile(String filename, String path) {
		String targetName = String.format("%s/%s", path, filename);
		return new File(targetName);
	}
	
//	public List<File> unpackFiles(List<File> files, String path) {
//		ArrayList<File> unpackedFiles = new ArrayList<File>();
//		for (File f : files) {
//			File unpackedFile = unpackFile(f, path);
//			if (unpackedFile != null)
//				unpackedFiles.add(unpackedFile);
//		}
//		
//		return unpackedFiles;
//	}
	
	private List<File> unrarFile(File f, String path) {
		this.getLog().Info(String.format("File appears to be a RAR archive"));

		ArrayList<File> unpackedFiles = new ArrayList<File>();
		
		String fileList = executeCommand(
			"unrar",
			"lb",
			f.getAbsolutePath());
			
		BufferedReader sr = new BufferedReader(new StringReader(fileList));
		String entryName;
		try {
			while ((entryName = sr.readLine()) != null) {
				this.getLog().Debug(String.format("entryName :: %s", entryName));
				if (StringUtils.endsWithIgnoreCase(entryName, "srt") || StringUtils.endsWithIgnoreCase(entryName, "sub")) {   				
					File targetFile = getTargetFile(entryName, path);
					String outputPath = FilenameUtils.getFullPath(targetFile.getAbsolutePath());
					String outputFilename = outputPath + entryName;
					
					executeCommand(
						"unrar", 
						"e",
						f.getAbsolutePath(),
						entryName,
						outputPath);
					
					unpackedFiles.add(new File(outputFilename));
					
				}
			}
		} catch (IOException e) {
			this.getLog().Error(String.format("Error when unpacking sub with filename :: %s", f.getName()), e);
		}
		
		return unpackedFiles;
	}

	private String executeCommand(String command, String... args) {
		String output = StringUtils.EMPTY;
		
		String arguments = StringUtils.join(args, ' ');
		
		this.getLog().Debug(String.format("EXEC :: Executing command :: %s %s", command, arguments));
		List<String> processArguments = new ArrayList<String>();
		processArguments.add(command);
		processArguments.addAll(Arrays.asList(args));
		
		try {
			ProcessBuilder pb = new ProcessBuilder(processArguments);
			Process p = pb.start();

			InputStream stdout = p.getInputStream();
			InputStream stderr = p.getErrorStream();

			this.getLog().Debug(String.format("EXEC :: Reading from command stream"));

			output = Util.readMessageFromStream(stdout);
			
			this.getLog().Debug(String.format("EXEC :: Read %s characters from command stream", output.length()));
			p.waitFor();
			
			this.getLog().Debug(String.format("EXEC :: Process exit value :: %s", p.exitValue()));

			String errorMessage = Util.readMessageFromStream(stderr);
			
			if (p.exitValue() != 0) {
				this.getLog().Debug("EXEC :: Error when unpacking archive:");
			}

			stdout.close();
			stderr.close();

			this.getLog().Debug(errorMessage);
			
		} catch (IOException e) {			
			this.getLog().Error(String.format("Error while executing command :: %s", command), e);
		} catch (InterruptedException e) {
			this.getLog().Error(String.format("Error while executing command :: %s", command), e);
		}	
		return output;
	}
	
	private List<File> unzipFile(File f, String path) {
		this.getLog().Info(String.format("File appears to be a ZIP archive"));
		
		ArrayList<File> unpackedFiles = new ArrayList<File>();
		
		try {
			ZipInputStream zipinputstream = new ZipInputStream(new FileInputStream(f));
		
        	ZipEntry zipentry = zipinputstream.getNextEntry();
        	while (zipentry != null) 
            { 
                //for each entry to be extracted
                String entryName = zipentry.getName();
                this.getLog().Info(String.format("ZIP entry :: %s", entryName));
                
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
	                
	                unpackedFiles.add(new File(outputFilename));
                }
	            
                zipinputstream.closeEntry();
	            zipentry = zipinputstream.getNextEntry();
        		
            }//while

            zipinputstream.close();
		} catch (IOException e) {
			
			this.getLog().Error(String.format("Error when unpacking sub with filename :: %s", f.getName()), e);
		} 
		
		return unpackedFiles;
	}

	
	
}
