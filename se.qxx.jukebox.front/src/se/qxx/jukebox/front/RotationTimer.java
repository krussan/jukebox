package se.qxx.jukebox.front;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import se.qxx.jukebox.front.model.ModelUpdatedEvent;
import se.qxx.jukebox.front.model.ModelUpdatedType;
import se.qxx.jukebox.front.model.Model.ModelUpdatedEventListener;

public class RotationWidget implements Runnable {
	final private double VELOCITY_THRESHOLD = .00002;

	private boolean enabled = false;
	private double acceleration = 0.0;
	private double currentRotation = 0.0;
	private double velocity = 0.0;
	
	List<RotationTimerListener> listeners = new ArrayList<RotationTimerListener>();
	
	public synchronized void addEventListener(RotationTimerListener listener) {
		listeners.add(listener);
	}
	
	public synchronized void removeEventListener(RotationTimerListener listener) {
		listeners.remove(listener);
	}
	
	public double getVelocity() {
		return velocity;
	}

	public void setVelocity(double velocity) {
		this.velocity = velocity;
		
		if (Math.abs(velocity) < VELOCITY_THRESHOLD) {
			if (this.isEnabled()) 
				this.disable();
			
			this.velocity = 0;
		} else if (!this.isEnabled()) {
			this.enable();
		}
	}
	
	private void fireRotationSet(double d) {
		Iterator<RotationTimerListener> i = listeners.iterator();
		
		while(i.hasNext())  {
			i.next().setRotation(d);
		}		
	}
	
	public void run() {
		long timeDiff, sleep;
		long lastTime =  System.currentTimeMillis();
		while (true) {
			long currentTime = System.currentTimeMillis();
			int ticks = (int) (currentTime - lastTime);
			
			if (this.isEnabled()) {
				if (this.getAcceleration() == 1.0) {
					fireRotationSet(currentRotation + ticks * this.getVelocity());
				} else {
					double newVelocity = this.getVelocity() * Math.pow(acceleration, ticks);
					
					if (Math.abs(newVelocity) < VELOCITY_THRESHOLD) {
						setRotation(currentRotation + distanceFromStartingVelocity(this.getVelocity(), acceleration, VELOCITY_THRESHOLD));
						this.setVelocity(0.0);
					} else {
						setRotation(currentRotation + distanceForXTicks(velocity, acceleration, ticks));
						this.setVelocity(newVelocity);
					}
				}
			}
			timeDiff = System.currentTimeMillis() - lastTime;
            sleep = DELAY - timeDiff;

            if (sleep < 0)
                sleep = 2;
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            
			lastTime = currentTime;
            
		}
	}
	
	public double getAcceleration() {
		return acceleration;
	}

	public void setAcceleration(double acceleration) {
		this.acceleration = acceleration;
	}

	public void enable() {
		this.setEnabled(true);
	}
	
	public void disable() {
		this.setEnabled(false);
	}

	public boolean isEnabled() {
		return enabled;
	}

	private void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
}

