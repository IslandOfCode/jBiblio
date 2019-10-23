package it.islandofcode.jbiblio;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.artefact.Loan;
import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;
import it.islandofcode.jbiblio.db.DBDate;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.db.ReadOnlyTableModel;
import it.islandofcode.jbiblio.settings.PropertiesException;
import it.islandofcode.jbiblio.settings.Settings;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AddLoan extends JFrame implements IRemoteUpdate{

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField TXT_ISBN;
	private JTextField TXT_collocation;
	private JScrollPane SP_book;
	private JButton B_completeLoan;
	private JButton B_removeBook;
	
	private List<Book> searched;
	private Client client;
	private Loan loan;
	private JTable resultTable;
	
	private HttpHandler HTTPH;

	/**
	 * Create the frame.
	 */
	public AddLoan(Client C, HttpHandler httph) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(HTTPH!=null)
					HTTPH.unregisterUI(AddLoan.this);
				dispose();
			}
		});
		
		if(httph!=null)
			HTTPH.registerUI(REGISTER_MODE.INPUT_DATA, this);
		
		this.client = C;
		searched = new ArrayList<Book>();
		if(loan==null) {
			String dateEnd = null;
			//try {
				//dateEnd = Loan.getEndLoanDate(Loan.getTodayDBFormat());
			int lenght = Settings.DEFAULT_DURATA_PRESTITO;
			try {
				lenght = Integer.parseInt(Settings.getValue(Settings.PROPERTIES.DURATA_PRESTITO));
			} catch (NumberFormatException | PropertiesException e1) {
				Logger.error(e1);
				JOptionPane.showMessageDialog(contentPane,
						"Durata prestito impostata a valore di default: " + Settings.DEFAULT_DURATA_PRESTITO,
						"Impossibile leggere file di impostazioni!",
						JOptionPane.ERROR_MESSAGE);
			}
			dateEnd = DBDate.todayPlus(lenght).getSQLiteDate();
				
				Logger.debug("DATAFINE:"+dateEnd);
			/*} catch (ParseException e1) {
				Logger.error(e1);
				JOptionPane.showMessageDialog(contentPane, "Data fine prestito in formato errato!", "Parse error!", JOptionPane.ERROR_MESSAGE);
				return;
			}*/
			loan = new Loan(client.getID(), DBDate.TODAY, dateEnd);
		}
		
		
		setTitle("Nuovo prestito");
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 495, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIsbn = new JLabel("ISBN:");
		lblIsbn.setBounds(10, 11, 37, 14);
		contentPane.add(lblIsbn);
		
		TXT_ISBN = new JTextField();
		TXT_ISBN.setFont(new Font("Dialog", Font.BOLD, 12));
		TXT_ISBN.setBounds(57, 8, 114, 21);
		contentPane.add(TXT_ISBN);
		TXT_ISBN.setColumns(10);
		
		JLabel lblCollocazione = new JLabel("Collocazione:");
		lblCollocazione.setHorizontalAlignment(SwingConstants.LEFT);
		lblCollocazione.setBounds(181, 10, 83, 16);
		contentPane.add(lblCollocazione);
		
		TXT_collocation = new JTextField();
		TXT_collocation.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_collocation.setBounds(274, 7, 46, 21);
		contentPane.add(TXT_collocation);
		TXT_collocation.setColumns(10);
		
		JButton B_search = new JButton("Cerca e aggiungi");
		B_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		B_search.setBounds(350, 8, 129, 20);
		contentPane.add(B_search);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 36, 469, 2);
		contentPane.add(separator);
		
		SP_book = new JScrollPane();
		SP_book.setBounds(10, 49, 469, 174);
		contentPane.add(SP_book);
		
		B_completeLoan = new JButton("Completa prestito");
		B_completeLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmLoan();
			}
		});
		B_completeLoan.setBounds(318, 235, 161, 26);
		contentPane.add(B_completeLoan);
		
		B_removeBook = new JButton("Rimuovi libro");
		B_removeBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(resultTable==null) return;
				if(resultTable.getSelectedRow()<0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!", JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					String ISBN = (String) M.getValueAt(row, 0); //suppongo che ISBN sia sempre all'inizio!
					
					//TODO qual è migliore?
					//Oddio, pare che i for-loop siano infinatamente più veloci, ma qui parliamo di due, massimo tre elementi
					searched = searched.stream().filter(item -> !item.getISBN().equals(ISBN)).collect(Collectors.toCollection(ArrayList::new));
					/*int toRemove = 0;
					for(Book B : searched) {
						if(B.getISBN().equals(ISBN))
							toRemove = searched.indexOf(B);
					}
					searched.remove(toRemove);*/
					
					if(searched.isEmpty()) {
						SP_book.setViewportView(null);
						searched = new ArrayList<Book>();
						loan.setBooks(searched);
						resultTable = null;
						return;
					}
					
					resultTable = new JTable(createModelFromList(searched));
					resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					SP_book.setViewportView(resultTable);
				}
			}
		});
		B_removeBook.setBounds(10, 235, 129, 26);
		contentPane.add(B_removeBook);
	}
	
	/**
	 * Lo metto separato perchè mi serve che 'this' faccia riferimento a questo oggetto
	 * e non all'ActionListener.
	 */
	private void confirmLoan() {
		loan.setBooks(searched);
		
		int IDloan = DBManager.addNewLoan(loan);
		
		if(IDloan<1) {
			Logger.error("Non è stato possibile creare il prestito!");
			JOptionPane.showMessageDialog(contentPane, "Impossibile creare il prestito", "ERRORE!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		loan.setID(IDloan);		
		
		ConfirmLoan CL = new ConfirmLoan(loan.getID());
		CL.setVisible(true);
		
		if(HTTPH!=null)
			HTTPH.unregisterUI(AddLoan.this);
		this.dispose();
	}
	
	private static DefaultTableModel createModelFromList(List<Book> BL) {
		
		Vector<String> columnNames = new Vector<String>();
		columnNames.add("ISBN");
		columnNames.add("Titolo");
		columnNames.add("Autore");
		columnNames.add("Collocazione");
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for(Book B : BL) {
			Vector<Object> R = new Vector<Object>();
			R.add(0, B.getISBN());
			R.add(1, B.getTitle());
			R.add(2, B.getAuthor());
			R.add(3, B.getCollocation());
			data.add(R);
		}
		
		//impedisci la modifica delle celle
		/*DefaultTableModel DTM = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int mColIndex) {
		        return false;
		      }
		};*/
		DefaultTableModel DTM = new ReadOnlyTableModel();
		
		DTM.setColumnIdentifiers(columnNames);
		DTM.setDataVector(data, columnNames);
		
		return DTM;
	}
	
	private void searchAction() {

		Book B;
		if(!TXT_ISBN.getText().trim().isEmpty())
			B = DBManager.getBookByISBN(TXT_ISBN.getText().trim());
		else if (!TXT_collocation.getText().trim().isEmpty())
			B = DBManager.getBookByCollocation(TXT_collocation.getText().trim().toUpperCase());
		else {
			JOptionPane.showMessageDialog(contentPane, "Uno dei due campi è necessario per la ricerca.", "Attenzione!", JOptionPane.WARNING_MESSAGE);	
			return;
		}
		
		if(B==null) {
			JOptionPane.showMessageDialog(contentPane, "Nessun libro corrispondente trovato!", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		if(DBManager.checkIfBookLoaned(TXT_ISBN.getText().trim(), TXT_collocation.getText().trim().toUpperCase())) {
			JOptionPane.showMessageDialog(contentPane, "Il libro scelto è già in prestito.", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		//cerca un duplicato nella lista
		Book dup = searched.stream()
				.filter(b-> (b.getISBN().equals(TXT_ISBN.getText().trim())||b.getCollocation().equals(TXT_collocation.getText().trim().toUpperCase())) )
				.findFirst()
				.orElse(null);
		if(dup!=null) {
			JOptionPane.showMessageDialog(contentPane, "Libro già inserito", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return;
		}

		searched.add(B);
		resultTable = new JTable(createModelFromList(searched));
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		SP_book.setViewportView(resultTable);
	}

	@Override
	public void appStatusNotification(STATUS status) {
		// do nothing
		
	}

	@Override
	public void receiveAppMessage(String msg) {
		//if (this.isActive() || this.isFocused()) {
			// verifica sia ISBN
			if (Pattern.matches("^(97(8|9))?\\d{9}(\\d|X)$", msg.trim())) {
				TXT_ISBN.setText(msg.trim());
				searchAction();
				Logger.debug("Lanciata ricerca con " + msg);
			} else {
				JOptionPane.showMessageDialog(contentPane, "Ricevuto valore non ISBN.", "Dati errati!",
						JOptionPane.ERROR_MESSAGE);
				Logger.warn("Ricevuta string non ISBN: " + msg);
			}
		//}
	}

	@Override
	public String receiveAndRespondAppMessage(String msg) {
		// do nothing
		return null;
	}

	@Override
	public String getRegisterId() {
		return "AddLoan";
	}
}
