package bankexercise;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class BankApplication extends JFrame {

	ArrayList<BankAccount> accountList = new ArrayList<BankAccount>();
	static HashMap<Integer, BankAccount> table = new HashMap<Integer, BankAccount>();
	final static int TABLE_SIZE = 29;
	JMenuBar menuBar;
	JMenu navigateMenu, recordsMenu, transactionsMenu, fileMenu, exitMenu;
	JMenuItem nextItem, prevItem, firstItem, lastItem, findByAccount, findBySurname, listAll;
	JMenuItem createItem, modifyItem, deleteItem, setOverdraft, setInterest;
	JMenuItem deposit, withdraw, calcInterest;
	JMenuItem open, save, saveAs;
	JMenuItem closeApp;
	private String[] imageNames = {"first.png", "prev.png", "next.png", "last.png" };
	private JButton[] itemButtons = new JButton[imageNames.length];
	String[] uiComp = {"Account ID", "Account Number", "First Name", "Surname", "Account Type", "Balance", "Overdraft"};
	Map<String, JLabel> labels = new HashMap<String, JLabel>();
	Map<String, JTextField> fields = new HashMap<String, JTextField>();
	
	JTable jTable;
	double interestRate;
	int currentItem=0;
	boolean openValues;

	public BankApplication() {	
		super("Bank Application");
		initComponents();
	}

	public void initComponents() {
		setLayout(new BorderLayout());
		createLabelsAndTextFields();
		createButtons();
		createMenus();
		addActionListeners();

	}
	
	//Method for creating buttons
	private void createButtons() {
		JPanel buttonPanel = new JPanel(new GridLayout(1, 4));
		for(int i = 0; i < imageNames.length; i++) {
			itemButtons[i] = new JButton(new ImageIcon(imageNames[i]));
			buttonPanel.add(itemButtons[i]);
		}

		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	//Method for creating labels and textfields
	private void createLabelsAndTextFields() {
		JPanel displayPanel = new JPanel(new MigLayout());

		for (String str: uiComp) {
			labels.put(str, new JLabel(str + ": "));
			if(str.equals("First Name") || str.equals("Surname")) {
				fields.put(str, new JTextField(20));
			}else {
				fields.put(str, new JTextField(15));
			}
			fields.get(str).setEditable(false);

			displayPanel.add(labels.get(str), "growx, pushx");
			displayPanel.add(fields.get(str), "growx, pushx, wrap");
		}

		add(displayPanel, BorderLayout.CENTER);

	}
	
	//Method for adding ActionListeners
	private void addActionListeners() {
		setOverdraft.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(table.get(currentItem).getAccountType().equals("Current")){
					fields.get("Overdraft").setText(JOptionPane.showInputDialog(null, "Enter new Overdraft", JOptionPane.OK_CANCEL_OPTION));
					table.get(currentItem).setOverdraft(Double.parseDouble(fields.get("Overdraft").getText()));
				}
				else
					JOptionPane.showMessageDialog(null, "Overdraft only applies to Current Accounts");

			}
		});

		ActionListener first = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveOpenValues();
				displayCurrentItems();
			}
		};


		ActionListener next = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				saveOpenValues();
				int maxKey = Collections.max(addArray());	
				if(currentItem<maxKey){
					currentItem++;
					while(!table.containsKey(currentItem)){
						currentItem++;
					}
				}
				displayDetails(currentItem);			
			}	
		};
	
		ActionListener prev = new ActionListener() {
					public void actionPerformed(ActionEvent e) {	
							saveOpenValues();
							int minKey = Collections.min(addArray());			
							if(currentItem>minKey){
								currentItem--;
								while(!table.containsKey(currentItem)){
									currentItem--;
								}
							}
							displayDetails(currentItem);				      
						}
					};
					
					
		ActionListener last = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveOpenValues();
				currentItem =TABLE_SIZE;			
				while(!table.containsKey(currentItem)){
					currentItem--;	
				}
				displayDetails(currentItem);
			}
		};
		
		itemButtons[0].addActionListener(first);
		firstItem.addActionListener(first);

		itemButtons[1].addActionListener(prev);
		prevItem.addActionListener(prev);
		
		itemButtons[2].addActionListener(next);
		nextItem.addActionListener(next);

		itemButtons[3].addActionListener(last);
		lastItem.addActionListener(last);


		deleteItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				table.remove(currentItem);
				JOptionPane.showMessageDialog(null, "Account Deleted");
				displayCurrentItems();
			}
		});

		createItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				new CreateBankDialog(table);		
			}
		});


		modifyItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				fields.get("Surname").setEditable(true);
				fields.get("First Name").setEditable(true);
				openValues = true;
			}
		});

		setInterest.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				interestRate = Double.parseDouble(JOptionPane.showInputDialog("Enter Interest Rate: (do not type the % sign)"));
			}
		});

		listAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				JFrame frame = new JFrame("TableDemo");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				String col[] = {"ID","Number","Name", "Account Type", "Balance", "Overdraft"};
				DefaultTableModel tableModel = new DefaultTableModel(col, 0);
				jTable = new JTable(tableModel);
				JScrollPane scrollPane = new JScrollPane(jTable);
				jTable.setAutoCreateRowSorter(true);
				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					Object[] objs = {
							entry.getValue().getAccountID(), 
							entry.getValue().getAccountNumber(), 
							entry.getValue().getFirstName().trim() + " " + entry.getValue().getSurname().trim(), 
							entry.getValue().getAccountType(), 
							entry.getValue().getBalance(), 
							entry.getValue().getOverdraft()};
					tableModel.addRow(objs);
				}
				frame.setSize(600,500);
				frame.add(scrollPane);
				frame.setVisible(true);			
			}
		});

		open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				FileHandling.readFile();
				displayCurrentItems();
			}
		});

		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				FileHandling.writeFile();
			}
		});

		saveAs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				FileHandling.saveFileAs();
			}
		});

		closeApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int answer = JOptionPane.showConfirmDialog(BankApplication.this, "Do you want to save before quitting?");
				if (answer == JOptionPane.YES_OPTION) {
					FileHandling.saveFileAs();
					dispose();
				}
				else if(answer == JOptionPane.NO_OPTION)
					dispose();
			}
		});	

		findBySurname.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String sName = JOptionPane.showInputDialog("Search for surname: ");
				boolean found = false;
				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					if(sName.equalsIgnoreCase((entry.getValue().getSurname().trim()))){
						found = true;
						findBy(entry);
					}
				}		
				if(found)
					JOptionPane.showMessageDialog(null, "Surname  " + sName + " found.");
				else
					JOptionPane.showMessageDialog(null, "Surname " + sName + " not found.");
			}
		});

		findByAccount.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String accNum = JOptionPane.showInputDialog("Search for account number: ");
				boolean found = false;
				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					if(accNum.equals(entry.getValue().getAccountNumber().trim())){
						found = true;
						findBy(entry);					
					}			 
				}
				if(found)
					JOptionPane.showMessageDialog(null, "Account number " + accNum + " found.");
				else
					JOptionPane.showMessageDialog(null, "Account number " + accNum + " not found.");
			}
		});

		deposit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String accNum = JOptionPane.showInputDialog("Account number to deposit into: ");
				boolean found = false;
				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					if(accNum.equals(entry.getValue().getAccountNumber().trim())){
						found = true;
						entry.getValue().setBalance(entry.getValue().getBalance() +
								Double.parseDouble(JOptionPane.showInputDialog("Account found, Enter Amount to Deposit: ")));
						displayDetails(entry.getKey());
					}
				}
				if (!found)
					JOptionPane.showMessageDialog(null, "Account number " + accNum + " not found.");
			}
		});


		withdraw.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				String accNum = JOptionPane.showInputDialog("Account number to withdraw from: ");
				String toWithdraw = JOptionPane.showInputDialog("Account found, Enter Amount to Withdraw: ");

				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					if(accNum.equals(entry.getValue().getAccountNumber().trim())){
						if(entry.getValue().getAccountType().trim().equals("Current")){
							if(Double.parseDouble(toWithdraw) > entry.getValue().getBalance() + entry.getValue().getOverdraft())
								JOptionPane.showMessageDialog(null, "Transaction exceeds overdraft limit");
							else{
								entry.getValue().setBalance(entry.getValue().getBalance() - Double.parseDouble(toWithdraw));
								displayDetails(entry.getKey());
							}
						}
						else if(entry.getValue().getAccountType().trim().equals("Deposit")){
							if(Double.parseDouble(toWithdraw) <= entry.getValue().getBalance()){
								entry.getValue().setBalance(entry.getValue().getBalance()-Double.parseDouble(toWithdraw));
								displayDetails(entry.getKey());
							}
							else
								JOptionPane.showMessageDialog(null, "Insufficient funds.");
						}
					}					
				}
			}
		});

		calcInterest.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
					if(entry.getValue().getAccountType().equals("Deposit")){
						double equation = 1 + ((interestRate)/100);
						entry.getValue().setBalance(entry.getValue().getBalance()*equation);
						JOptionPane.showMessageDialog(null, "Balances Updated");
						displayDetails(entry.getKey());
					}
				}
			}
		});		
	}

	private void saveOpenValues(){		
		if (openValues){
			fields.get("Surname").setEditable(false);
			fields.get("First Name").setEditable(false);
			table.get(currentItem).setSurname(fields.get("Surname").getText());
			table.get(currentItem).setFirstName(fields.get("First Name").getText());
		}
	}	
	
	private void findBy(Entry<Integer, BankAccount> entry) {
		fields.get("Account ID").setText(entry.getValue().getAccountID()+"");
		fields.get("Account Number").setText(entry.getValue().getAccountNumber());
		fields.get("Surname").setText(entry.getValue().getSurname());
		fields.get("First Name").setText(entry.getValue().getFirstName());
		fields.get("Account Type").setText(entry.getValue().getAccountType());
		fields.get("Balance").setText(entry.getValue().getBalance()+"");
		fields.get("Overdraft").setText(entry.getValue().getOverdraft()+"");
	}
	
	private void displayCurrentItems() {
		currentItem=0;
		do{currentItem++;
		}while(!table.containsKey(currentItem));
		displayDetails(currentItem);
	}

	private void createMenus() {
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		navigateMenu = new JMenu("Navigate");

		nextItem = new JMenuItem("Next Item");
		prevItem = new JMenuItem("Previous Item");
		firstItem = new JMenuItem("First Item");
		lastItem = new JMenuItem("Last Item");
		findByAccount = new JMenuItem("Find by Account Number");
		findBySurname = new JMenuItem("Find by Surname");
		listAll = new JMenuItem("List All Records");

		navigateMenu.add(nextItem);
		navigateMenu.add(prevItem);
		navigateMenu.add(firstItem);
		navigateMenu.add(lastItem);
		navigateMenu.add(findByAccount);
		navigateMenu.add(findBySurname);
		navigateMenu.add(listAll);

		menuBar.add(navigateMenu);

		recordsMenu = new JMenu("Records");

		createItem = new JMenuItem("Create Item");
		modifyItem = new JMenuItem("Modify Item");
		deleteItem = new JMenuItem("Delete Item");
		setOverdraft = new JMenuItem("Set Overdraft");
		setInterest = new JMenuItem("Set Interest");

		recordsMenu.add(createItem);
		recordsMenu.add(modifyItem);
		recordsMenu.add(deleteItem);
		recordsMenu.add(setOverdraft);
		recordsMenu.add(setInterest);

		menuBar.add(recordsMenu);

		transactionsMenu = new JMenu("Transactions");

		deposit = new JMenuItem("Deposit");
		withdraw = new JMenuItem("Withdraw");
		calcInterest = new JMenuItem("Calculate Interest");

		transactionsMenu.add(deposit);
		transactionsMenu.add(withdraw);
		transactionsMenu.add(calcInterest);

		menuBar.add(transactionsMenu);

		fileMenu = new JMenu("File");

		open = new JMenuItem("Open File");
		save = new JMenuItem("Save File");
		saveAs = new JMenuItem("Save As");

		fileMenu.add(open);
		fileMenu.add(save);
		fileMenu.add(saveAs);

		menuBar.add(fileMenu);

		exitMenu = new JMenu("Exit");

		closeApp = new JMenuItem("Close Application");

		exitMenu.add(closeApp);

		menuBar.add(exitMenu);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	private ArrayList<Integer> addArray() {
		ArrayList<Integer> keyList = new ArrayList<Integer>();	
		for(int i = 0; i<TABLE_SIZE; i++){
			if(table.containsKey(i))
				keyList.add(i);
		}
		return keyList;
	}

	public void displayDetails(int currentItem) {	

		fields.get("Account ID").setText(table.get(currentItem).getAccountID()+"");
		fields.get("Account Number").setText(table.get(currentItem).getAccountNumber());
		fields.get("Surname").setText(table.get(currentItem).getSurname());
		fields.get("First Name").setText(table.get(currentItem).getFirstName());
		fields.get("Account Type").setText(table.get(currentItem).getAccountType());
		fields.get("Balance").setText(table.get(currentItem).getBalance()+"");
		if(fields.get("Account Type").getText().trim().equals("Current"))
			fields.get("Overdraft").setText(table.get(currentItem).getOverdraft()+"");
		else
			fields.get("Overdraft").setText("Only applies to current accs");

	}
}

	