package it.islandofcode.jbiblio;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.gbooks.GBooks;

public class EditBook extends JFrame implements IRemoteUpdate{
	
	private static String NO_IMAGE = "Copertina non presente";
	
	public static enum BOOKMODE {
		ADD,	//aggiunge nuovo libro
		EDIT	//modifica libro esistente
	};

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_ISBN;
	private JTextField TXT_bookTitle;
	private JTextField TXT_bookAuthor;
	private JTextField TXT_bookPublisher;
	private JTextField TXT_bookDate;
	private JTextField TXT_collocation;
	private JLabel L_thumbnail;
	
	private JButton B_addBook;
	private JButton B_searchBook;
	
	private BOOKMODE MODE;
	private Book book;
	
	/**
	 * Create the frame.
	 */
	public EditBook(BOOKMODE mode, String ID, GUI parent) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				HttpHandler.getInstance().unregisterUI(EditBook.this);
				parent.signalFrameClosed(getTitle());
				dispose();
			}
		});
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 485, 239);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblIsbn = new JLabel("ISBN:");
		lblIsbn.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblIsbn.setBounds(10, 11, 46, 14);
		contentPane.add(lblIsbn);
		
		TXT_ISBN = new JTextField();
		TXT_ISBN.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		TXT_ISBN.setFont(new Font("Tahoma", Font.BOLD, 11));
		TXT_ISBN.setBounds(66, 8, 110, 20);
		contentPane.add(TXT_ISBN);
		TXT_ISBN.setColumns(13);
		
		JLabel lblTitolo = new JLabel("Titolo");
		lblTitolo.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblTitolo.setBounds(10, 49, 46, 14);
		contentPane.add(lblTitolo);
		
		B_searchBook = new JButton("Cerca libro");
		B_searchBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchAction();
			}
		});
		B_searchBook.setBounds(186, 7, 101, 23);
		contentPane.add(B_searchBook);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 36, 277, 2);
		contentPane.add(separator);
		
		TXT_bookTitle = new JTextField();
		TXT_bookTitle.setBounds(76, 46, 211, 20);
		contentPane.add(TXT_bookTitle);
		TXT_bookTitle.setColumns(10);
		
		JLabel lblAutore = new JLabel("Autore");
		lblAutore.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAutore.setBounds(10, 80, 46, 14);
		contentPane.add(lblAutore);
		
		TXT_bookAuthor = new JTextField();
		TXT_bookAuthor.setBounds(76, 77, 211, 20);
		contentPane.add(TXT_bookAuthor);
		TXT_bookAuthor.setColumns(10);
		
		JLabel lblPublisher = new JLabel("Publisher");
		lblPublisher.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblPublisher.setBounds(10, 111, 65, 14);
		contentPane.add(lblPublisher);
		
		TXT_bookPublisher = new JTextField();
		TXT_bookPublisher.setToolTipText("Questo valore non è ritornato dall'API Google Books.");
		TXT_bookPublisher.setBounds(76, 108, 211, 20);
		contentPane.add(TXT_bookPublisher);
		TXT_bookPublisher.setColumns(10);
		
		JLabel lblAnno = new JLabel("Anno");
		lblAnno.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblAnno.setBounds(10, 142, 46, 14);
		contentPane.add(lblAnno);
		
		TXT_bookDate = new JTextField();
		TXT_bookDate.setBounds(76, 139, 86, 20);
		contentPane.add(TXT_bookDate);
		TXT_bookDate.setColumns(10);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setBounds(10, 167, 277, 2);
		contentPane.add(separator_1);
		
		JLabel lblCollocazione = new JLabel("Collocazione");
		lblCollocazione.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblCollocazione.setBounds(10, 180, 89, 14);
		contentPane.add(lblCollocazione);
		
		TXT_collocation = new JTextField();
		TXT_collocation.setFont(new Font("Tahoma", Font.BOLD, 12));
		TXT_collocation.setBounds(109, 179, 52, 20);
		contentPane.add(TXT_collocation);
		TXT_collocation.setColumns(10);
		
		B_addBook = new JButton();
		B_addBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkIfFilledCorrectly()) {
					JOptionPane.showMessageDialog(contentPane, "Campi come ISBN, titolo e collocazione DEVONO essere compilati.", "Campi mancanti!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(DBManager.checkBookAlreadyPresent(TXT_collocation.getText().trim().toUpperCase())) {
					JOptionPane.showMessageDialog(contentPane, "Questa collocazione è già presente nel database.", "Libro già presente", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				if(MODE==BOOKMODE.ADD) {
					book = retriveDataFromForm();
					DBManager.addNewBook(book);
					Logger.info("Nuovo libro aggiunto, ISBN:"+book.getISBN() + " COLLOCAZIONE:"+book.getCollocation());
				} else { //EDIT
					Book bookUpdate = retriveDataFromForm();
					
					if(!book.equals(bookUpdate)) {
						DBManager.updateBook(book.getISBN(), bookUpdate);
						JOptionPane.showMessageDialog(contentPane, "Libro aggiornato", "Aggiornamento", JOptionPane.INFORMATION_MESSAGE);
						Logger.info("Libro ["+book.getISBN()+"], aggiornato con ISBN:"+book.getISBN());
					} //else do nothing
					
					Logger.warn("nessun aggiornamento effetturato ["+book.hashCode()+"]=["+bookUpdate.hashCode()+"]");
				}
				
				HttpHandler.getInstance().unregisterUI(EditBook.this);
				parent.signalFrameClosed(getTitle());
				dispose();
			}
		});
		B_addBook.setBounds(171, 178, 116, 23);
		contentPane.add(B_addBook);
		
		JPanel P_thumbnail = new JPanel();
		P_thumbnail.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		P_thumbnail.setBounds(299, 11, 168, 187);
		contentPane.add(P_thumbnail);
		P_thumbnail.setLayout(null);
		
		L_thumbnail = new JLabel(NO_IMAGE);
		L_thumbnail.setHorizontalAlignment(SwingConstants.CENTER);
		L_thumbnail.setBounds(0, 0, 168, 187);
		P_thumbnail.add(L_thumbnail);
		
		

		HttpHandler.getInstance().registerUI(REGISTER_MODE.INPUT_DATA, this);
		
		this.MODE = mode;

		switch(MODE) {
		case ADD:
			this.setTitle("Aggiungi nuovo libro");
			B_addBook.setText("Aggiungi libro");
			break;
		case EDIT:
			book = DBManager.getBookByCollocation(ID);
			
			populateForm();
			
			this.setTitle("Modifica libro");
			B_addBook.setText("Modifica libro");
			break;
		}
		
		parent.signalFrameOpened(getTitle());
		this.setVisible(true);
	}

	private void searchAction() {
		if (TXT_ISBN.getText().isEmpty()) {
			JOptionPane.showMessageDialog(contentPane, "Inserire un ISBN valido!", "Errore ISBN mancante",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		GBooks GB = new GBooks(TXT_ISBN.getText().trim());

		GB.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				B_addBook.setEnabled(true);
				B_searchBook.setEnabled(true);
				TXT_ISBN.setEnabled(true);

				switch (e.getPropertyName()) {
				case "done":
					Logger.info("GBooks ha finito il lavoro.");
					try {
						book = GB.get();
						populateForm();
					} catch (InterruptedException | ExecutionException e1) {
						Logger.error(e1);
						JOptionPane.showMessageDialog(contentPane,
								"<html>Impossibile recuperare le informazioni sul libro<br/><b>ERRORE: </b><code>"
										+ e1.getMessage() + "</code>",
								"Errore!", JOptionPane.ERROR_MESSAGE);
					}
					break;
				case "error":
					Logger.error("GBooks ha ritornato " + e.getPropertyName() + "->" + e.getNewValue());
					JOptionPane.showMessageDialog(contentPane, e.getNewValue(), "Errore!", JOptionPane.ERROR_MESSAGE);
					break;
				}
			}
		});

		B_addBook.setEnabled(false);
		B_searchBook.setEnabled(false);
		TXT_ISBN.setEnabled(false);

		GB.execute();

	}
	
	private Book retriveDataFromForm() {
		
		String thumb = "";
		if(L_thumbnail.getIcon()!=null) {
			thumb = book.getThumbnail();
		}
		
		int removed = 0;
		if(MODE==BOOKMODE.EDIT) {
			removed = 1;
		}
		
		return new Book(
				this.TXT_ISBN.getText().trim(),
				this.TXT_bookTitle.getText().trim(),
				this.TXT_bookAuthor.getText().trim(),
				this.TXT_bookPublisher.getText().trim(),
				this.TXT_bookDate.getText().trim(),
				thumb,
				this.TXT_collocation.getText().trim().toUpperCase(),
				removed
				);
	}
	
	private boolean checkIfFilledCorrectly() {
		return TXT_ISBN.getText().trim().isEmpty() || TXT_bookTitle.getText().trim().isEmpty() || TXT_collocation.getText().trim().isEmpty();
	}
	
	private void populateForm() {
		this.TXT_ISBN.setText(book.getISBN());
		this.TXT_bookTitle.setText(book.getTitle());
		
		this.TXT_bookAuthor.setText(book.getAuthor());
		this.TXT_bookPublisher.setText(book.getPublisher());
		this.TXT_bookDate.setText(book.getPublishedData());
		this.TXT_collocation.setText(book.getCollocation());
		if(book.getThumbnail()!=null && !book.getThumbnail().isEmpty()) {
			L_thumbnail.setIcon(book.getThumbnailAsImage());
			L_thumbnail.setText("");
		} else {
			L_thumbnail.setIcon(null);
			L_thumbnail.setText("Copertina non presente");
		}
		
	}
	
	

	@Override
	public void appStatusNotification(STATUS status) {
		//do nothing
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
		//do nothing
		return null;
	}

	@Override
	public String getRegisterId() {
		return "EditBook";
	}
}
