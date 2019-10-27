package it.islandofcode.jbiblio;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;
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
import java.util.regex.Pattern;
import java.awt.Font;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;

public class SearchLoan extends JFrame implements IRemoteUpdate{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_nominativo;
	private JComboBox<String> CB_classe;
	private JComboBox<String> CB_sezione;
	private JScrollPane SP_result;
	private JPanel P_command;
	
	private JTable resultTable;
	
	private GUI parent;
	private JLabel lblIsbn;
	private JTextField TXT_ISBN;
	private JLabel lblTitolo;
	private JTextField TXT_title;
	private JLabel lblCollocazione;
	private JTextField TXT_collocation;
	private JButton B_view;
	private JButton B_resolve;
	private JPanel P_search_control;
	private JPanel P_search_command_wrapper;
	private JCheckBox CBX_resolved;
	private JLabel lblSez;
	private JPanel P_search_command;
		
	/**
	 * Create the frame.
	 */
	public SearchLoan(GUI p) {
		
		this.parent = p;
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Logger.debug("FRAME ["+getTitle()+"] IN CHIUSURA");
				parent.signalFrameClosed(getTitle());
				HttpHandler.getInstance().unregisterUI(SearchLoan.this);
				e.getWindow().dispose();
			}
		});
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Cerca prestiti");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 683, 412);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel P_search_wrapper = new JPanel();
		contentPane.add(P_search_wrapper, BorderLayout.NORTH);
		P_search_wrapper.setPreferredSize(new Dimension(contentPane.getWidth(),60));
		P_search_wrapper.setLayout(new BorderLayout(0, 0));
		
		P_search_control = new JPanel();
		FlowLayout flowLayout = (FlowLayout) P_search_control.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		P_search_wrapper.add(P_search_control, BorderLayout.CENTER);
		
		lblIsbn = new JLabel("ISBN");
		P_search_control.add(lblIsbn);
		
		TXT_ISBN = new JTextField();
		P_search_control.add(TXT_ISBN);
		TXT_ISBN.setFont(new Font("Tahoma", Font.BOLD, 11));
		TXT_ISBN.setColumns(9);
		
		lblTitolo = new JLabel("Titolo");
		P_search_control.add(lblTitolo);
		
		TXT_title = new JTextField();
		P_search_control.add(TXT_title);
		TXT_title.setColumns(15);
		
		lblCollocazione = new JLabel("Coll.");
		P_search_control.add(lblCollocazione);
		
		TXT_collocation = new JTextField();
		P_search_control.add(TXT_collocation);
		TXT_collocation.setHorizontalAlignment(SwingConstants.CENTER);
		TXT_collocation.setFont(new Font("Tahoma", Font.BOLD, 11));
		TXT_collocation.setColumns(3);
		
		JLabel lblNominativo = new JLabel("Nome cliente");
		P_search_control.add(lblNominativo);
		
		TXT_nominativo = new JTextField();
		P_search_control.add(TXT_nominativo);
		TXT_nominativo.setColumns(13);
		
		JLabel lblClasse = new JLabel("Classe");
		P_search_control.add(lblClasse);
		
		CB_classe = new JComboBox<String>();
		P_search_control.add(CB_classe);
		CB_classe.setFont(new Font("Dialog", Font.BOLD, 11));
		CB_classe.setModel(new DefaultComboBoxModel<String>(new String[] {"-", "1", "2", "3"}));
		
		lblSez = new JLabel("Sez.");
		P_search_control.add(lblSez);
		
		CB_sezione = new JComboBox<String>();
		P_search_control.add(CB_sezione);
		CB_sezione.setFont(new Font("Dialog", Font.BOLD, 11));
		CB_sezione.setModel(new DefaultComboBoxModel<String>(new String[] {"-", "A", "B", "C", "D", "E", "F", "G"}));
		
		P_search_command_wrapper = new JPanel();
		P_search_wrapper.add(P_search_command_wrapper, BorderLayout.EAST);
		P_search_command_wrapper.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		P_search_command = new JPanel();
		P_search_command_wrapper.add(P_search_command);
		P_search_command.setLayout(new BorderLayout(0, 0));
		
		JButton B_searchLoan = new JButton("Cerca");
		P_search_command.add(B_searchLoan, BorderLayout.CENTER);
		
		CBX_resolved = new JCheckBox("Considera anche i risolti");
		P_search_command.add(CBX_resolved, BorderLayout.SOUTH);
		
		B_searchLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		
		SP_result = new JScrollPane();
		contentPane.add(SP_result, BorderLayout.CENTER);
		
		P_command = new JPanel();
		contentPane.add(P_command, BorderLayout.SOUTH);
		
		JButton B_close = new JButton("Chiudi");
		B_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parent.signalFrameClosed(getTitle());
				HttpHandler.getInstance().unregisterUI(SearchLoan.this);
				dispose();
			}
		});
		
		B_resolve = new JButton("Risolvi");
		B_resolve.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTable resultTable = (JTable) SP_result.getViewport().getView();

				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
				int row = resultTable.getSelectedRow();
				int code = (int) M.getValueAt(row, 0); // suppongo che ID sia sempre all'inizio!
				
				String status = (String) M.getValueAt(row, M.getColumnCount()-1); //suppongo che lo stato sia sempre alla fine
				
				if(status.toLowerCase().contains("risolto")) {
					JOptionPane.showMessageDialog(contentPane, "Non puoi risolvere un prestito già risolto!", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
					return;
				}

				new ResolveLoan(null, code, false);
				
			}
		});
		P_command.add(B_resolve);
		
		B_view = new JButton("Visualizza");
		B_view.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTable resultTable = (JTable) SP_result.getViewport().getView();

				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
				int row = resultTable.getSelectedRow();
				int code = (int) M.getValueAt(row, 0); // suppongo che ID sia sempre all'inizio!

				new ResolveLoan(null, code, true);
			}
		});
		P_command.add(B_view);
		P_command.add(B_close);
		
		HttpHandler.getInstance().registerUI(REGISTER_MODE.INPUT_DATA, this);
		
		parent.signalFrameOpened(getTitle());
		this.setVisible(true);
	}
		
	private void searchAction() {
		if (TXT_ISBN.getText().trim().isEmpty() && TXT_title.getText().trim().isEmpty()
				&& TXT_collocation.getText().trim().isEmpty() && TXT_nominativo.getText().trim().isEmpty()
				&& CB_classe.getSelectedIndex() == 0 && CB_sezione.getSelectedIndex() == 0) {

			int r = JOptionPane.showConfirmDialog(rootPane,
					"Verranno mostrati TUTTI i prestiti del database, vuoi continuare?", "Ricerca senza parametri",
					JOptionPane.YES_NO_OPTION);
			if (r != 0)
				return;

			resultTable = new JTable(DBManager.generateOngoingLoanTableModel(!CBX_resolved.isSelected())// NB devo
																										// negare il
																										// valore perchè
																										// il parametro
																										// indica una
																										// cosa diversa!
			);

		} else {
			String tmp = (String) CB_classe.getSelectedItem();
			int classe = tmp.equals("-") ? 0 : Integer.parseInt(tmp);
			String sezione = ((String) CB_sezione.getSelectedItem()).replace("-", "");

			resultTable = new JTable(DBManager.searchLoansAsTableModel(TXT_ISBN.getText().trim(),
					TXT_title.getText().trim(), TXT_collocation.getText().trim(), TXT_nominativo.getText().trim(),
					classe, sezione, CBX_resolved.isSelected()));
		}
		
		if(resultTable.getModel().getRowCount()<=0) {
			JOptionPane.showMessageDialog(contentPane, "La ricerca non ha prodotto alcun risultato.", "Attenzione", JOptionPane.WARNING_MESSAGE);
		}
		
		resultTable.setAutoCreateRowSorter(true);
		resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		SP_result.setViewportView(resultTable);
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
		return "SearchLoan";
	}

}
