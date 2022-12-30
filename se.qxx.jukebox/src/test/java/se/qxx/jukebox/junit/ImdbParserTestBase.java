package se.qxx.jukebox.junit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

public class ImdbParserTestBase {

    protected String readResource(String resourceName) throws IOException {
        InputStream stream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resourceName));
        InputStreamReader reader = new InputStreamReader(stream);
        BufferedReader in = new BufferedReader(reader);

        StringBuilder sb = new StringBuilder();

        String line;
        while ((line = in.readLine()) != null){
            sb.append(line).append("\n");
        }

        return sb.toString();
    }

}
