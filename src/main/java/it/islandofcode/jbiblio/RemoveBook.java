package it.islandofcode.jbiblio;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.db.DBManager;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class RemoveBook extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public static final String[] DAMAGED_STATUS = new String[] {"Ritirato", "Danneggiato", "Perso", "Trasferito"};
	public enum DAMAGED_STATUS_INDEX {
		RITIRATO,
		DANNEGGIATO,
		PERSO,
		TRASFERITO
	}
	
	private static final int NOTE_LENGHT = 100;
	
	private JPanel contentPane;
	private JTextField TXT_IDclient;
	private JButton B_confirm;
	private JLabel TXT_ISBN;
	private JLabel TXT_Collocation;
	private JComboBox<String> CB_reason;
	private JTextPane TXT_note;
	private JLabel TXT_charLen;
	
	private Book book;
	private String newColl;
	private static final String PREFIX = "R#_";

	public RemoveBook(Book libro) {
		this.book = libro;
		this.newColl = PREFIX+book.getCollocation();
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				dispose();
			}
		});
		setTitle("Rimuovi libro");
		setResizable(false);
		setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 310, 423);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIsbn = new JLabel("ISBN:");
		lblIsbn.setBounds(10, 11, 94, 16);
		contentPane.add(lblIsbn);
		
		TXT_ISBN = new JLabel("0123456789012");
		TXT_ISBN.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_ISBN.setBounds(116, 10, 131, 16);
		contentPane.add(TXT_ISBN);
		
		JLabel lblNewLabel = new JLabel("Collocazione:");
		lblNewLabel.setBounds(10, 38, 94, 16);
		contentPane.add(lblNewLabel);
		
		TXT_Collocation = new JLabel("99Z");
		TXT_Collocation.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_Collocation.setBounds(116, 38, 131, 16);
		contentPane.add(TXT_Collocation);
		
		JLabel lblNuovaColl = new JLabel("Nuova Coll.:");
		lblNuovaColl.setBounds(12, 66, 92, 16);
		contentPane.add(lblNuovaColl);
		
		JLabel TXT_newColl = new JLabel("R#_99Z");
		TXT_newColl.setToolTipText("<html>La collocazione viene modificata aggiungendo <code>R#_</code><br/>\r\nall'inizio della stessa, in questo modo il vecchio codice collocazione può<br/>\r\nessere riutilizzato per un nuovo libro.<br/>\r\nQuesto permette anche una più facile gestione del database, dato che<br/>\r\npermette una più facile distinzione tra libri rimossi e libri presenti.");
		TXT_newColl.setFont(new Font("Dialog", Font.BOLD | Font.ITALIC, 12));
		TXT_newColl.setBounds(116, 66, 131, 16);
		contentPane.add(TXT_newColl);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 94, 282, 2);
		contentPane.add(separator);
		
		JLabel lblRagione = new JLabel("Ragione:");
		lblRagione.setBounds(20, 111, 66, 16);
		contentPane.add(lblRagione);
		
		CB_reason = new JComboBox<String>();
		CB_reason.setModel(new DefaultComboBoxModel<String>(DAMAGED_STATUS));
		CB_reason.setSelectedIndex(0);
		CB_reason.setBounds(96, 107, 131, 25);
		contentPane.add(CB_reason);
		
		JLabel lblCliente = new JLabel("ID Cliente:");
		lblCliente.setBounds(20, 156, 66, 16);
		contentPane.add(lblCliente);
		
		TXT_IDclient = new JTextField();
		TXT_IDclient.setHorizontalAlignment(SwingConstants.CENTER);
		TXT_IDclient.setText("0");
		TXT_IDclient.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_IDclient.setBounds(96, 152, 66, 24);
		contentPane.add(TXT_IDclient);
		TXT_IDclient.setColumns(10);
		
		JLabel lblNote = new JLabel("Note:");
		lblNote.setBounds(20, 189, 43, 16);
		contentPane.add(lblNote);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBounds(20, 216, 272, 120);
		contentPane.add(scrollPane);
		
		TXT_note = new JTextPane();
		TXT_note.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String txt = TXT_note.getText();
				if(txt.length()>NOTE_LENGHT) {
					txt.substring(0, NOTE_LENGHT);
					TXT_note.setText(txt);
				}
				TXT_charLen.setText(txt.length()+"/"+NOTE_LENGHT);
			}
		});
		TXT_note.setFont(new Font("Dialog", Font.ITALIC, 12));
		scrollPane.setViewportView(TXT_note);
		
		JButton B_checkClient = new JButton("Check");
		B_checkClient.setIcon(new ImageIcon(RemoveBook.class.getResource("/icon/cerca.png")));
		B_checkClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!checkClientByID()) return;
			}
		});
		B_checkClient.setBounds(174, 151, 98, 26);
		contentPane.add(B_checkClient);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 348, 282, 2);
		contentPane.add(separator_1);
		
		B_confirm = new JButton("Conferma");
		B_confirm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!checkClientByID()) return;
				String oldColl = book.getCollocation();
				book.setCollocation(newColl);
				boolean res = DBManager.updateBook(oldColl, book);
				
				/*
				 * TODO crea funzione che aggiorni la nuova collocazione in tutti i prestiti (bookloaned)
				 * GIA' RISOLTI!!! Se ci sono prestiti da risolvere, blocca l'operazione!
				 * 
				 * Modifica Books per aggiungere queste info: ragione, note, codice cliente e data ritiro.
				 * 
				 * Una ricerca libro per mostrare i libri ritirati.
				 */
				
				
				if(res) {
					JOptionPane.showMessageDialog(contentPane, "<html>Il libro<br/>"
							+ "<center><i>"+book.getTitle()+"</i></center><br/>"
									+ "è stato rimosso con successo!", "Rimosso!", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(contentPane, "Il libro non è stato rimosso correttamente.", "Attenzione!", JOptionPane.WARNING_MESSAGE);
				}
				dispose();
			}
		});
		B_confirm.setEnabled(false);
		B_confirm.setBounds(12, 358, 98, 26);
		contentPane.add(B_confirm);
		
		JButton B_cancel = new JButton("Annulla");
		B_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		B_cancel.setBounds(194, 358, 98, 26);
		contentPane.add(B_cancel);
		
		TXT_charLen = new JLabel("0/100");
		TXT_charLen.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_charLen.setHorizontalAlignment(SwingConstants.RIGHT);
		TXT_charLen.setBounds(237, 189, 55, 16);
		contentPane.add(TXT_charLen);
	}
	
	
	private boolean checkClientByID() {
		Client C = DBManager.getClientByID(Integer.parseInt(TXT_IDclient.getText().trim()));
		if(C==null || C.isRemoved()) {
			JOptionPane.showMessageDialog(contentPane, "L'ID indicato non è associato ad alcun cliente attivo.", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		int r = JOptionPane.showConfirmDialog(contentPane,
				"<html>Questo è il cliente trovato:<br/>"
				+ "<i>Nome</i> : <b>"+C.getNome()+" "+C.getCognome()+"</b><br/>"
						+ "<i>Classe</i> : <b>"+C.getClasse()+C.getSezione()+"</b><br/><br/>"
								+ "Confermi?",
				"Conferma", JOptionPane.YES_NO_OPTION);
		if(r!=0)
			return false;
					
		B_confirm.setEnabled(true);
		
		return true;
	}
}
