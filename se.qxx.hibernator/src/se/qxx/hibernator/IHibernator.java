package se.qxx.hibernator;

public interface IHibernator {
	public void hibernate() throws HibernationFailedException;
	public void suspend() throws HibernationFailedException;
}
