import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JTextArea;


public class NewRegexTester implements ActionListener {

	private JFrame frmRegextester;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NewRegexTester window = new NewRegexTester();
					window.frmRegextester.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NewRegexTester() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmRegextester = new JFrame();
		frmRegextester.setTitle("RegexTester");
		frmRegextester.setSize(1024, 768);
		frmRegextester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		frmRegextester.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		JButton btnNewButton_1 = new JButton("Execute");
		panel.add(btnNewButton_1);
		
		JButton btnNewButton = new JButton("Exit");
		btnNewButton.addActionListener(this);
		panel.add(btnNewButton);
		btnNewButton.addActionListener(this);
		JPanel panel_1 = new JPanel();
		frmRegextester.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JTextArea txtRegex = new JTextArea();
		txtRegex.setPreferredSize(new Dimension(800, 400));
		panel_1.add(txtRegex);
		
		JPanel panel_2 = new JPanel();
		frmRegextester.getContentPane().add(panel_2, BorderLayout.EAST);
		JTable table = new JTable();
		table.setPreferredSize(new Dimension(300, 800));
		panel_2.add(tree);
		
		JTextArea txtInput = new JTextArea();
		txtInput.setPreferredSize(new Dimension(800, 400));
		panel_1.add(txtInput);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		if (e.getID() == ActionEvent.ACTION_PERFORMED) {
			System.exit(0);
		}
	}

}
