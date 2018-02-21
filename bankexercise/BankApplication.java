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
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

public class BankApplication extends JFrame {

	ArrayList<BankAccount> accountList = new ArrayList<BankAccount>();
	static HashMap<Integer, BankAccount> table = new HashMap<Integer, BankAccount>();
	private final static int TABLE_SIZE = 29;
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
	static JFileChooser fc;
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
		addActionListers();

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
			fields.put(str, new JTextField(15));
			fields.get(str).setEditable(false);

			displayPanel.add(labels.get(str), "growx, pushx");
			displayPanel.add(fields.get(str), "growx, pushx, wrap");
		}

		add(displayPanel, BorderLayout.CENTER);

	}
	
	//Method for adding ActionListeners
	private void addActionListers() {
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
				currentItem=0;
				while(!table.containsKey(currentItem)){
					currentItem++;
				}
				displayDetails(currentItem);
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
				int minKey = Collections.max(addArray());
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

		itemButtons[2].addActionListener(next);
		nextItem.addActionListener(next);

		itemButtons[1].addActionListener(prev);
		prevItem.addActionListener(prev);

		itemButtons[0].addActionListener(first);
		firstItem.addActionListener(first);

		itemButtons[3].addActionListener(last);
		lastItem.addActionListener(last);


		deleteItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				table.remove(currentItem);
				JOptionPane.showMessageDialog(null, "Account Deleted");


				currentItem=0;
				while(!table.containsKey(currentItem)){
					currentItem++;
				}
				displayDetails(currentItem);

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
				JPanel pan = new JPanel();

				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				String col[] = {"ID","Number","Name", "Account Type", "Balance", "Overdraft"};

				DefaultTableModel tableModel = new DefaultTableModel(col, 0);
				jTable = new JTable(tableModel);
				JScrollPane scrollPane = new JScrollPane(jTable);
				jTable.setAutoCreateRowSorter(true);

				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {


					Object[] objs = {entry.getValue().getAccountID(), entry.getValue().getAccountNumber(), 
							entry.getValue().getFirstName().trim() + " " + entry.getValue().getSurname().trim(), 
							entry.getValue().getAccountType(), entry.getValue().getBalance(), 
							entry.getValue().getOverdraft()};

					tableModel.addRow(objs);
				}
				frame.setSize(600,500);
				frame.add(scrollPane);
				//frame.pack();
				frame.setVisible(true);			
			}
		});

		open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				readFile();
				currentItem=0;
				while(!table.containsKey(currentItem)){
					currentItem++;
				}
				displayDetails(currentItem);
			}
		});

		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				writeFile();
			}
		});

		saveAs.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				saveFileAs();
			}
		});

		closeApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int answer = JOptionPane.showConfirmDialog(BankApplication.this, "Do you want to save before quitting?");
				if (answer == JOptionPane.YES_OPTION) {
					saveFileAs();
					dispose();
				}
				else if(answer == JOptionPane.NO_OPTION)
					dispose();
				else if(answer==0)
					;



			}
		});	

		findBySurname.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){

				String sName = JOptionPane.showInputDialog("Search for surname: ");
				boolean found = false;

				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {

					if(sName.equalsIgnoreCase((entry.getValue().getSurname().trim()))){
						found = true;
						fields.get("Account ID").setText(entry.getValue().getAccountID()+"");
						fields.get("Account Number").setText(entry.getValue().getAccountNumber());
						fields.get("Surname").setText(entry.getValue().getSurname());
						fields.get("First Name").setText(entry.getValue().getFirstName());
						fields.get("Account Type").setText(entry.getValue().getAccountType());
						fields.get("Balance").setText(entry.getValue().getBalance()+"");
						fields.get("Overdraft").setText(entry.getValue().getOverdraft()+"");
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
						fields.get("Account ID").setText(entry.getValue().getAccountID()+"");
						fields.get("Account Number").setText(entry.getValue().getAccountNumber());
						fields.get("Surname").setText(entry.getValue().getSurname());
						fields.get("First Name").setText(entry.getValue().getFirstName());
						fields.get("Account Type").setText(entry.getValue().getAccountType());
						fields.get("Balance").setText(entry.getValue().getBalance()+"");
						fields.get("Overdraft").setText(entry.getValue().getOverdraft()+"");						

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
						String toDeposit = JOptionPane.showInputDialog("Account found, Enter Amount to Deposit: ");
						entry.getValue().setBalance(entry.getValue().getBalance() + Double.parseDouble(toDeposit));
						displayDetails(entry.getKey());
						//balanceTextField.setText(entry.getValue().getBalance()+"");
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
				boolean found;

				for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {


					if(accNum.equals(entry.getValue().getAccountNumber().trim())){

						found = true;

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
						//System.out.println(equation);
						JOptionPane.showMessageDialog(null, "Balances Updated");
						displayDetails(entry.getKey());
					}
				}
			}
		});		

	}

	public void saveOpenValues(){		
		if (openValues){
			fields.get("Surname").setEditable(false);
			fields.get("First Name").setEditable(false);

			table.get(currentItem).setSurname(fields.get("Surname").getText());
			table.get(currentItem).setFirstName(fields.get("First Name").getText());
		}
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

	private static RandomAccessFile input;
	private static RandomAccessFile output;
	private static final int NUMBER_RECORDS = 100;


	public static void openFileRead()
	{

		table.clear();

		fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

		} else {
		}


		try // open file
		{
			if(fc.getSelectedFile()!=null)
				input = new RandomAccessFile( fc.getSelectedFile(), "r" );
		} // end try
		catch ( IOException ioException )
		{
			JOptionPane.showMessageDialog(null, "File Does Not Exist.");
		} // end catch

	} // end method openFile

	static String fileToSaveAs = "";

	public static void openFileWrite()
	{
		if(fileToSaveAs!=""){
			try // open file
			{
				output = new RandomAccessFile( fileToSaveAs, "rw" );
				JOptionPane.showMessageDialog(null, "Accounts saved to " + fileToSaveAs);
			} // end try
			catch ( IOException ioException )
			{
				JOptionPane.showMessageDialog(null, "File does not exist.");
			} // end catch
		}
		else
			saveToFileAs();
	}

	public static void saveToFileAs()
	{

		fc = new JFileChooser();

		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			fileToSaveAs = file.getName();
			JOptionPane.showMessageDialog(null, "Accounts saved to " + file.getName());
		} else {
			JOptionPane.showMessageDialog(null, "Save cancelled by user");
		}


		try {
			if(fc.getSelectedFile()==null){
				JOptionPane.showMessageDialog(null, "Cancelled");
			}
			else
				output = new RandomAccessFile(fc.getSelectedFile(), "rw" );
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}

	public static void closeFile() 
	{
		try // close file and exit
		{
			if ( input != null )
				input.close();
		} // end try
		catch ( IOException ioException )
		{

			JOptionPane.showMessageDialog(null, "Error closing file.");//System.exit( 1 );
		} // end catch
	} // end method closeFile

	public static void readRecords()
	{

		RandomAccessBankAccount record = new RandomAccessBankAccount();



		try // read a record and display
		{
			while ( true )
			{
				do
				{
					if(input!=null)
						record.read( input );
				} while ( record.getAccountID() == 0 );



				BankAccount ba = new BankAccount(record.getAccountID(), record.getAccountNumber(), record.getFirstName(),
						record.getSurname(), record.getAccountType(), record.getBalance(), record.getOverdraft());


				Integer key = Integer.valueOf(ba.getAccountNumber().trim());

				int hash = (key%TABLE_SIZE);


				while(table.containsKey(hash)){

					hash = hash+1;
				}

				table.put(hash, ba);


			} // end while
		} // end try
		catch ( EOFException eofException ) // close file
		{
			return; // end of file was reached
		} // end catch
		catch ( IOException ioException )
		{
			JOptionPane.showMessageDialog(null, "Error reading file.");
			System.exit( 1 );
		} // end catch
	}

	public static void saveToFile(){


		RandomAccessBankAccount record = new RandomAccessBankAccount();

		Scanner input = new Scanner( System.in );


		for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {
			record.setAccountID(entry.getValue().getAccountID());
			record.setAccountNumber(entry.getValue().getAccountNumber());
			record.setFirstName(entry.getValue().getFirstName());
			record.setSurname(entry.getValue().getSurname());
			record.setAccountType(entry.getValue().getAccountType());
			record.setBalance(entry.getValue().getBalance());
			record.setOverdraft(entry.getValue().getOverdraft());

			if(output!=null){

				try {
					record.write( output );
				} catch (IOException u) {
					u.printStackTrace();
				}
			}

		}


	}

	public static void writeFile(){
		openFileWrite();
		saveToFile();
		//addRecords();
		closeFile();
	}

	public static void saveFileAs(){
		saveToFileAs();
		saveToFile();	
		closeFile();
	}

	public static void readFile(){
		openFileRead();
		readRecords();
		closeFile();		
	}

	public void put(int key, BankAccount value){
		int hash = (key%TABLE_SIZE);

		while(table.containsKey(key)){
			hash = hash+1;

		}
		table.put(hash, value);

	}

	public static void main(String[] args) {
		BankApplication ba = new BankApplication();
		ba.setSize(1200,400);
		ba.pack();
		ba.setVisible(true);
	}


}




