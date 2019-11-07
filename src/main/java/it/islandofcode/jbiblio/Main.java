package it.islandofcode.jbiblio;

import java.awt.EventQueue;
import java.io.File;
import java.time.LocalDateTime;

import javax.swing.JOptionPane;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.db.DBManager;

public class Main {

	public static void main(String[] args) {
		Logger.info("JBIBLIO START @ "+LocalDateTime.now().toString());
		
		//init sqlite
		File dbdir = new File("db");
		File dbfile = new File("db/jbiblio.db");
		if(!dbfile.exists()) {
			if(!dbdir.exists()) dbdir.mkdir();
			DBManager.createDB();
			if(dbfile.exists()) {
				DBManager.initDB();
				Logger.warn("DB non presente, nuovo DB creato.");
			} else {
				Logger.error("Creazione DB fallita, notifico ed esco");
				JOptionPane.showMessageDialog(null, "<html>Non è stato possibile creare il database.<br/>"
						+ "Forse il programma non ha l'autorizzazione di scrittura nella cartella.<br/>"
						+ "Prova a cambiare cartella (il Desktop è una buona scelta) e riavvia jBiblio.</html>",
						"Errore!", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		} else {
			int ver = DBManager.getUserVersion();
			if(ver!=DBManager.USER_VERSION) {
				Logger.error("USER_VERSION non coincide. Trovato "+ver+", atteso "+DBManager.USER_VERSION);
				JOptionPane.showMessageDialog(null, "<html>Non è stato possibile utilizzare il database corrente.<br/>"
						+ "Probabilmente il programma è stato aggiornato e non è in grado di usare il vecchio database.<br/>"
						+ "Per favore contatta lo sviluppatore per ulteriori istruzioni.<br/>"
						+ "<center><b>ERRORE: </b><code>Trovato "+ver+", atteso "+DBManager.USER_VERSION+"</code></center></html>",
						"Errore!", JOptionPane.ERROR_MESSAGE);
				System.exit(1);
			}
		}
		
		/*if(!Settings.propFileExist()) {
			Settings.initPropFile();
		}*/

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new GUI();
				} catch (Exception e) {
					Logger.error(e);
					e.printStackTrace();
				}
			}
		});
	}

}
