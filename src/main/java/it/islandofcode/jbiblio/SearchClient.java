package it.islandofcode.jbiblio;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.db.DBManager;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SearchClient extends JFrame {

	public static enum AFTERSEARCH {
		NOTHING, // 0 serve per cercare un cliente e ottenere le relative informazioni
		EDIT, // 1 mostra un pulsante che porta alla schermata di modifica del cliente
				// selezionato
		DELETE, // 2 mostra un pulsante che rimuove il cliente selezionato (dopo doppia
				// conferma)
		LOAN // 3 mostra la finestra per la creazione del prestito
	};

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_nome;
	private JTextField TXT_cognome;
	private JComboBox<String> CB_classe;
	private JComboBox<String> CB_sezione;
	private JScrollPane SP_result;
	private JButton B_editSelected;
	private JButton B_removeSelected;
	private JPanel P_command;

	private JTable resultTable;
	private JButton B_createLoan;

	/**
	 * Create the frame.
	 */
	public SearchClient(AFTERSEARCH mode, GUI parent) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Logger.debug("FRAME [" + getTitle() + "] IN CHIUSURA");
				parent.signalFrameClosed(getTitle());
				e.getWindow().dispose();
			}
		});
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Cerca cliente");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 700, 412);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel P_search = new JPanel();
		contentPane.add(P_search, BorderLayout.NORTH);

		JLabel lblNome = new JLabel("Nome");
		P_search.add(lblNome);

		TXT_nome = new JTextField();
		P_search.add(TXT_nome);
		TXT_nome.setColumns(12);

		JLabel lblCognome = new JLabel("Cognome");
		P_search.add(lblCognome);

		TXT_cognome = new JTextField();
		P_search.add(TXT_cognome);
		TXT_cognome.setColumns(12);

		JLabel lblClasse = new JLabel("Classe");
		P_search.add(lblClasse);

		CB_classe = new JComboBox<String>();
		CB_classe.setModel(new DefaultComboBoxModel<String>(new String[] { "-", "1", "2", "3" }));
		P_search.add(CB_classe);

		JLabel lblSezione = new JLabel("Sezione");
		P_search.add(lblSezione);

		CB_sezione = new JComboBox<String>();
		CB_sezione.setModel(new DefaultComboBoxModel<String>(new String[] { "-", "A", "B", "C", "D", "E", "F", "G" }));
		P_search.add(CB_sezione);

		JLabel label = new JLabel("|");
		P_search.add(label);

		JButton B_searchClient = new JButton("Cerca");
		B_searchClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (TXT_nome.getText().isEmpty() && TXT_cognome.getText().isEmpty() && CB_classe.getSelectedIndex() == 0
						&& CB_sezione.getSelectedIndex() == 0) {
					int r = JOptionPane.showConfirmDialog(rootPane,
							"Verranno mostrati TUTTI i clienti del database, vuoi continuare?",
							"Ricerca senza parametri", JOptionPane.YES_NO_OPTION);
					if (r != 0)
						return;
					// r = JOptionPane.showConfirmDialog(rootPane, "Questa operazione potrebbe
					// bloccare il programma per alcuni secondi, continuare?", "Conferma ricerca
					// senza parametri", JOptionPane.YES_NO_OPTION);
					// if(r!=0) return;
					// ritorna tutto

					resultTable = new JTable(DBManager.searchClientsAsTableModel(null, null, -1, null, true));

				} else {
					String tmp = (String) CB_classe.getSelectedItem();
					int classe = tmp.equals("-") ? 0 : Integer.parseInt(tmp);
					tmp = (String) CB_sezione.getSelectedItem();

					resultTable = new JTable(DBManager.searchClientsAsTableModel(TXT_nome.getText().trim(),
							TXT_cognome.getText().trim(), classe, tmp.equals("-") ? null : tmp, false));
				}

				if (resultTable.getModel().getRowCount() <= 0) {
					JOptionPane.showMessageDialog(contentPane, "La ricerca non ha prodotto alcun risultato.",
							"Attenzione", JOptionPane.WARNING_MESSAGE);
				}
				resultTable.setAutoCreateRowSorter(true);
				resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				SP_result.setViewportView(resultTable);
				// SP_result.repaint();
			}
		});
		P_search.add(B_searchClient);

		SP_result = new JScrollPane();
		contentPane.add(SP_result, BorderLayout.CENTER);

		P_command = new JPanel();
		contentPane.add(P_command, BorderLayout.SOUTH);

		B_editSelected = new JButton("Modifica cliente");
		B_editSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resultTable == null)
					return;
				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int ID = (int) M.getValueAt(row, 0); // suppongo che ISBN sia sempre all'inizio!

					if (DBManager.getClientByID(ID).isRemoved()) {
						JOptionPane.showMessageDialog(contentPane, "Il cliente scelto non è più disponibile!",
								"Attenzione!", JOptionPane.WARNING_MESSAGE);
						return;
					}

					new EditClient(EditClient.CLIENTMODE.EDIT, ID, parent);
				}
			}
		});

		B_createLoan = new JButton("Crea prestito");
		B_createLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resultTable == null)
					return;
				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int ID = (int) M.getValueAt(row, 0); // suppongo che ISBN sia sempre all'inizio!

					Client C = DBManager.getClientByID(ID);

					if (C.isRemoved()) {
						JOptionPane.showMessageDialog(contentPane, "Il cliente scelto è stato rimosso!", "Attenzione!",
								JOptionPane.WARNING_MESSAGE);
						return;
					}

					AddLoan AL = new AddLoan(C);
					AL.setVisible(true);
				}

			}
		});
		P_command.add(B_createLoan);
		P_command.add(B_editSelected);

		B_removeSelected = new JButton("Rimuovi cliente");
		B_removeSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resultTable == null)
					return;
				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int ID = (int) M.getValueAt(row, 0); // suppongo che ISBN sia sempre all'inizio!

					Client C = DBManager.getClientByID(ID);

					if (C.isRemoved()) {
						JOptionPane.showMessageDialog(contentPane, "Il cliente scelto è già stato rimosso!",
								"Attenzione!", JOptionPane.WARNING_MESSAGE);
						return;
					}

					int ret = DBManager.removeClient(C.getID());

					if (ret < 0) {
						JOptionPane.showMessageDialog(contentPane, "Non è stato possibile rimuovere il cliente.",
								"Errore DataBase!", JOptionPane.ERROR_MESSAGE);
						return;
					}

					JOptionPane.showMessageDialog(contentPane, "Il cliente scelto è stato rimosso!",
							"Operazione completata", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		P_command.add(B_removeSelected);

		JButton B_close = new JButton("Chiudi");
		B_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		P_command.add(B_close);

		switch (mode) {
		case EDIT:
			P_command.remove(B_removeSelected);
			P_command.remove(B_createLoan);
			break;
		case DELETE:
			P_command.remove(B_editSelected);
			P_command.remove(B_createLoan);
			break;
		case LOAN:
			P_command.remove(B_editSelected);
			P_command.remove(B_removeSelected);
			break;
		default: // NOTHING o altro
			P_command.remove(B_editSelected);
			P_command.remove(B_removeSelected);
			P_command.remove(B_createLoan);
		}

		parent.signalFrameOpened(getTitle());
		this.setVisible(true);
	}

}
