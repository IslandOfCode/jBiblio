package it.islandofcode.jbiblio;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Blame;
import it.islandofcode.jbiblio.artefact.Blame.DAMAGED_STATUS_INDEX;
import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.db.DBDate;
import it.islandofcode.jbiblio.db.DBManager;

public class RemoveBook extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private JTextField TXT_IDclient;
	private JButton B_confirm;
	private JLabel TXT_ISBN;
	private JLabel TXT_Collocation;
	private JComboBox<String> CB_reason;
	private JTextPane TXT_note;
	private JLabel TXT_charLen;
	
	private Book book;
	private Client client;
	
	public RemoveBook(Book libro) {
		this.book = libro;
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
		CB_reason.setModel(new DefaultComboBoxModel<String>(Blame.DAMAGED_STATUS));
		CB_reason.setSelectedIndex(0);
		CB_reason.setBounds(96, 107, 131, 25);
		contentPane.add(CB_reason);
		
		JLabel lblCliente = new JLabel("ID Cliente:");
		lblCliente.setBounds(20, 156, 66, 16);
		contentPane.add(lblCliente);
		
		TXT_IDclient = new JTextField();
		TXT_IDclient.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				//if(client!=null && client.getID()!=Integer.parseInt(TXT_IDclient.getText().trim()))
				B_confirm.setEnabled(false);
			}
		});
		TXT_IDclient.setHorizontalAlignment(SwingConstants.CENTER);
		TXT_IDclient.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_IDclient.setBounds(96, 152, 66, 24);
		contentPane.add(TXT_IDclient);
		TXT_IDclient.setColumns(10);
		
		JLabel lblNote = new JLabel("Note:");
		lblNote.setBounds(20, 189, 43, 16);
		contentPane.add(lblNote);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(20, 216, 272, 120);
		contentPane.add(scrollPane);
		
		TXT_note = new JTextPane();
		TXT_note.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				StyledDocument SD = TXT_note.getStyledDocument();
				if(SD.getLength()>Blame.NOTE_LENGHT) {
					try {
						SD.remove(Blame.NOTE_LENGHT, SD.getLength()-Blame.NOTE_LENGHT);
					} catch (BadLocationException e1) {
						Logger.error(e1);
					}
					TXT_note.setDocument(SD);
				}
				TXT_charLen.setText(SD.getLength()+"/"+Blame.NOTE_LENGHT);
			}
		});
		TXT_note.setFont(new Font("Dialog", Font.ITALIC, 12));
		scrollPane.setViewportView(TXT_note);
		
		JButton B_checkClient = new JButton("Check");
		B_checkClient.setIcon(new ImageIcon(RemoveBook.class.getResource("/icon/cerca.png")));
		B_checkClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!checkClientByID()) return;
				B_confirm.setEnabled(true);
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
				
				int res = DBManager.removeBook(book.getCollocation());
				
				Blame B = new Blame(
						book.getCollocation(),
						DAMAGED_STATUS_INDEX.valueOf(((String)CB_reason.getSelectedItem()).toUpperCase()),
						Integer.parseInt(TXT_IDclient.getText().trim()),
						TXT_note.getText().trim(),
						(new DBDate()).getSQLiteDate()
						);
				
				boolean R = (res>0) && DBManager.addRemovedBookReason(B);
				
				if(R) {
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
		
		
		if(book!=null) {
			TXT_ISBN.setText(book.getISBN());
			TXT_Collocation.setText(book.getCollocation());
			TXT_newColl.setText(Blame.PREFIX+book.getCollocation());
		}
		
		setVisible(true);
	}
	
	
	private boolean checkClientByID() {
		if(TXT_IDclient.getText().trim().isEmpty()) {
			JOptionPane.showMessageDialog(contentPane, "Nessun ID cliente indicato!", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		client = DBManager.getClientByID(Integer.parseInt(TXT_IDclient.getText().trim()));
		if(client==null || client.isRemoved()) {
			JOptionPane.showMessageDialog(contentPane, "L'ID indicato non è associato ad alcun cliente attivo.", "Attenzione!", JOptionPane.WARNING_MESSAGE);
			return false;
		}
		
		int r = JOptionPane.showConfirmDialog(contentPane,
				"<html>Questo è il cliente trovato:<br/>"
				+ "<i>Nome</i> : <b>"+client.getNome()+" "+client.getCognome()+"</b><br/>"
						+ "<i>Classe</i> : <b>"+client.getClasse()+client.getSezione()+"</b><br/><br/>"
								+ "Confermi?",
				"Conferma", JOptionPane.YES_NO_OPTION);
		if(r!=0)
			return false;
					
		B_confirm.setEnabled(true);
		
		return true;
	}
}
