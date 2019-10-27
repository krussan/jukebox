package se.qxx.jukebox.builders;

import se.qxx.jukebox.settings.ParserType;

public class ParserToken {
	ParserType type;
	String originalToken;
	String resultingToken;
	int recursiveCount;
	boolean isFirst = false;
	boolean isLast = false;
	
	
	public boolean isFirst() {
		return isFirst;
	}

	public void setFirst(boolean isFirst) {
		this.isFirst = isFirst;
	}

	public boolean isLast() {
		return isLast;
	}

	public void setLast(boolean isLast) {
		this.isLast = isLast;
	}

	public ParserType getType() {
		return type;
	}

	private void setType(ParserType type) {
		this.type = type;
	}

	public String getResultingToken() {
		return resultingToken;
	}

	private void setResultingToken(String resultingToken) {
		this.resultingToken = resultingToken;
	}

	public String getOriginalToken() {
		return originalToken;
	}

	private void setOriginalToken(String originalToken) {
		this.originalToken = originalToken;
	}

	public int getRecursiveCount() {
		return recursiveCount;
	}

	private void setRecursiveCount(int recursiveCount) {
		this.recursiveCount = recursiveCount;
	}


	public ParserToken(
			ParserType type, 
			String originalToken, 
			String resultingToken, 
			int recursiveCount,
			boolean isFirst,
			boolean isLast) {
		this.setType(type);
		this.setOriginalToken(originalToken);
		this.setResultingToken(resultingToken);
		this.setRecursiveCount(recursiveCount);
		this.setFirst(isFirst);
		this.setLast(isLast);
	}
}
