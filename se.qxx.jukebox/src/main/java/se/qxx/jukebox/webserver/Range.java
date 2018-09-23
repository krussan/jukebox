package se.qxx.jukebox.webserver;

public class Range {

	private long startFrom = 0;
	private long endAt = -1;
	private long fileLength = 0;
	
	private Range() {
		
	}
	
	public static Range parse(String range, long fileLength) {
		Range r = new Range();
		r.setFileLength(fileLength);
		
		if (range != null) {
            if (range.startsWith("bytes=")) {
                range = range.substring("bytes=".length());
                int minus = range.indexOf('-');
                try {
                    if (minus > 0) {
                        r.setStartFrom(Long.parseLong(range.substring(0, minus)));
                        r.setEndAt(Long.parseLong(range.substring(minus + 1)));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
		
		if (r.getEndAt() < 0)
			r.setEndAt(fileLength - 1);
		
		return r; 
	}

	public long getLength() {
		long length = this.getEndAt() - this.getStartFrom() + 1;
        if (length < 0)
            length = 0;
       
		return length; 
	}
	
	public String getContentRange() {
		return String.format("bytes %s-%s/%s", 
				this.getStartFrom(), 
				this.getEndAt(),
				this.getFileLength());
	}

	public long getStartFrom() {
		return startFrom;
	}

	public void setStartFrom(long startFrom) {
		this.startFrom = startFrom;
	}

	public long getEndAt() {
		return endAt;
	}

	public void setEndAt(long endAt) {
		this.endAt = endAt;
	}

	public long getFileLength() {
		return fileLength;
	}

	public void setFileLength(long fileLength) {
		this.fileLength = fileLength;
	}
	
}
