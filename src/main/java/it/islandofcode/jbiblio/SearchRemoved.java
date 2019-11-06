package it.islandofcode.jbiblio;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

import it.islandofcode.jbiblio.artefact.Blame;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.db.ReadOnlyTableModel;

public class SearchRemoved extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_ISBN;
	private JTextField TXT_title;
	private JTextField TXT_name;
	
	private JTable resultTable;
	private JScrollPane SP_result;

	public SearchRemoved() {
		setTitle("Ricerca libri rimossi");
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 762, 489);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel P_search = new JPanel();
		contentPane.add(P_search, BorderLayout.NORTH);
		
		JLabel lblIsbn = new JLabel("ISBN");
		P_search.add(lblIsbn);
		
		TXT_ISBN = new JTextField();
		P_search.add(TXT_ISBN);
		TXT_ISBN.setColumns(10);
		
		JLabel lblTitolo = new JLabel("Titolo");
		P_search.add(lblTitolo);
		
		TXT_title = new JTextField();
		P_search.add(TXT_title);
		TXT_title.setColumns(12);
		
		JLabel lblNominativo = new JLabel("Nominativo");
		P_search.add(lblNominativo);
		
		TXT_name = new JTextField();
		P_search.add(TXT_name);
		TXT_name.setColumns(12);
		
		JLabel label = new JLabel("|");
		label.setFont(new Font("Tahoma", Font.PLAIN, 14));
		P_search.add(label);
		
		JButton B_search = new JButton("Cerca");
		B_search.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (TXT_ISBN.getText().isEmpty() && TXT_title.getText().isEmpty()
						&& TXT_name.getText().isEmpty()) {

					resultTable = new JTable(DBManager.searchRemovedAsTableModel(null, null, null, true));

				} else {
					resultTable = new JTable(DBManager.searchRemovedAsTableModel(TXT_ISBN.getText().trim(),
							TXT_title.getText().trim(), TXT_name.getText().trim(), false));
				}

				if (resultTable.getModel().getRowCount() <= 0) {
					JOptionPane.showMessageDialog(contentPane, "La ricerca non ha prodotto alcun risultato.",
							"Attenzione", JOptionPane.WARNING_MESSAGE);
				}

				resultTable.setAutoCreateRowSorter(true);
				resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				SP_result.setViewportView(resultTable);
			}
		});
		P_search.add(B_search);
		
		JPanel P_result = new JPanel();
		contentPane.add(P_result, BorderLayout.CENTER);
		P_result.setLayout(new BorderLayout(0, 0));
		
		SP_result = new JScrollPane();
		P_result.add(SP_result);
		
		JPanel P_command = new JPanel();
		contentPane.add(P_command, BorderLayout.SOUTH);
		
		JButton B_detail = new JButton("Dettaglio");
		B_detail.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(resultTable==null || resultTable.getSelectedRow()<0) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna riga selezionata", "Attenzione!", JOptionPane.WARNING_MESSAGE);
				} else {
					DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
					int row = resultTable.getSelectedRow();
					int col = ReadOnlyTableModel.indexOfColumnByName(M, "Ref.");

					String COLL = (String) M.getValueAt(row, col);
					
					Blame B = DBManager.getRemovedBookReason(COLL);
					
					if(B!=null) {
						if(B.getNote().isEmpty()) {
							JOptionPane.showMessageDialog(contentPane, "Non ci sono annotazioni per questo oggetto.", "Attenzione", JOptionPane.WARNING_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(contentPane, B.getNote(), "Nota", JOptionPane.PLAIN_MESSAGE);
						}
					}

				}
			}
		});
		P_command.add(B_detail);
		
		JButton B_close = new JButton("Chiudi");
		B_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		P_command.add(B_close);
		
		setVisible(true);
	}

}
