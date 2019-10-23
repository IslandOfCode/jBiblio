package it.islandofcode.jbiblio.gbooks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.tinylog.Logger;

public class GBooksCredential {
	// static final String API_KEY = "";
	static final String APPLICATION_NAME = "Islandofcode.it-jBiblio/1.0";

	public static final String getKEY() {
		String ret = "";
		//try (InputStream input = new FileInputStream("api.properties")) {
		//ClassLoader.getSystemClassLoader().getResource("/api.properties").getFile())
		try (InputStream input = GBooksCredential.class.getResourceAsStream("/api.properties")) {

			Properties prop = new Properties();

			// load a properties file
			prop.load(input);
			ret = prop.getProperty("GBOOKS_API");

		} catch (IOException ex) {
			Logger.error(ex);
		}
		return ret;
	}
}
