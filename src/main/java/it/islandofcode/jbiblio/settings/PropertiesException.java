package it.islandofcode.jbiblio.settings;

import java.io.IOException;

public class PropertiesException extends IOException {

	private static final long serialVersionUID = 1L;
	
	public PropertiesException() {
		super("Il file delle preferenze non esiste, non è accessibile o esiste ma la chiave indicata è inesistente");
	}
	
	
	public PropertiesException(String msg) {
		super(msg);
	}

}
