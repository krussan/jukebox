package se.qxx.hibernator;

public class HibernationFactory {
	public static IHibernator Create() {
		String os = System.getProperty("os.name");
		
		System.out.println(String.format("os :: %s", os));
		if (os.toLowerCase().startsWith("windows"))
			return new WindowsHibernator();
		
		if (os.toLowerCase().startsWith("linux"))
			return new UbuntuHibernator();
		
		return null;
	}
}
