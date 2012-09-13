package se.qxx.hibernator;

import org.apache.commons.lang3.StringUtils;

public class HibernationFactory {
	public static IHibernator Create() {
		String os = System.getProperty("os.name");
		
		System.out.println(String.format("os :: %s", os));
		if (StringUtils.startsWithIgnoreCase(os, "windows"))
			return new WindowsHibernator();
		
		if (StringUtils.equalsIgnoreCase(os, "ubuntu"))
			return new UbuntuHibernator();
		
		return null;
	}
}