/*
The task for your second assignment is to construct a system that will allow users to define a data structure representing a collection of records that can be displayed both by means of a dialog that can be scrolled through and by means of a table to give an overall view of the collection contents. 
The user should be able to carry out tasks such as adding records to the collection, modifying the contents of records, and deleting records from the collection, as well as being able to save and retrieve the contents of the collection to and from external random access files.
The records in the collection will represent bank account records with the following fields:

AccountID (this will be an integer unique to a particular account and 
will be automatically generated when a new account record is created)

AccountNumber (this will be a string of eight digits and should also 
be unique - you will need to check for this when creating a new record)

Surname (this will be a string of length 20)

FirstName (this will be a string of length 20)

AccountType (this will have two possible options - "Current " and 
"Deposit" - and again will be selected from a drop down list when 
entering a record)

Balance (this will a real number which will be initialised to 0.0 
and can be increased or decreased by means of transactions)

Overdraft (this will be a real number which will be initialised 
to 0.0 but can be updated by means of a dialog - it only applies 
to current accounts)

You may consider whether you might need more than one class to deal with bank accounts.
The system should be menu-driven, with the following menu options:

Navigate: First, Last, Next, Previous, Find By Account Number 
(allows you to find a record by account number entered via a 
dialog box), Find By Surname (allows you to find a record by 
surname entered via a dialog box),List All (displays the 
contents of the collection as a dialog containing a JTable)

Records: Create, Modify, Delete, Set Overdraft (this should 
use a dialog to allow you to set or update the overdraft for 
a current account), Set Interest Rate (this should allow you 
to set the interest rate for deposit accounts by means of a 
dialog)

Transactions: Deposit, Withdraw (these should use dialogs which
allow you to specify an account number and the amount to withdraw
or deposit, and should check that a withdrawal would not cause
the overdraft limit for a current account to be exceeded, or be 
greater than the balance in a deposit account, before the balance 
is updated), Calculate Interest (this calculates the interest rate 
for all deposit accounts and updates the balances)

File: Open, Save, Save As (these should use JFileChooser dialogs. 
The random access file should be able to hold 25 records. The position 
in which a record is stored and retrieved will be determined by its account 
number by means of a hashing procedure, with a standard method being used when 
dealing with possible hash collisions)

Exit Application (this should make sure that the collection is saved - or that 
the user is given the opportunity to save the collection - before the application closes)

When presenting the results in a JTable for the List All option, the records should be sortable on all fields, but not editable (changing the records or adding and deleting records can only be done through the main dialog).
For all menu options in your program, you should perform whatever validation, error-checking and exception-handling you consider to be necessary.
The programs Person.java and PersonApplication.java (from OOSD2) and TableDemo.java may be of use to you in constructing your interfaces. The set of Java programs used to create, edit and modify random access files will also provide you with a basis for your submission.

 */