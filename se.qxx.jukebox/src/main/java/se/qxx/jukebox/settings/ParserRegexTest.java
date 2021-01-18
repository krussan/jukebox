package se.qxx.jukebox.settings;

public class ParserRegexTest {
    private String regex;
    private String group;
    private String recursiveCount;
    private boolean firstToken;
    private boolean isregex = true;
    private String lookahead;
    private boolean lastToken;

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    public String getGroup() {
        return group;
    }

    public int getGroupInt() {
        return parseInt(group);
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getRecursiveCount() {
        return recursiveCount;
    }

    public int getRecursiveCountInt() {
        return parseInt(recursiveCount);
    }

    public void setRecursiveCount(String recursiveCount) {
        this.recursiveCount = recursiveCount;
    }

    public boolean isFirstToken() {
        return firstToken;
    }

    public void setFirstToken(boolean firstToken) {
        this.firstToken = firstToken;
    }

    public boolean isRegex() {
        return isregex;
    }

    public void setIsregex(boolean isregex) {
        this.isregex = isregex;
    }

    public String getLookahead() {
        return lookahead;
    }

    public int getLookaheadInt() {
        return parseInt(lookahead);
    }


    public void setLookahead(String lookahead) {
        this.lookahead = lookahead;
    }

    private int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            return 1;
        }
    }

    public boolean isLastToken() {
        return lastToken;
    }

    public void setLastToken(boolean lastToken) {
        this.lastToken = lastToken;
    }
}
