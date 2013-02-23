import java.io.File;

import se.qxx.jukebox.domain.JukeboxDomain.*;
import se.qxx.daogenerator.DaoGenerator;
import se.qxx.daogenerator.Schema;


public class JukeboxDaoGenerator {
	private static final String jukeboxDomain = "se.qxx.jukebox.domain.JukeboxDomain";
    public static void main(String[] args) throws Exception {
    	if (args.length > 0) {
    		File f = new File(args[0]);
    		if (f.isDirectory() && f.exists()) {
		        Schema schema = new Schema(3, "se.qxx.jukebox.domain.dao");
		        schema.setSimpleFilename(true);

		        schema.addProtobufEntity(String.format("%s.Movie", jukeboxDomain));
//		        schema.addProtobufEntity(Identifier.class.getName());
//		        schema.addProtobufEntity(Rating.class.getName());
		        schema.addProtobufEntity(String.format("%s.Season", jukeboxDomain));		        
		        schema.addProtobufEntity(String.format("%s.Subtitle", jukeboxDomain));
		        schema.addProtobufEntity(String.format("%s.Media", jukeboxDomain));
		
		        new DaoGenerator().generateAll(schema, args[0]);
		        
//		        return 0;
    		}
    		else {
    			System.out.println(String.format("%s is not an existing directory", args[0]));
//    			return -1;
    		}
    	}
    	else {
    		System.out.println(String.format("An output directory must be supplied"));
//    		return -1;
    	}
    }

}
