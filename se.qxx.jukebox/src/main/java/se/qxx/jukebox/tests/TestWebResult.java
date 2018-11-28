package se.qxx.jukebox.tests;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.xml.bind.JAXBException;

import com.google.inject.Inject;
import com.google.inject.Injector;

import se.qxx.jukebox.core.Binder;
import se.qxx.jukebox.interfaces.IWebRetriever;
import se.qxx.jukebox.tools.WebResult;
import se.qxx.jukebox.tools.WebRetriever;

public class TestWebResult {

	private IWebRetriever webRetriever;
	
	@Inject
	public TestWebResult(IWebRetriever webRetriever) {
		this.webRetriever = webRetriever;
	}
	public static void main(String[] args) throws IOException, JAXBException {
		if (args.length > 0) {
			Injector injector = Binder.setupBindings(args);
			TestWebResult prog = injector.getInstance(TestWebResult.class);
			prog.execute(args[0]);
		}
		else {
			System.out.println("No arguments");
		}
	}

	public void execute(String url) throws IOException {
		WebResult r = this.webRetriever.getWebResult(url);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("weboutput.txt"), "UTF-8"));
		bw.write(r.getResult());
		bw.close();
		
		System.out.println("Web request made successfully. Saved to weboutput.txt");
	}

}
