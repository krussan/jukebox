package se.qxx.jukebox.junit;

import org.junit.Test;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import se.qxx.jukebox.settings.ImdbTest;
import se.qxx.jukebox.settings.ParserTest;
import se.qxx.jukebox.settings.SettingsTest;

public class TestYamlConfiguration {

    @Test
    public void TestReadSettings() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SettingsTest user = mapper.readValue(new File("settings.yaml"), SettingsTest.class);
    }

    @Test
    public void TestReadImdbSettings() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ImdbTest user = mapper.readValue(new File("imdb.yaml"), ImdbTest.class);
    }

    @Test
    public void TestReadParserSettings() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ParserTest user = mapper.readValue(new File("parser.yaml"), ParserTest.class);
    }

}
