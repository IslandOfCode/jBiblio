package it.islandofcode.jbiblio;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.artefact.Loan;
import it.islandofcode.jbiblio.db.DBDate;
import it.islandofcode.jbiblio.stats.LoadingUI;
import javax.swing.JSeparator;

public class ConfirmLoan extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the dialog.
	 */
	public ConfirmLoan(Client C, Loan L) {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Dati prestito");
		setBounds(100, 100, 450, 316);
		getContentPane().setLayout(null);
		
		JLabel lblpart1 = new JLabel("Il prestito è stato inserito nel sistema.");
		lblpart1.setHorizontalAlignment(SwingConstants.CENTER);
		lblpart1.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 18));
		lblpart1.setBounds(10, 11, 424, 50);
		getContentPane().add(lblpart1);
		
		JLabel L_code = new JLabel("# "+L.getID());
		L_code.setForeground(Color.RED);
		L_code.setFont(new Font("Dialog", Font.BOLD, 22));
		L_code.setHorizontalAlignment(SwingConstants.LEFT);
		L_code.setBounds(137, 72, 297, 30);
		getContentPane().add(L_code);
		
		JButton B_confirm = new JButton("Conferma");
		B_confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int r = JOptionPane.showConfirmDialog(
						getContentPane(),
						"<html>Sei sicuro di aver copiato il codice?<br/>Non sarà possibile recuperarlo.",
						"Attenzione!",
						JOptionPane.OK_CANCEL_OPTION
						);
				if(r!=0) return;
				dispose();
			}
		});
		B_confirm.setFont(new Font("Dialog", Font.BOLD, 14));
		B_confirm.setBounds(321, 250, 113, 26);
		getContentPane().add(B_confirm);

		JButton btnStampaRicevuta = new JButton("Stampa ricevuta");
		btnStampaRicevuta.addActionListener(new ActionListener() {
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
								
				new LoadingUI(null, LoadingUI.WORKTYPE.LOAN, destination, L.getID());
			}
		});
		btnStampaRicevuta.setFont(new Font("Dialog", Font.ITALIC, 14));
		btnStampaRicevuta.setBounds(10, 250, 156, 26);
		getContentPane().add(btnStampaRicevuta);
		
		JLabel lblNominativo = new JLabel("Nominativo:");
		lblNominativo.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNominativo.setBounds(10, 124, 113, 16);
		getContentPane().add(lblNominativo);
		
		JLabel L_name = new JLabel("Tizio Caio Sempronio");
		L_name.setFont(new Font("Dialog", Font.BOLD, 16));
		L_name.setBounds(137, 116, 297, 30);
		getContentPane().add(L_name);
		
		JLabel lblDataFinePrestito = new JLabel("Classe:");
		lblDataFinePrestito.setFont(new Font("Dialog", Font.BOLD, 14));
		lblDataFinePrestito.setBounds(10, 166, 113, 16);
		getContentPane().add(lblDataFinePrestito);
		
		JLabel L_class = new JLabel("1A");
		L_class.setFont(new Font("Dialog", Font.BOLD, 16));
		L_class.setBounds(137, 158, 297, 30);
		getContentPane().add(L_class);
		
		JLabel label = new JLabel("Data fine: ");
		label.setFont(new Font("Dialog", Font.BOLD, 14));
		label.setBounds(10, 208, 113, 16);
		getContentPane().add(label);
		
		JLabel L_endLoanDate = new JLabel("01/01/1970");
		L_endLoanDate.setFont(new Font("Dialog", Font.BOLD, 16));
		L_endLoanDate.setBounds(137, 200, 297, 30);
		getContentPane().add(L_endLoanDate);
		
		JLabel lblCodicePrestito = new JLabel("Codice prestito:");
		lblCodicePrestito.setFont(new Font("Dialog", Font.BOLD, 14));
		lblCodicePrestito.setBounds(10, 78, 113, 25);
		getContentPane().add(lblCodicePrestito);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 236, 422, 2);
		getContentPane().add(separator);
		
		
		//POPOLA
		L_code.setText("# "+L.getID());
		L_name.setText(C.getNome()+" "+C.getCognome());
		L_class.setText(C.getClasse()+C.getSezione());
		L_endLoanDate.setText( (new DBDate(L.getDateEnd())).getHumanDate() );

		setVisible(true);
	}
}
