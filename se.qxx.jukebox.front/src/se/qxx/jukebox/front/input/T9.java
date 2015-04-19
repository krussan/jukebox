package se.qxx.jukebox.front.input;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.Timer;

import org.apache.commons.lang3.StringUtils;

import se.qxx.jukebox.front.FrontSettings;
import se.qxx.jukebox.front.JukeboxFront;

public class T9 implements ActionListener {
	private final static int COMPLETED_DELAY = 2000;
	private final static int KEYCOMPLETED_DELAY = 1200;
	
	private Timer completedTimer;
	private String typedInput = StringUtils.EMPTY;
	private long timestamp;
	private int lastKey = -1;
	private int lastIndex = -1;
	
	private HashMap<Integer, char[]> keymap = new HashMap<Integer, char[]>(); 
	
	public T9() {
		 completedTimer = new Timer(COMPLETED_DELAY, this);
		 completedTimer.setActionCommand("completed");

		 JukeboxFront.log.debug(String.format("T9 :: keymap :: %s", FrontSettings.get().getT9ActiveKeymap()));
		 
		 for (int i=0;i < 10; i++) {
			 String map = FrontSettings.get().getT9key(i);
			 JukeboxFront.log.debug(String.format("T9 :: %s :: %s", i, map));
			 keymap.put(i,map.toCharArray());
		 }
	}

	public interface KeyInputCompletedListener {
		public void handleKeyInputCompletedListener(java.util.EventObject e);
	}

	private List<KeyInputCompletedListener> _listeners = new ArrayList<KeyInputCompletedListener>();
	
	public synchronized void addEventListener(KeyInputCompletedListener listener) {
		_listeners.add(listener);
	}
	
	public synchronized void removeEventListener(KeyInputCompletedListener listener) {
		_listeners.remove(listener);
	}
	
	private synchronized void fireKeyInputCompletedListener(String input) {
		T9InputCompletedEvent event = new T9InputCompletedEvent(this, input);
		Iterator<KeyInputCompletedListener> i = _listeners.iterator();

		while(i.hasNext())  {
			i.next().handleKeyInputCompletedListener(event);
		}
	}

	public void addKey(int number) {
		long newTimestamp = System.currentTimeMillis();
		char[] keys = keymap.get(number);
		
		completedTimer.start();
		
		JukeboxFront.log.debug(String.format("T9 :: Key pressed :: %s", number));
		JukeboxFront.log.debug(String.format("T9 :: Last index :: %s", lastIndex));
		JukeboxFront.log.debug(String.format("T9 :: Keys :: %s", Arrays.toString(keys)));

		if (newTimestamp - timestamp <= KEYCOMPLETED_DELAY && lastKey == number) {
			// toggle to next character
			lastIndex++;
			if (typedInput.length() > 0)
				typedInput = typedInput.substring(0, typedInput.length() - 1);			
			
			if (lastIndex == keys.length)
				lastIndex = 0;
			
			typedInput += keys[lastIndex];
		}
		else {
			// waited to long on the existing character
			// save the last character to the feed
			// start over on the next
			lastIndex = 0;
			
			typedInput += keys[lastIndex];
		}
		
		timestamp = newTimestamp;
		lastKey = number;
	}
	
	/****
	 * Occurs when the timer has waited long enough. Gets
	 * the input and triggers a type completed event
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getActionCommand() == "completed") {
			completedTimer.stop();
			fireKeyInputCompletedListener(this.getTypedInput());
			//this.setTypedInput(StringUtils.EMPTY);
		}
	}

	public String getTypedInput() {
		return typedInput;
	}

	public void setTypedInput(String typedInput) {
		this.typedInput = typedInput;
	}

}
