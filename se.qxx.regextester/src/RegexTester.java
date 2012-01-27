import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;


public class RegexTester extends JFrame {

	public RegexTester() {
		initUI();
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	private void initUI() {
		setTitle("RegexTester");
		setSize(1000, 700);
		
		JPanel panel = new JPanel();
		
		JPanel top = new JPanel(new BorderLayout(15, 15));
		JTextArea txtRegex = new JTextArea();
		txtRegex.setPreferredSize(new Dimension(800,200));
		top.add(txtRegex);
		panel.add(top);
		
		JPanel bottom = new JPanel(new BorderLayout(30, 30));
		JTextArea txtInput = new JTextArea();
		txtInput.setPreferredSize(new Dimension(800,200));
		bottom.add(txtInput);
		panel.add(bottom);
		

		this.add(panel);
	}
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				RegexTester r = new RegexTester();
				r.setVisible(true);
			}
		});
	}
	
	
}
