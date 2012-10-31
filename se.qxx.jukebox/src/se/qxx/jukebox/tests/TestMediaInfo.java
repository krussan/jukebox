package se.qxx.jukebox.tests;

import net.sourceforge.filebot.mediainfo.MediaInfo;

public class TestMediaInfo {

	public static void main(String[] args) throws Exception
	{
	    String FileName = "Example.ogg";
	    System.out.println(String.format("Nr of args\t\t::%s", args.length));
	    if (args.length > 0)
	        FileName = args[0];

	    //Info about the library

	    /*
	    To_Display += MediaInfo.Option_Static("Info_Version");

	    To_Display += "\r\n\r\nInfo_Parameters\r\n";
	    To_Display += MediaInfo.Option_Static("Info_Parameters");

	    To_Display += "\r\n\r\nInfo_Capacities\r\n";
	    To_Display += MediaInfo.Option_Static("Info_Capacities");

	    To_Display += "\r\n\r\nInfo_Codecs\r\n";
	    To_Display += MediaInfo.Option_Static("Info_Codecs");

	    //An example of how to use the library
*/
	    MediaInfo MI = new MediaInfo();

	    System.out.println("--------------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------------");
	    
	    System.out.println(String.format("Filename\t\t:: %s", FileName));
	    System.out.print("Open\t\t\t:: ");
	    
	    if (MI.Open(FileName)>0)
	    	System.out.println("OK");
	    else
	    	System.out.println("ERROR");

	    /*
	    To_Display += "\r\n\r\nInform with Complete=false\r\n";
	    MI.Option("Complete", "");
	    To_Display += MI.Inform();

	    To_Display += "\r\n\r\nInform with Complete=true\r\n";
	    MI.Option("Complete", "1");
	    To_Display += MI.Inform();

	    To_Display += "\r\n\r\nCustom Inform\r\n";
	    MI.Option("Inform", "General;Example : FileSize=%FileSize%");
	    To_Display += MI.Inform();

	    To_Display += "\r\n\r\nGetI with Stream=General and Parameter=2\r\n";
	    To_Display += MI.Get(MediaInfo.StreamKind.General, 0, 2, MediaInfo.InfoKind.Text);

	    To_Display += "\r\n\r\nCount_Get with StreamKind=Stream_Audio\r\n";
	    To_Display += MI.Count_Get(MediaInfo.StreamKind.Audio, -1);

	    To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"AudioCount\"\r\n";
	    To_Display += MI.Get(MediaInfo.StreamKind.General, 0, "AudioCount", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

	    To_Display += "\r\n\r\nGet with Stream=Audio and Parameter=\"StreamCount\"\r\n";
	    To_Display += MI.Get(MediaInfo.StreamKind.Audio, 0, "StreamCount", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

	    To_Display += "\r\n\r\nGet with Stream=General and Parameter=\"FileSize\"\r\n";
	    To_Display += MI.Get(MediaInfo.StreamKind.General, 0, "FileSize", MediaInfo.InfoKind.Text, MediaInfo.InfoKind.Name);

	    To_Display += "\r\n\r\nDuration";
	    To_Display += MI.get(MediaInfo.StreamKind., StreamNumber, parameterIndex)
	    To_Display += "\r\n\r\nClose\r\n";
	    */
	    
	    System.out.print("Duration\t\t:: ");
	    System.out.println(MI.Get(MediaInfo.StreamKind.General, 0, "Duration/String2"));
	    System.out.print("FrameRate\t\t:: ");
	    System.out.println(MI.Get(MediaInfo.StreamKind.Video, 0, "FrameRate"));
	    System.out.print("MovieName\t\t:: ");
	    System.out.println(MI.Get(MediaInfo.StreamKind.General, 0, "Movie name"));
	    
	    
	    System.out.println("--------------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------------");
	    System.out.println("--------------------------------------------------------------------");

	    System.out.println(MI.Inform());
	    System.out.println("");
	    System.out.println("Close");
	    MI.Close();

	}

}
