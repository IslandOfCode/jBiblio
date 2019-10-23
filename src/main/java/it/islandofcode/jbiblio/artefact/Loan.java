package it.islandofcode.jbiblio.artefact;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

public class Loan {
	
	public static final int RETURNED = 1;
	public static final int NOT_RETURNED = 0;
	
	public static final SimpleDateFormat DBDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat HumanDateFormatter = new SimpleDateFormat("dd/MM/yyyy");
	
	private int ID;
	private int client;
	private String dateStart;
	private String dateEnd;
	private String dateReturned;
	private List<Book> books;
	private int returned;
	
	/**
	 * Costruttore vuoto, per successiva popolazione dell'oggetto.
	 */
	public Loan() {
		super();
	}
	
	/**
	 * Costruttore completo, per operazioni di modifica o cancellazione.
	 * @param iD
	 * @param client
	 * @param dateStart
	 * @param dateEnd
	 * @param dateRet
	 * @param books
	 * @param returned 0 no, 1 si
	 */
	public Loan(int iD, int client, String dateStart, String dateEnd, String dateRet, List<Book> books, int returned) {
		super();
		ID = iD;
		this.client = client;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.dateReturned = dateRet;
		this.books = books;
		this.returned = returned;
	}
	
	/**
	 * Costruttore senza ID, per l'inserimento nel DB (chiave autoincrementante).
	 * @param client
	 * @param dateStart
	 * @param dateEnd
	 * @param books
	 */
	public Loan(int client, String dateStart, String dateEnd, List<Book> books) {
		super();
		ID = -1;
		this.client = client;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.dateReturned = "";
		this.books = books;
	}
	
	/**
	 * Costruttore senza ID o lista libri, per inserimento con successiva creazione
	 * della lista.
	 * @param client
	 * @param dateStart
	 * @param dateEnd
	 */
	public Loan(int client, String dateStart, String dateEnd) {
		super();
		ID = -1;
		this.client = client;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.dateReturned = "";
		this.books = null;
	}

	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public int getClient() {
		return client;
	}

	public void setClient(int client) {
		this.client = client;
	}

	public String getDateStart() {
		return dateStart;
	}

	public void setDateStart(String dateStart) {
		this.dateStart = dateStart;
	}

	public String getDateEnd() {
		return dateEnd;
	}

	public void setDateEnd(String dateEnd) {
		this.dateEnd = dateEnd;
	}

	public String getDateReturned() {
		return dateReturned;
	}

	public void setDateReturned(String dateReturned) {
		this.dateReturned = dateReturned;
	}

	public List<Book> getBooks() {
		return books;
	}

	public void setBooks(List<Book> books) {
		this.books = books;
	}
	
	
	public int getReturned() {
		return returned;
	}

	public void setReturned(int returned) {
		this.returned = (returned>=1)?1:0;
		this.dateReturned = (returned>=1)?LocalDate.now().toString():"";
	}

	public boolean isIDset() {
		return ID>0;
	}
	
	public boolean isReturned() {
		return returned==1;
	}
}
