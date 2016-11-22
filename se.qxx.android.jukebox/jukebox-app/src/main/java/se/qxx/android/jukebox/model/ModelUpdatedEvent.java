package se.qxx.android.jukebox.model;


public class ModelUpdatedEvent extends java.util.EventObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6575858181792885533L;

	private ModelUpdatedType type;
	
	public ModelUpdatedEvent(Object source, ModelUpdatedType type) {
		super(source);
		
		this.setType(type);
	}

	public ModelUpdatedType getType() {
		return type;
	}

	private void setType(ModelUpdatedType type) {
		this.type = type;
	}
}
