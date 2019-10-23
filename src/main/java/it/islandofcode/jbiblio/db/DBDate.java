package it.islandofcode.jbiblio.db;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class DBDate {
	private static final DateTimeFormatter DATE_FORMAT = new DateTimeFormatterBuilder()
			.appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
			.appendOptional(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
			.toFormatter();
		
	public static final String TODAY = LocalDate.now().toString();
		
	private LocalDate date;
	
	public DBDate() {
		this.date = LocalDate.now();
	}
	
	public DBDate(String date) {
		this.date = LocalDate.parse(date,DATE_FORMAT);
	}
	
	public DBDate(LocalDate date) {
		this.date = date;
	}
	
	public LocalDate getDate() {
		return date;
	}
	
	public String getHumanDate() {
		return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	}
	
	public String getSQLiteDate() {
		return date.toString();
	}
	
	public void addDays(int days) {
		date.plusDays(days);
	}
	
	public static DBDate todayPlus(int plus) {
		return new DBDate(LocalDate.now().plusDays(plus));
	}

	@Override
	public String toString() {
		return date.toString();
	}

}
