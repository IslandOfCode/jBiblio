package it.islandofcode.jbiblio.stats;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.artefact.Loan;
import it.islandofcode.jbiblio.db.DBManager;

public class LoadingUI extends JDialog {

	private static final long serialVersionUID = 1L;
	
	public static enum WORKTYPE {
		BOOKSLIST,
		STATISTICS,
		LOAN
	}
	
	private JProgressBar progressBar;

	/**
	 * Create the dialog.
	 */
	public LoadingUI(JFrame parent, WORKTYPE work, File destination, int LoanCode) {
		getContentPane().setBackground(new Color(255, 228, 196));
		setUndecorated(true);
		setType(Type.UTILITY);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		setResizable(false);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setBounds(100, 100, 200, 46);
		getContentPane().setLayout(null);
		
		JLabel L_icon = new JLabel("");
		L_icon.setHorizontalAlignment(SwingConstants.CENTER);
		L_icon.setIcon(new ImageIcon(LoadingUI.class.getResource("/icon/write.png")));
		L_icon.setBounds(0, 0, 42, 48);
		getContentPane().add(L_icon);
		
		progressBar = new JProgressBar();
		progressBar.setForeground(new Color(255, 140, 0));
		progressBar.setIndeterminate(true);
		progressBar.setValue(100);
		progressBar.setBounds(47, 11, 143, 24);
		getContentPane().add(progressBar);
		setPreferredSize(new Dimension(200, 48));
		pack();
		setLocationRelativeTo(parent);
		
		
		StatsWorker SW;
		if(LoanCode>=0) {
			Loan L = DBManager.getLoan(LoanCode);
			if(L==null) {
				JOptionPane.showMessageDialog(getContentPane(), "Il prestito con codice "+LoanCode+" non è stato trovato!", "Errore critico!", JOptionPane.ERROR_MESSAGE);
				dispose();
			}
			Client C = DBManager.getClientByID(L.getClient());
			if(C==null) {
				JOptionPane.showMessageDialog(getContentPane(), "Il cliente con codice "+L.getClient()+" non è stato trovato!", "Errore critico!", JOptionPane.ERROR_MESSAGE);
				dispose();
			}
			SW = new StatsWorker(work,destination,C,L);
		} else {
			SW = new StatsWorker(work, destination);
		}
		
		SW.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				//System.out.println("PROPERTY NAME: " + e.getPropertyName());
				//System.out.println("PROPERTY VALUE: " + e.getNewValue());
				switch(e.getPropertyName()) {
				case "done":
					Logger.info("StatsWorker ha finito il lavoro.");
					L_icon.setIcon(new ImageIcon(LoadingUI.class.getResource("/icon/done.png")));
					progressBar.setIndeterminate(false);
					progressBar.setValue(100);
					JOptionPane.showMessageDialog(getContentPane(), "Creazione file completata!", "Operazione completata", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(LoadingUI.class.getResource("/icon/done.png")));
					dispose();
					break;
				case "error":
					Logger.error("StatsWorker è terminato con un errore " + e.getNewValue());
					L_icon.setIcon(new ImageIcon(LoadingUI.class.getResource("/icon/error.png")));
					progressBar.setIndeterminate(false);
					progressBar.setValue(0);
					JOptionPane.showMessageDialog(getContentPane(), "<html>Creazione file fallita!<br/>Errore: <b>"+e.getNewValue()+"</b>", "Errore!", JOptionPane.ERROR_MESSAGE, new ImageIcon(LoadingUI.class.getResource("/icon/error.png")));
					dispose();
					break;
				}
			}
		});

		SW.execute();
		
		setVisible(true);
		
	}
}
