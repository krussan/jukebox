package se.qxx.hibernator;

import java.io.IOException;

/**
 * This hibernator is dependent on the program pmi
 * Pmi might also be dependent on hal.
 * 
 * sudo apt-get install powermanagement-interface
 * sudo apt-get install hal
 * 
 * Pmi has to be run as root so you will need to add
 * the user that this program is run as to the sudoers
 * file with the option NOPASSWD:
 * 
 * <user>   ALL=  NOPASSWD:   /usr/sbin/pmi
 * 
 * This tells the operating system that the user is
 * allowed to run pmi without specifying a password

 * 
 * @author Chris
 *
 */
public class UbuntuHibernator implements IHibernator {

	public void hibernate() throws HibernationFailedException {
		try {
			Process p  = Runtime.getRuntime().exec("sudo pmi action hibernate");
		} catch (IOException e) {
			throw new HibernationFailedException();		
		}
	}

	public void suspend() throws HibernationFailedException {
		try {
			Process p  = Runtime.getRuntime().exec("sudo pmi action suspend");
		} catch (IOException e) {
			throw new HibernationFailedException();		
		}		
	}

}

