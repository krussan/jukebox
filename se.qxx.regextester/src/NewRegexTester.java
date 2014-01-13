import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

public class NewRegexTester implements ActionListener {

	JButton btnExecute = new JButton("Execute");
	JButton btnExit = new JButton("Exit");
	JTextArea txtRegex = new JTextArea();
	JTextArea txtInput = new JTextArea();
	JTable table = new JTable();
	
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
		
		// -------------------------------------------- Bottom panel
		JPanel panel = new JPanel();
		frmRegextester.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			
		panel.add(btnExecute);
		btnExecute.addActionListener(this);
		panel.add(btnExit);
		btnExit.addActionListener(this);
		
		// -------------------------------------------- Left panel		
		JPanel panel_1 = new JPanel();
		frmRegextester.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		txtRegex.setPreferredSize(new Dimension(600, 400));
		txtRegex.setFont(new Font("Courier new", Font.PLAIN, 12));
		txtRegex.setLineWrap(true);
		//scrollPane.add(txtRegex);
		panel_1.add(txtRegex);
		

		JScrollPane pnlBottomLeft = new JScrollPane(txtInput, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pnlBottomLeft.setPreferredSize(new Dimension(600, 400));
		panel_1.add(pnlBottomLeft);
		
		// -------------------------------------------- Right panel				
		Hashtable<String, String> ht = new Hashtable<String, String>();		
		ht.put("Test", "A2");
		ht.put("Test2", "B3");
		table.setModel(new HashtableModel(ht));
		
		JScrollPane pnlRight = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		pnlRight.setPreferredSize(new Dimension(300, 800));

		frmRegextester.getContentPane().add(pnlRight, BorderLayout.EAST);
		

	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getID() == ActionEvent.ACTION_PERFORMED) {
			if (e.getActionCommand() == "Exit")
				System.exit(0);
			else if (e.getActionCommand() == "Execute")
				execute();
		}
	}
	
	private void execute() {
	
		String pattern = txtRegex.getText().trim();
		pattern = pattern.replace("\\\\", "\\");
		
		Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
		Matcher m = p.matcher(txtInput.getText());

		Hashtable<String, String> ht = new Hashtable<String, String>();

		int mc = 1;
		while(m.find()) {
			ht.put(String.format("Match::%s%s::", "000".substring(3 - String.valueOf(mc).length()), mc), m.group());
			for (int i = 0; i <= m.groupCount(); i++) {
				try {
					ht.put(
						String.format(
							"Match::%s%s::%s%s", 
							"000".substring(3 - String.valueOf(mc).length()), 
							mc,
							"000".substring(3 - String.valueOf(i).length()),
							i)
						, m.group(i));
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}
			}
			
			mc++;
		}
		
		table.setModel(new HashtableModel(ht));
		
	}

	private class HashtableModel extends AbstractTableModel {/**
		 * 
		 */
		private static final long serialVersionUID = 1835878596666319553L;
		
		private List<SimpleEntry<String, String>> list = new ArrayList<SimpleEntry<String,String>>();
		
		public HashtableModel(Hashtable<String, String> ht) {
			ArrayList<String> keyList = new ArrayList<String>(ht.keySet());
			Collections.sort(keyList);
			
			for(String key : keyList) {
				list.add(new SimpleEntry<String, String>(key, ht.get(key)));
			}
		}
		@Override
		public int getColumnCount() {
			
			return 2;
		}

		@Override
		public int getRowCount() {
			
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			
			if (columnIndex==0)
				return list.get(rowIndex).getKey();
			else
				return list.get(rowIndex).getValue();
		}
		
	}
}
