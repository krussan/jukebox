package se.qxx.jukebox.settings;

import java.util.List;

public class ConverterTest {
    private List<String> acceptedVideoCodecs;
    private List<String> acceptedAudioCodecs;

    public List<String> getAcceptedVideoCodecs() {
        return acceptedVideoCodecs;
    }

    public void setAcceptedVideoCodecs(List<String> acceptedVideoCodecs) {
        this.acceptedVideoCodecs = acceptedVideoCodecs;
    }

    public List<String> getAcceptedAudioCodecs() {
        return acceptedAudioCodecs;
    }

    public void setAcceptedAudioCodecs(List<String> acceptedAudioCodecs) {
        this.acceptedAudioCodecs = acceptedAudioCodecs;
    }
}
