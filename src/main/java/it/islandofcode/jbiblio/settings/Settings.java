package it.islandofcode.jbiblio.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Properties;

import org.tinylog.Logger;

public class Settings {

	public static enum PROPERTIES {
		DURATA_PRESTITO,
		TITOLO_RESPONSABILE,
		NOME_RESPONSABILE,
		NOME_SCUOLA
	}
	
	/**
	 * Durata del prestito, in giorni.
	 */
	public static final int DEFAULT_DURATA_PRESTITO = 15;
	public static final String DEFAULT_TITOLO_RESPONSABILE = "Referente";
	
	public static final int[] DURATE = {7,10,15,30};
	public static final String[] TITOLI = {"Referente", "Responsabile", "Curatore", "Curatrice", "Insegnante", "Sig.", "Sig.ra", "Dott.", "Dott.sa"};
	
	static final String FILENAME = "pref.properties";
	
	public static boolean initPropFile() {
		Properties prop = new Properties();
		prop.setProperty(PROPERTIES.DURATA_PRESTITO.name(), String.valueOf(DEFAULT_DURATA_PRESTITO));
		prop.setProperty(PROPERTIES.TITOLO_RESPONSABILE.name(), DEFAULT_TITOLO_RESPONSABILE);
		prop.setProperty(PROPERTIES.NOME_RESPONSABILE.name(), "");
		prop.setProperty(PROPERTIES.NOME_SCUOLA.name(), "");
		try (OutputStream out = new FileOutputStream(FILENAME)) {
			prop.store(out, "Settings per jBiblio");
		}  catch (IOException io) {
			Logger.error(io);
        }
		
		return true;
	}
	
	public static boolean propFileExist() {
		File PF = new File(FILENAME);
		return(PF.exists() && PF.canRead() && PF.canWrite());
	}
	
	public static String getValue(PROPERTIES key) throws PropertiesException {
		try (InputStream input = new FileInputStream(FILENAME)) {

            Properties prop = new Properties();

            prop.load(input);

            if(prop.containsKey(key.name())) {
            	return prop.getProperty(key.name(),"");
            }

        } catch (IOException ex) {
        	Logger.debug(ex);
        	throw new PropertiesException("Errore accesso al file.");
        }
		throw new PropertiesException("Chiave ["+key.name()+"] non trovata.");
	}
	
	public static boolean setValue(PROPERTIES key, String value) throws PropertiesException {
		Properties prop = new Properties();
		File temp = new File("temp");
		File old = new File(FILENAME);
		try (
				InputStream input = new FileInputStream(FILENAME);
				OutputStream out = new FileOutputStream(temp)
			) {
			prop.load(input);
			if(prop.containsKey(key.name())) {
				
				prop.setProperty(key.name(), value);
				
				prop.store(out, LocalDateTime.now().toString());
				input.close();
				out.close();
				old.delete();
				temp.renameTo(new File(FILENAME));
				return true;
            }
			
			
		}  catch (IOException io) {
			Logger.debug(io);
        	throw new PropertiesException("Errore accesso al file.");
        }
		
		throw new PropertiesException("Chiave ["+key.name()+"] non trovata.");
	}
	
	public static boolean setValueMap(Map<PROPERTIES, String> map) throws PropertiesException {
		Properties prop = new Properties();
		File temp = new File("temp");
		File old = new File(FILENAME);
		try (
				InputStream input = new FileInputStream(FILENAME);
				OutputStream out = new FileOutputStream(temp)
			) {
			prop.load(input);
			
			for(PROPERTIES key : map.keySet()) {
				if(!prop.containsKey(key.name())) {
					return false;
				}
				prop.setProperty(key.name(), map.get(key));
			}
			
			prop.store(out, LocalDateTime.now().toString());
			input.close();
			out.close();
			old.delete();
			temp.renameTo(new File(FILENAME));
		}  catch (IOException io) {
			Logger.debug(io);
			throw new PropertiesException("Errore accesso al file.");
        }
		return true;
	}
	
}
