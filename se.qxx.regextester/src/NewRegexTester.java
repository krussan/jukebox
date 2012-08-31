import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;

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
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

import com.google.code.regexp.NamedMatcher;
import com.google.code.regexp.NamedPattern;


public class NewRegexTester implements ActionListener {

	JButton btnExecute = new JButton("Execute");
	JButton btnExit = new JButton("Exit");
	JTextArea txtRegex = new JTextArea();
	JTextArea txtInput = new JTextArea();
	JTable table = new JTable();
	JScrollPane scrollPane = new JScrollPane();
	
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
		
		
		panel.add(btnExecute);
		btnExecute.addActionListener(this);
		panel.add(btnExit);
		btnExit.addActionListener(this);
		
		JPanel panel_1 = new JPanel();
		frmRegextester.getContentPane().add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		txtRegex.setPreferredSize(new Dimension(600, 400));
		txtRegex.setFont(new Font("Courier new", Font.PLAIN, 12));
		txtRegex.setLineWrap(true);
		//scrollPane.add(txtRegex);
		panel_1.add(txtRegex);
		
		JPanel panel_2 = new JPanel();
		frmRegextester.getContentPane().add(panel_2, BorderLayout.EAST);
		
		table.setPreferredSize(new Dimension(300, 800));
		panel_2.add(table);
		Hashtable<String, String> ht = new Hashtable<String, String>();
		
		ht.put("Test", "A2");
		ht.put("Test2", "B3");
		
		txtInput.setPreferredSize(new Dimension(600, 400));
		panel_1.add(txtInput);
	
		table.setModel(new HashtableModel(ht));
	}
	

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
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
		
		NamedPattern p = NamedPattern.compile(pattern, Pattern.CASE_INSENSITIVE);
		NamedMatcher m = p.matcher(txtInput.getText());

		Hashtable<String, String> ht = new Hashtable<String, String>();
		
		for (String groupName : p.groupNames()) {
			if (m.matches() && m.group(groupName) != null && m.group(groupName).length() > 0) {
				ht.put(groupName, m.group(groupName));
			}
			else
			{
				ht.put(String.valueOf(ht.size()), "N/A");
			}

		}
		for (int i = 0; i < m.groupCount(); i++) {
			try {
				ht.put(String.valueOf(i), m.group(i));
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
		}
		
		table.setModel(new HashtableModel(ht));
		
	}

	private class HashtableModel extends AbstractTableModel {/**
		 * 
		 */
		private static final long serialVersionUID = 1835878596666319553L;
		
		private List<SimpleEntry<String, String>> list = new ArrayList<SimpleEntry<String,String>>();
		
		public HashtableModel(Hashtable<String, String> ht) {
			ArrayList<String> keyList = new ArrayList<String>();
			keyList.addAll(Collections.list(ht.keys()));
			Collections.sort(keyList);
			
			for(String key : keyList) {
				list.add(new SimpleEntry<String, String>(key, ht.get(key)));
			}
		}
		@Override
		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 2;
		}

		@Override
		public int getRowCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			if (columnIndex==0)
				return list.get(rowIndex).getKey();
			else
				return list.get(rowIndex).getValue();
		}
		
	}
}
