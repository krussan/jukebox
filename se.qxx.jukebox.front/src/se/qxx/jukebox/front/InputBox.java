package se.qxx.jukebox.front;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;

public class InputBox extends Canvas {

	private static final long serialVersionUID = -418461412256097225L;
	private String searchString;
	
	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public InputBox() {
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		render(g);
	}
	
	private void render(Graphics g) {
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		Graphics2D g2 = (Graphics2D)g;
		
		float boxTop = RenderUtils.setupWindow(g2, d, 10.0f, 50);

		if (this.getSearchString() != null) {
			g2.setColor(new Color(240,240,240));			
			g2.setFont(new Font("Calibri", Font.BOLD, 20));	
			g.drawString(this.getSearchString(), 30, (int)boxTop + 30);
		}
	}
	

}
