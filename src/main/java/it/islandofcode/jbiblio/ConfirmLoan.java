package it.islandofcode.jbiblio;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

public class ConfirmLoan extends JDialog {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the dialog.
	 */
	public ConfirmLoan(int loanCode) {
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setModalityType(ModalityType.APPLICATION_MODAL);
		
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Dati prestito");
		setBounds(100, 100, 450, 260);
		getContentPane().setLayout(null);
		
		JLabel lblpart1 = new JLabel("<html><center><span style=\"font-size:18pt\">Il prestito è stato confermato.</span><br/>\r\ncodice prestito:</center>");
		lblpart1.setHorizontalAlignment(SwingConstants.CENTER);
		lblpart1.setFont(new Font("Dialog", Font.BOLD, 16));
		lblpart1.setBounds(10, 11, 424, 50);
		getContentPane().add(lblpart1);
		
		JLabel L_code = new JLabel("# "+loanCode);
		L_code.setForeground(Color.RED);
		L_code.setFont(new Font("Dialog", Font.BOLD, 22));
		L_code.setHorizontalAlignment(SwingConstants.CENTER);
		L_code.setBounds(10, 72, 424, 30);
		getContentPane().add(L_code);
		
		JLabel lblpart2 = new JLabel("<html><b>NON PERDERE IL CODICE !!</b> Senza di esso non sarà possibile risolvere il prestito alla scandenza.<br/>\r\n<i>Allegare Codice prestito e collocazione di ogni libro associato al prestito nella ricevuta.</i>");
		lblpart2.setVerticalAlignment(SwingConstants.TOP);
		lblpart2.setBounds(10, 113, 424, 69);
		getContentPane().add(lblpart2);
		
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
		B_confirm.setBounds(321, 193, 113, 26);
		getContentPane().add(B_confirm);
		
		
		L_code.setText("# "+loanCode);

	}

}
