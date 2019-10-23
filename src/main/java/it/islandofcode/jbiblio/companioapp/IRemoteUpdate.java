package it.islandofcode.jbiblio.companioapp;

/**
 * Interfaccia per ricevere e inviare dati, oltre che notifiche di stato,
 * dal client remoto.
 * @author Pier Riccardo Monzo
 */
public interface IRemoteUpdate {
	
	/**
	 * Enumeratore che elenca i possibili stati del client remoto.
	 */
	public static enum STATUS{
		CONNECTED,
		DISCONNECTED,
		TIMEOUT,
		DATA_RECEIVED
	}
	
	/**
	 * Ritorna un valore identificativo della finestra.
	 * Non è necessario che sia unico (ad es. il titolo del frame),
	 * ma deve essere sempre lo stesso, indipendenemtente da quando venga
	 * chiamata questa funzione tra quando la classe si registra a quando
	 * questa cancella la registrazione.
	 * @return String identificativo unico della classe implementatrice
	 */
	public String getRegisterId();
	
	/**
	 * Viene invocato se la classe ha richiesto di essere notifica
	 * su un cambio di stato del client remoto.
	 * @param status un valore dell'enum {@linkplain IRemoteUpdate.STATUS}
	 */
	public void appStatusNotification(STATUS status);
	
	/**
	 * Viene invocato se la classe ha richiesto di essere notificata
	 * su la ricezione di dati dal client remoto.
	 * @param msg String Il messaggio ricevuto dal client remoto. Può anche essere null.
	 */
	public void receiveAppMessage(String msg);
	
	/**
	 * Come per {@linkplain receiveAndRespondAppMessage}, ma si può anche
	 * inviare una risposta specifica al client remoto.
	 * @param msg String Il messaggio ricevuto dal client remoto. Può anche essere null.
	 * @return String Il messaggio da inviare al client remoto.
	 */
	public String receiveAndRespondAppMessage(String msg);
}
