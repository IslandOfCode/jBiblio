package it.islandofcode.jbiblio;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.artefact.Loan;
import it.islandofcode.jbiblio.db.DBDate;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.stats.LoadingUI;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class ResolveLoan extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final String ENDLOANMSG = "<html><span style=\"color:{COLOR}; font-weight:bold\">{DATE}</span> {LATE}";
	private static final String COLOR_TOKEN = "{COLOR}";
	private static final String COLOR_LATE = "red";
	private static final String COLOR_NOT_LATE = "green";
	private static final String COLOR_LAST_DAY = "orange";
	private static final String DATE_TOKEN = "{DATE}";
	private static final String LATE_TOKEN = "{LATE}";
	
	private JPanel contentPane;
	private JTextField TXT_idLoan;
	private JSeparator separator;
	private JButton B_resolve;
	private JButton B_cancel;
	private JSeparator separator_1;
	private JButton B_clean;
	private JButton B_search;
	private JLabel TXT_nominative;
	private JLabel TXT_class;
	private JLabel lblInizioPrestito;
	private JLabel TXT_startLoan;
	private JLabel lblFinePrestito;
	private JLabel TXT_endLoan;
	private JLabel TXT_returnedLoan;
	private JScrollPane SP_loans;
	
	private JTable listOfBook;
	
	private Loan theLoan;
	private GUI parent;

	/**
	 * IDLoan deve valere >0 se si vuole che i campi vengano popolati direttamente
	 * dal database.
	 * 
	 * Un errore viene mostrato all'utente se non esiste prestito con quel codice
	 * @param parent
	 * @param IDLoan
	 */
	public ResolveLoan(GUI parent, int IDLoan, boolean viewOnly) {
		this.parent = parent;
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Logger.debug("FRAME ["+getTitle()+", ID:"+IDLoan+"] IN CHIUSURA");
				if(parent!=null)
					parent.signalFrameClosed(getTitle());
				e.getWindow().dispose();
			}
		});
		setResizable(false);
		setTitle("Risolvi prestito");
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 333);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblCodicePrestito = new JLabel("Codice prestito:");
		lblCodicePrestito.setVerticalAlignment(SwingConstants.TOP);
		lblCodicePrestito.setHorizontalAlignment(SwingConstants.LEFT);
		lblCodicePrestito.setBounds(10, 8, 98, 22);
		contentPane.add(lblCodicePrestito);
		
		TXT_idLoan = new JTextField();
		TXT_idLoan.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_idLoan.setBounds(114, 7, 129, 23);
		contentPane.add(TXT_idLoan);
		TXT_idLoan.setColumns(8);
		
		B_search = new JButton("Cerca");
		B_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String loanid = TXT_idLoan.getText().trim();
				if(loanid.isEmpty()) {
					JOptionPane.showMessageDialog(contentPane, "Un codice deve essere indicato", "Attenzione", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(DBManager.checkIfLoanReturned(Integer.parseInt(loanid)) ) {
					JOptionPane.showMessageDialog(contentPane, "Prestito già risolto.", "Attenzione", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				Loan L = DBManager.getLoan(Integer.parseInt(loanid));
				if(L==null) {
					JOptionPane.showMessageDialog(contentPane, "Prestito non esistente", "Attenzione", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				populateForm(L);
			}
		});
		B_search.setBounds(272, 8, 74, 22);
		contentPane.add(B_search);
		
		separator = new JSeparator();
		separator.setBounds(12, 37, 420, 2);
		contentPane.add(separator);
		
		B_resolve = new JButton("Risolvi");
		B_resolve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(theLoan==null || listOfBook==null) {
					JOptionPane.showMessageDialog(contentPane, "Nessun prestito selezionato!", "Attenzione", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				int r = JOptionPane.showConfirmDialog(contentPane, "Sei sicuro di voler risolvere questo prestito?", "Conferma risoluzione prestito", JOptionPane.YES_NO_OPTION);
				if(r==JOptionPane.NO_OPTION) return;
				
				//qui imposto la data di ritorno del prestito (quella odierna)
				//prima di eseguire la query sul db
				theLoan.setDateReturned(LocalDate.now().toString());
				//oltre che il flag "returned"
				theLoan.setReturned(Loan.RETURNED);
				
				if( !DBManager.resolveLoan(theLoan) ) {
					JOptionPane.showMessageDialog(contentPane, "Errore database, impossibile risolvere il prestito!", "ERRORE", JOptionPane.ERROR_MESSAGE);					
				} else {
					JOptionPane.showMessageDialog(contentPane, "Il prestito con codice ["+theLoan.getID()+"] è stato risolto.", "Operazione completata", JOptionPane.INFORMATION_MESSAGE);
				}
				cleanEverything();
				dispose();
			}
		});
		B_resolve.setFont(new Font("Dialog", Font.BOLD, 14));
		B_resolve.setBounds(10, 267, 98, 26);
		contentPane.add(B_resolve);
		
		B_cancel = new JButton("Cancella");
		B_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		B_cancel.setBounds(334, 268, 98, 26);
		contentPane.add(B_cancel);
		
		separator_1 = new JSeparator();
		separator_1.setBounds(10, 253, 422, 2);
		contentPane.add(separator_1);
		
		B_clean = new JButton("Pulisci");
		B_clean.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cleanEverything();
			}
		});
		B_clean.setBounds(358, 8, 74, 22);
		contentPane.add(B_clean);
		
		SP_loans = new JScrollPane();
		SP_loans.setBounds(10, 138, 422, 103);
		contentPane.add(SP_loans);
		
		JLabel lblNominativoCliente = new JLabel("Cliente:");
		lblNominativoCliente.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblNominativoCliente.setVerticalAlignment(SwingConstants.TOP);
		lblNominativoCliente.setBounds(10, 43, 55, 18);
		contentPane.add(lblNominativoCliente);
		
		TXT_nominative = new JLabel();
		TXT_nominative.setFont(new Font("Dialog", Font.BOLD, 12));
		TXT_nominative.setBounds(83, 41, 189, 20);
		contentPane.add(TXT_nominative);
		
		JLabel lblClasse = new JLabel("Classe:");
		lblClasse.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblClasse.setHorizontalAlignment(SwingConstants.LEFT);
		lblClasse.setBounds(284, 42, 55, 17);
		contentPane.add(lblClasse);
		
		TXT_class = new JLabel();
		TXT_class.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_class.setHorizontalAlignment(SwingConstants.CENTER);
		TXT_class.setBounds(358, 41, 48, 20);
		contentPane.add(TXT_class);
		
		lblInizioPrestito = new JLabel("Inizio Prestito:");
		lblInizioPrestito.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblInizioPrestito.setBounds(10, 73, 89, 16);
		contentPane.add(lblInizioPrestito);
		
		TXT_startLoan = new JLabel();
		TXT_startLoan.setFont(new Font("Dialog", Font.BOLD, 12));
		TXT_startLoan.setBounds(114, 72, 81, 20);
		contentPane.add(TXT_startLoan);
		
		lblFinePrestito = new JLabel("Fine Prestito:");
		lblFinePrestito.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblFinePrestito.setBounds(206, 73, 81, 16);
		contentPane.add(lblFinePrestito);
		
		TXT_endLoan = new JLabel();
		TXT_endLoan.setFont(new Font("Dialog", Font.BOLD, 12));
		TXT_endLoan.setBounds(305, 72, 127, 20);
		contentPane.add(TXT_endLoan);
		
		JLabel lblPrestitoRestituito = new JLabel("Prestito restituito:");
		lblPrestitoRestituito.setFont(new Font("Dialog", Font.ITALIC, 12));
		lblPrestitoRestituito.setBounds(10, 110, 117, 16);
		contentPane.add(lblPrestitoRestituito);
		
		TXT_returnedLoan = new JLabel();
		TXT_returnedLoan.setBounds(124, 106, 96, 20);
		contentPane.add(TXT_returnedLoan);
		
		JButton B_printReceipt = new JButton("Stampa PDF");
		B_printReceipt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String basePath = System.getProperty("user.home") + "/Desktop";
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(basePath));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Salva ricevuta prestito");				
				int res = chooser.showSaveDialog(getContentPane());
				
				if(res!=JFileChooser.APPROVE_OPTION) {
					return;
				}

				File destination = new File(chooser.getSelectedFile().getAbsolutePath());
								
				new LoadingUI(null, LoadingUI.WORKTYPE.LOAN, destination, Integer.parseInt(TXT_idLoan.getText().trim()) );
			}
		});
		B_printReceipt.setFont(new Font("Dialog", Font.PLAIN, 12));
		B_printReceipt.setBounds(164, 269, 113, 24);
		contentPane.add(B_printReceipt);
		
		if(parent!=null)
			this.parent.signalFrameOpened(getTitle());
		
		//METODI VARI
		if(IDLoan>0) {
			Loan L = DBManager.getLoan(IDLoan);
			
			if(L==null) {
				JOptionPane.showMessageDialog(contentPane, "Questo prestito non esiste", "Attenzione", JOptionPane.WARNING_MESSAGE);
				if(parent!=null)
					parent.signalFrameClosed(getTitle());
				this.dispose();
				return;
			}
			
			TXT_idLoan.setText(""+L.getID());
			populateForm(L);
			
			if(viewOnly || L.getReturned()!=0) {
				setViewOnlyMode();
			} else {
				contentPane.remove(B_printReceipt);
			}
		} else {
			contentPane.remove(B_printReceipt);
		}

		this.setVisible(true);
	}
	
	private DefaultTableModel generateTableModel(Loan L) {
		Vector<String> columns = new Vector<String>();
		columns.add("ISBN");
		columns.add("Titolo");
		columns.add("Collocazione");
		columns.add("Rimosso");

		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		Vector<Object> vector;
		for (Book B : L.getBooks()) {
			vector = new Vector<Object>();
			vector.add(0, B.getISBN());
			vector.add(1, B.getTitle());
			vector.add(2, B.getCollocation());
			vector.add(3, (B.isRemoved())?"SI":"");
			data.add(vector);
		}

		DefaultTableModel DTM = new DefaultTableModel() {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int rowIndex, int mColIndex) {
				return false;
			}
		};

		if(data.size()<0)
			return null;
		
		DTM.setColumnIdentifiers(columns);
		DTM.setDataVector(data, columns);
		return DTM;

	}
	
	private void cleanEverything() {
		TXT_nominative.setText("");
		TXT_class.setText("");
		TXT_startLoan.setText("");
		TXT_endLoan.setText("");
		TXT_idLoan.setText("");
		TXT_returnedLoan.setText("");
		SP_loans.setViewportView(null);
		listOfBook = null;
		theLoan = null;
	}
	
	private void setViewOnlyMode() {
		contentPane.remove(B_resolve);
		TXT_idLoan.setText("SOLO VISIONE");
		TXT_idLoan.setEnabled(false);
		B_search.setEnabled(false);
		B_clean.setEnabled(false);
		setTitle("Visualizza dettagli prestito");
	}

	private void populateForm(Loan L) {
		Client C = DBManager.getClientByID(L.getClient());

		//computo alcuni dati intermedi
		String end = null;
		//end = Loan.convertDateToHuman(L.getDateEnd());
		end = (new DBDate(L.getDateEnd())).getHumanDate();
		int late = LocalDate.parse(L.getDateEnd()).compareTo(LocalDate.now()); //0 uguale, >0 in orario, <0 in ritardo
		
		//creo testo per data fine
		String txt = ENDLOANMSG.replace(DATE_TOKEN, end);
		if(late==0) {
			txt = txt.replace(COLOR_TOKEN, COLOR_LAST_DAY).replace(LATE_TOKEN, "");
		} else if(late<0){
			txt = txt.replace(COLOR_TOKEN, COLOR_LATE).replace(LATE_TOKEN, "(ritardo)");
		} else {
			txt = txt.replace(COLOR_TOKEN, COLOR_NOT_LATE).replace(LATE_TOKEN, "");
		}
		
		TXT_endLoan.setText(txt);
		
		TXT_nominative.setText(C.getCognome()+", "+C.getNome());
		TXT_class.setText(C.getClasse()+C.getSezione());
		
		TXT_startLoan.setText((new DBDate(L.getDateStart())).getHumanDate());
		if(!L.getDateReturned().isEmpty())
			TXT_returnedLoan.setText( new DBDate(L.getDateReturned()).getHumanDate() );
		else
			TXT_returnedLoan.setText("<html><i>in corso");
		listOfBook = new JTable(generateTableModel(L));
		
		listOfBook.setEnabled(false);
		SP_loans.setViewportView(listOfBook);
		
		theLoan = L;
	}
}
