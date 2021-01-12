package se.qxx.jukebox.settings;

import java.util.List;
import java.util.stream.Collectors;

public class ParserTest {
    private List<String> type;
    private List<ParserRegexTest> year;
    private List<ParserRegexTest> parts;
    private List<ParserRegexTest> ignored;
    private List<ParserRegexTest> season;
    private List<ParserRegexTest> episode;
    private List<String> format;
    private List<String> sound;
    private List<String> language;
    private List<String> groups;
    private List<String> other;

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public List<ParserRegexTest> getTypeAsParserRegex() {
        return this.getAsParserRegex(this.getType());
    }

    public List<ParserRegexTest> getYear() {
        return year;
    }

    public void setYear(List<ParserRegexTest> year) {
        this.year = year;
    }

    public List<ParserRegexTest> getParts() {
        return parts;
    }

    public void setParts(List<ParserRegexTest> parts) {
        this.parts = parts;
    }

    public List<ParserRegexTest> getSeason() {
        return season;
    }

    public void setSeason(List<ParserRegexTest> season) {
        this.season = season;
    }

    public List<ParserRegexTest> getIgnored() {
        return ignored;
    }

    public void setIgnored(List<ParserRegexTest> ignored) {
        this.ignored = ignored;
    }

    public List<ParserRegexTest> getEpisode() {
        return episode;
    }

    public void setEpisode(List<ParserRegexTest> episode) {
        this.episode = episode;
    }

    public List<String> getFormat() {
        return format;
    }

    public List<ParserRegexTest> getFormatAsParserRegex() {
        return this.getAsParserRegex(this.getFormat());
    }

    public void setFormat(List<String> format) {
        this.format = format;
    }

    public List<String> getSound() {
        return sound;
    }

    public void setSound(List<String> sound) {
        this.sound = sound;
    }


    public List<ParserRegexTest> getSoundAsParserRegex() {
        return this.getAsParserRegex(this.getSound());
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    public List<ParserRegexTest> getLanguageAsParserRegex() {
        return this.getAsParserRegex(this.getLanguage());
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public List<ParserRegexTest> getGroupsAsParserRegex() {
        return this.getAsParserRegex(this.getGroups());
    }

    public List<String> getOther() {
        return other;
    }

    public void setOther(List<String> other) {
        this.other = other;
    }

    public List<ParserRegexTest> getOtherAsParserRegex() {
        return this.getAsParserRegex(this.getOther());
    }

    private List<ParserRegexTest> getAsParserRegex(List<String> list) {
        return
                list.stream().map(x -> {
                    ParserRegexTest y = new ParserRegexTest();
                    y.setRegex(x);
                    y.setIsregex(false);
                    return y;
                }).collect(Collectors.toList());
    }

}
