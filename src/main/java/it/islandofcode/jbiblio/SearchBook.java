package it.islandofcode.jbiblio;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.db.ReadOnlyTableModel;

public class SearchBook extends JFrame implements IRemoteUpdate{

	private static final long serialVersionUID = 1L;

	public static enum AFTERSEARCH{
		NOTHING,	//0 serve per cercare un libro e ottenere le relative informazioni
		EDIT,		//1 mostra un pulsante che porta alla schermata di modifica del libro selezionato
		DELETE		//2 mostra un pulsante che rimuove il libro selezionato (dopo doppia conferma)
	};

	private JPanel contentPane;
	private JTextField TXT_ISBN;
	private JTextField TXT_title;
	private JTextField TXT_collocation;
	private JButton B_search;
	private JButton B_editSelected;
	private JButton B_removeSelected;
	private JPanel P_result;
	private JScrollPane SP_result;
	private JPanel P_command;
	
	private JTable resultTable;
	
	/**
	 * Invocami se devi mostrare tutto
	 */
	public SearchBook(AFTERSEARCH mode, GUI parent) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Logger.debug("FRAME ["+getTitle()+"] IN CHIUSURA");
				if(parent!=null)
					parent.signalFrameClosed(getTitle());
				e.getWindow().dispose();
			}
		});
		setAlwaysOnTop(true);
		setTitle("Ricerca libro");
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 878, 438);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel P_search = new JPanel();
		contentPane.add(P_search, BorderLayout.NORTH);
		P_search.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JLabel lblIsbn = new JLabel("ISBN");
		P_search.add(lblIsbn);
		
		TXT_ISBN = new JTextField();
		P_search.add(TXT_ISBN);
		TXT_ISBN.setColumns(10);
		
		JLabel lblTitolo = new JLabel("Titolo");
		P_search.add(lblTitolo);
		
		TXT_title = new JTextField();
		P_search.add(TXT_title);
		TXT_title.setColumns(15);
		
		JLabel lblCollocazione = new JLabel("Collocazione");
		P_search.add(lblCollocazione);
		
		TXT_collocation = new JTextField();
		P_search.add(TXT_collocation);
		TXT_collocation.setColumns(4);
		
		JLabel label = new JLabel("|");
		P_search.add(label);
		
		B_search = new JButton("Cerca");
		B_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		P_search.add(B_search);
		
		P_result = new JPanel();
		contentPane.add(P_result, BorderLayout.CENTER);
		P_result.setLayout(new BorderLayout(0, 0));
		
		SP_result = new JScrollPane();
		P_result.add(SP_result, BorderLayout.CENTER);
		
		P_command = new JPanel();
		contentPane.add(P_command, BorderLayout.SOUTH);
		
		B_editSelected = new JButton("Modifica libro");
		B_editSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(resultTable==null || resultTable.getSelectedRow()<0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!", JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int col = ReadOnlyTableModel.indexOfColumnByName(M, "Coll.");

					String COLL = (String) M.getValueAt(row, col);
					
					if(DBManager.checkCollocationRemoved(COLL)) {
						JOptionPane.showMessageDialog(contentPane, "Il libro scelto non è più disponibile!", "Attenzione!", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					new EditBook(EditBook.BOOKMODE.EDIT, COLL, parent);
				}
			}
		});
		P_command.add(B_editSelected);
		
		B_removeSelected = new JButton("Rimuovi libro");
		B_removeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(resultTable==null || resultTable.getSelectedRow()<0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!", JOptionPane.WARNING_MESSAGE);
				} else {
					int r = JOptionPane.showConfirmDialog(rootPane, "<html>Il libro non potrà essere più recuperato una volta rimosso.<br/>Confermi la rimozione?", "Conferma rimozione", JOptionPane.YES_NO_OPTION);
					if(r!=0) return;
					
					
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int col = ReadOnlyTableModel.indexOfColumnByName(M, "Coll.");
					int isbn = ReadOnlyTableModel.indexOfColumnByName(M, "ISBN");
					String COLL = (String) M.getValueAt(row, col);
					String ISBN = (String) M.getValueAt(row, isbn);
					
					if(DBManager.checkCollocationRemoved(COLL)) {
						JOptionPane.showMessageDialog(contentPane, "Il libro scelto è già stato rimosso!", "Attenzione!", JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					if(DBManager.checkIfBookLoaned(ISBN, COLL)) {
						r = JOptionPane.showConfirmDialog(rootPane,
								"<html>Il libro è presente in un prestito attivo.<br/>"
								+ "Se rimosso, potresti creare una situazione anomala.<br/>"
								+ "E' consigliabile risolvere prima il prestito attivo e poi procedere alla sua rimozione.<br/>"
								+ "<b>Vuoi davvero continuare?</b>",
								"Attenzione!", JOptionPane.YES_NO_OPTION);
						if (r != 0)
							return;
					}
					
					new RemoveBook(DBManager.getSpecificBook(ISBN, COLL));					
					
					/*int ret = DBManager.removeBook(COLL);
					
					if(ret<0) {
						JOptionPane.showMessageDialog(contentPane, "Non è stato possibile rimuovere il libro selezionato.", "Errore DataBase!", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					JOptionPane.showMessageDialog(contentPane, "Il libro scelto è stato rimosso!", "Operazione completata", JOptionPane.INFORMATION_MESSAGE);
					*/
				}
			}
		});
		P_command.add(B_removeSelected);
		
		JButton B_close = new JButton("Chiudi");
		B_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(parent!=null)
					parent.signalFrameClosed(getTitle());
				dispose();
			}
		});
		P_command.add(B_close);
		
		
		
		switch(mode) {
		case EDIT:
			P_command.remove(B_removeSelected);
			break;
		case DELETE:
			P_command.remove(B_editSelected);
			break;
		default: //NOTHING o altro
			P_command.remove(B_editSelected);
			P_command.remove(B_removeSelected);
		}
		
		parent.signalFrameOpened(getTitle());
		
		HttpHandler.getInstance().registerUI(REGISTER_MODE.INPUT_DATA, this);
		
		this.setVisible(true);

	}
	
	private void searchAction() {
		if(TXT_ISBN.getText().isEmpty() && TXT_title.getText().isEmpty() && TXT_collocation.getText().isEmpty()) {
			int r = JOptionPane.showConfirmDialog(rootPane, "Verranno mostrati TUTTI i libri del database, vuoi continuare?", "Ricerca senza parametri", JOptionPane.YES_NO_OPTION);
			if(r!=0) return;
			//r = JOptionPane.showConfirmDialog(rootPane, "Questa operazione potrebbe bloccare il programma per alcuni secondi, continuare?", "Conferma ricerca senza parametri", JOptionPane.YES_NO_OPTION);
			//if(r!=0) return;
			//ritorna tutto
			
			resultTable = new JTable(
					DBManager.searchBooksAsTableModel(null,null,null,true)
					);
			
		} else {
			resultTable = new JTable(
					DBManager.searchBooksAsTableModel(
							TXT_ISBN.getText().trim(),
							TXT_title.getText().trim(),
							TXT_collocation.getText().trim(),
							false
							)
					);
		}
		
		if(resultTable.getModel().getRowCount()<=0) {
			JOptionPane.showMessageDialog(contentPane, "La ricerca non ha prodotto alcun risultato.", "Attenzione", JOptionPane.WARNING_MESSAGE);
		}
		
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		SP_result.setViewportView(resultTable);
		//SP_result.repaint();
	}

	@Override
	public void appStatusNotification(STATUS status) {
		// do nothin
		
	}

	@Override
	public void receiveAppMessage(String msg) {
		Logger.debug(this.getTitle()+" RICEVE " + msg + " DA REMOTO");
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
		// do nothinh
		return null;
	}

	@Override
	public String getRegisterId() {
		return "SearchBook";
	}

}
