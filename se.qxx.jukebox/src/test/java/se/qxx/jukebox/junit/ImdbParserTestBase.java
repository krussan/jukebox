package se.qxx.jukebox.junit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImdbParserTestBase {

    protected String readResource(String resourceName) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(resourceName)));
        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null){
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

}
