package it.islandofcode.jbiblio.artefact;

import it.islandofcode.jbiblio.db.DBDate;

public class Blame {
	
	public static final String[] DAMAGED_STATUS = new String[] {"Ritirato", "Danneggiato", "Perso", "Trasferito"};
	public enum DAMAGED_STATUS_INDEX {
		RITIRATO,
		DANNEGGIATO,
		PERSO,
		TRASFERITO
	}
	public static final int NOTE_LENGHT = 100;
	public static final String PREFIX = "R#_";
	
	private String collocation;
	private String originalColl;
	private DAMAGED_STATUS_INDEX reason;
	private int client;
	private String note;
	private DBDate date;
	
	public Blame(String collocation, String originalColl, DAMAGED_STATUS_INDEX reason, int client, String note, String date) {
		this.collocation = PREFIX+(collocation.replace(PREFIX, ""));
		this.originalColl = originalColl;
		this.reason = reason;
		this.client = client;
		this.note = note;
		this.date = new DBDate(date);
	}
	
	public Blame(String collocation, DAMAGED_STATUS_INDEX reason, int client, String note, String date) {
		this.collocation = PREFIX+(collocation.replace(PREFIX, ""));
		this.originalColl = collocation.replace(PREFIX, "");
		this.reason = reason;
		this.client = client;
		this.note = note;
		this.date = new DBDate(date);
	}
	
	public Blame(String collocation, DAMAGED_STATUS_INDEX reason, int client, String date) {
		this.collocation = PREFIX+(collocation.replace(PREFIX, ""));
		this.originalColl = collocation.replace(PREFIX, "");
		this.reason = reason;
		this.client = client;
		this.note = "";
		this.date = new DBDate(date);
	}
	
	public Blame(String collocation, DAMAGED_STATUS_INDEX reason, String note, String date) {
		this.collocation = PREFIX+(collocation.replace(PREFIX, ""));
		this.originalColl = collocation.replace(PREFIX, "");
		this.reason = reason;
		this.client = -1;
		this.note = note;
		this.date = new DBDate(date);
	}

	public String getCollocation() {
		return collocation;
	}

	public String getOriginalColl() {
		return originalColl;
	}

	public DAMAGED_STATUS_INDEX getReason() {
		return reason;
	}

	public int getClient() {
		return client;
	}

	public String getNote() {
		return note;
	}

	public DBDate getDate() {
		return date;
	}

}
