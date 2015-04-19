package se.qxx.jukebox.front;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

public class RenderUtils {
	public static float setupWindow(Graphics2D g2, Dimension d, float boxTop, float height) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(3.0f));
		g2.setColor(new Color(224,172,27));

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));

    	RoundRectangle2D roundedRectangle = new RoundRectangle2D.Float(10.0f, boxTop, (float)d.getWidth() - 20, height, 20, 20);
        g2.draw(roundedRectangle);
        g2.setStroke(oldStroke);
        
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
        g2.fill(roundedRectangle);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        
        return boxTop;
	}
		
	public static void rightText(String text, int yCoord, Graphics g, int containerWidth, int margin) {
    	int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();
    	int x = containerWidth - stringLen - margin;
    	
    	g.drawString(text, x, yCoord);
	}
	
	public static void centerText(String text, int yCoord, Graphics g, int containerWidth) {
    	int stringLen = (int)g.getFontMetrics().getStringBounds(text, g).getWidth();  
        int x = containerWidth / 2 - stringLen/2;
        
        g.drawString(text, x, yCoord);  
	}

	public static int drawTextarea(Graphics g, String s, int x, int y, int width)
	{
		// FontMetrics gives us information about the width,
		// height, etc. of the current Graphics object's Font.
		FontMetrics fm = g.getFontMetrics();

		int lineHeight = fm.getHeight();

		int curX = x;
		int curY = y;

		String[] words = s.split(" ");

		for (String word : words)
		{
			// Find out thw width of the word.
			int wordWidth = fm.stringWidth(word + " ");

			// If text exceeds the width, then move to next line.
			if (curX + wordWidth >= x + width)
			{
				curY += lineHeight;
				curX = x;
			}
			
			g.drawString(word, curX, curY);

			// Move over to the right for next word.
			curX += wordWidth;
		}
		
		return curY + lineHeight;
	}
}
