package bankexercise;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;

public class CreateBankDialog extends JFrame {
	private static final long serialVersionUID = 1L;
	ArrayList<BankAccount> accountList;
	HashMap<Integer, BankAccount> table = new HashMap<Integer, BankAccount>();
	private static int counter = 0;
	private int accountId;



	public void put(int key, BankAccount value){
		int hash = (key%BankApplication.TABLE_SIZE);

		while(table.containsKey(key)){
			hash = hash+1;
		}
		table.put(hash, value);
	}

	// Constructor code based on that for the Create and Edit dialog classes in the Shapes exercise.

	JLabel accountNumberLabel, firstNameLabel, surnameLabel, accountTypeLabel, balanceLabel, overdraftLabel;
	JTextField accountNumberTextField, firstNameTextField, surnameTextField, accountTypeTextField, balanceTextField, overdraftTextField;
	JButton addButton, cancelButton;

	CreateBankDialog(HashMap<Integer, BankAccount> accounts) {
		super("Add Bank Details");
		table = accounts;
		setLayout(new BorderLayout());
		String[] comboTypes = {"Current", "Deposit"};
		final JComboBox<String> comboBox = new JComboBox<String>(comboTypes);

		createLabelsAndTextFields(comboBox);
		createButtons();
		addActionListeners(comboBox);

		setSize(400,800);
		pack();
		setVisible(true);

	}

	private void addActionListeners(JComboBox<String> comboBox) {
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String accountNumber = accountNumberTextField.getText();
				String surname = surnameTextField.getText();
				String firstName = firstNameTextField.getText();
				String accountType = comboBox.getSelectedItem().toString();
				accountId = ++counter;
				if (accountNumber != null && accountNumber.length()==8 && surname.length() != 0 && firstName.length() != 0) {
					try {
						boolean accNumTaken=false;
						for (Map.Entry<Integer, BankAccount> entry : table.entrySet()) {					
							if(entry.getValue().getAccountNumber().trim().equals(accountNumberTextField.getText())){
								accNumTaken=true;	 
							}
						}

						if(!accNumTaken){
							BankAccount account = new BankAccount(accountId, accountNumber, surname, firstName, accountType, 0.0, 0.0);
							int key = Integer.parseInt(account.getAccountNumber());
							put(key, account);
						}
						else{
							JOptionPane.showMessageDialog(null, "Account Number must be unique");
						}
					}
					catch (Exception ex) {
						JOptionPane.showMessageDialog(null, "Number format exception");					
					}
				}
				else JOptionPane.showMessageDialog(null, "Please make sure all fields have values, and Account Number is a unique 8 digit number");
				dispose();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

	}

	private void createButtons() {
		JPanel buttonPanel = new JPanel(new FlowLayout());
		addButton = new JButton("Add");
		cancelButton = new JButton("Cancel");

		buttonPanel.add(addButton);
		buttonPanel.add(cancelButton);

		add(buttonPanel, BorderLayout.SOUTH);
	}

	private void createLabelsAndTextFields(JComboBox<String> comboBox) {
		JPanel dataPanel = new JPanel(new MigLayout());
		accountNumberLabel = new JLabel("Account Number: ");
		accountNumberTextField = new JTextField(15);
		accountNumberTextField.setEditable(true);

		dataPanel.add(accountNumberLabel, "growx, pushx");
		dataPanel.add(accountNumberTextField, "growx, pushx, wrap");

		surnameLabel = new JLabel("Last Name: ");
		surnameTextField = new JTextField(15);
		surnameTextField.setEditable(true);

		dataPanel.add(surnameLabel, "growx, pushx");
		dataPanel.add(surnameTextField, "growx, pushx, wrap");

		firstNameLabel = new JLabel("First Name: ");
		firstNameTextField = new JTextField(15);
		firstNameTextField.setEditable(true);

		dataPanel.add(firstNameLabel, "growx, pushx");
		dataPanel.add(firstNameTextField, "growx, pushx, wrap");

		accountTypeLabel = new JLabel("Account Type: ");
		accountTypeTextField = new JTextField(5);
		accountTypeTextField.setEditable(true);

		dataPanel.add(accountTypeLabel, "growx, pushx");	
		dataPanel.add(comboBox, "growx, pushx, wrap");

		balanceLabel = new JLabel("Balance: ");
		balanceTextField = new JTextField(10);
		balanceTextField.setText("0.0");
		balanceTextField.setEditable(false);

		dataPanel.add(balanceLabel, "growx, pushx");
		dataPanel.add(balanceTextField, "growx, pushx, wrap");

		overdraftLabel = new JLabel("Overdraft: ");
		overdraftTextField = new JTextField(10);
		overdraftTextField.setText("0.0");
		overdraftTextField.setEditable(false);

		dataPanel.add(overdraftLabel, "growx, pushx");
		dataPanel.add(overdraftTextField, "growx, pushx, wrap");

		add(dataPanel, BorderLayout.CENTER);

	}



}