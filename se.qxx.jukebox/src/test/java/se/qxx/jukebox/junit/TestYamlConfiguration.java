package se.qxx.jukebox.junit;

import org.junit.Test;
import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import se.qxx.jukebox.settings.ImdbTest;
import se.qxx.jukebox.settings.SettingsTest;

public class TestYamlConfiguration {

    @Test
    public void TestReadSettings() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        SettingsTest user = mapper.readValue(new File("settings.yaml"), SettingsTest.class);
        System.out.println(ReflectionToStringBuilder.toString(user,ToStringStyle.MULTI_LINE_STYLE));
    }

    @Test
    public void TestReadImdbSettings() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ImdbTest user = mapper.readValue(new File("imdb.yaml"), ImdbTest.class);
        System.out.println(ReflectionToStringBuilder.toString(user,ToStringStyle.MULTI_LINE_STYLE));
    }
}
