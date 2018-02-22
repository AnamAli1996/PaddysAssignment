package bankexercise;
import java.io.*;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class FileHandling {
	private static RandomAccessFile input;
	private static RandomAccessFile output;
	static JFileChooser fc;
	static File file;
	static String fileToSaveAs = "";

	public static void openFileRead()
	{

		BankApplication.table.clear();

		fc = new JFileChooser();
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();

		} else {
		}

		//Open file
		try{
			if(file !=null)
				input = new RandomAccessFile(file, "r" );
		} // end try
		catch ( IOException ioException ){
			JOptionPane.showMessageDialog(null, "File Does Not Exist.");
		} // end catch
	} // end method openFile



	public static void openFileWrite(){
		if(fileToSaveAs!=""){
			try {
				output = new RandomAccessFile( fileToSaveAs, "rw" );
				JOptionPane.showMessageDialog(null, "Accounts saved to " + fileToSaveAs);
			} // end try
			catch ( IOException ioException ){
				JOptionPane.showMessageDialog(null, "File does not exist.");
			} // end catch
		}
		else
			saveToFileAs();
	}

	public static void saveToFileAs(){
		fc = new JFileChooser();
		int returnVal = fc.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			fileToSaveAs = file.getName();
			JOptionPane.showMessageDialog(null, "Accounts saved to " + file.getName());
		} else {
			JOptionPane.showMessageDialog(null, "Save cancelled by user");
		}

		try {
			if(fc.getSelectedFile()==null){
				JOptionPane.showMessageDialog(null, "Cancelled");
			}else
				output = new RandomAccessFile(fc.getSelectedFile(), "rw" );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void closeFile() {
		try{
			if ( input != null )
				input.close();
		}catch ( IOException ioException ){
			JOptionPane.showMessageDialog(null, "Error closing file.");//System.exit( 1 );
		} // end catch
	} // end method closeFile

	public static void readRecords(){
		RandomAccessBankAccount record = new RandomAccessBankAccount();
		try{
			while(true){
				do{
					if(input!=null)
						record.read( input );
				} while ( record.getAccountID() == 0 );

				BankAccount ba = new BankAccount(record.getAccountID(),record.getAccountNumber(), record.getFirstName(),
						record.getSurname(), record.getAccountType(), record.getBalance(), record.getOverdraft());
				Integer key = Integer.valueOf(ba.getAccountNumber().trim());
				int hash = (key%BankApplication.TABLE_SIZE);
				while(BankApplication.table.containsKey(hash)){
					hash = hash+1;
				}
				BankApplication.table.put(hash, ba);
			} // end while
		} // end try
		catch ( EOFException eofException ){
			return; // end of file was reached
		}
		catch ( IOException ioException ){
			JOptionPane.showMessageDialog(null, "Error reading file.");
			System.exit( 1 );
		} // end catch
	}

	public static void saveToFile(){
		RandomAccessBankAccount record = new RandomAccessBankAccount();
		for (Map.Entry<Integer, BankAccount> entry : BankApplication.table.entrySet()) {
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
}
