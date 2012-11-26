package se.qxx.hibernator;

public class HibernatorTest {
	public static void main(String[] args) {
		IHibernator hib = HibernationFactory.Create();
		
		try {
			hib.suspend();
		} catch (HibernationFailedException e) {
			e.printStackTrace();
		}
	}
}
