package se.qxx.jukebox;

public class Version {
	private int major;
	private int minor;
	
	private static final String CURRENT_VERSION = "0.20";
	
	public Version() {
		parseVersion(CURRENT_VERSION);
	}
	
	public Version(String version) {
		parseVersion(version);
	}
	
	public Version(int major, int minor) {
		this.setMajor(major);
		this.setMinor(minor);
	}
	
	private void parseVersion(String version) {
		String[] parts = version.split("\\.");
		this.setMajor(Integer.parseInt(parts[0]));
		this.setMinor(Integer.parseInt(parts[1]));
	}
	
	public int getMajor() {
		return major;
	}
	
	public void setMajor(int major) {
		this.major = major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public void setMinor(int minor) {
		this.minor = minor;
	}
	
	public String getUpgradeClazzName() {
		return String.format("se.qxx.jukebox.upgrade.Upgrade_%s_%s", major, minor);
	}
	
	public boolean isEqualTo(Version otherVersion) {
		return this.getMajor() == otherVersion.getMajor() && this.getMinor() == otherVersion.getMinor();
	
	}
	public boolean isLessThan(Version otherVersion) {
		if (this.getMajor() > otherVersion.getMajor())
			return false;
					
		return this.getMajor() < otherVersion.getMajor() || this.getMinor() < otherVersion.getMinor();
	}
	
	public String toString() {
		return String.format("%s.%s", this.getMajor(), this.getMinor());
	}
	
	public static String getCurrentVersion() {
		return CURRENT_VERSION;
	}
}
