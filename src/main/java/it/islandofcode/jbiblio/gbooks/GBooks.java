package it.islandofcode.jbiblio.gbooks;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;

import javax.swing.SwingWorker;

import org.tinylog.Logger;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.books.Books;
import com.google.api.services.books.BooksRequestInitializer;
import com.google.api.services.books.model.Volume;
import com.google.api.services.books.model.Volume.VolumeInfo.ImageLinks;
import com.google.api.services.books.model.Volumes;
import com.google.api.services.books.Books.Volumes.List;
import it.islandofcode.jbiblio.artefact.Book;

public class GBooks extends SwingWorker<Book, Object>{
	private String ISBN;
	public GBooks(String iSBN) {
		this.ISBN = iSBN;
	}
	
	@Override
	protected Book doInBackground() {// throws GeneralSecurityException, IOException, BookNotFound {
		Book ret = new Book();

		Books books = null;
		try {
			books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(),
					JacksonFactory.getDefaultInstance(), null).setApplicationName(GBooksCredential.APPLICATION_NAME)
							.setGoogleClientRequestInitializer(new BooksRequestInitializer(GBooksCredential.getKEY()))
							.build();
		} catch (GeneralSecurityException | IOException e) {
			Logger.error(e);
			firePropertyChange("error", null, "Controllare la propria connessione ad internet e riprovare");
			return null;
		}

		String query = "isbn:" + ISBN;
		Volumes volumes = null;
		List volumesList = null;
		
		try {
			volumesList = books.volumes().list(query);
			volumes = volumesList.execute();
			if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
				Logger.error("Libro cercato [" + ISBN + "] non trovato");
				firePropertyChange("error", null, "Controllare l'ISBN inserito o inserire a mano le informazioni.");
				//throw new BookNotFound();
			}
		} catch (IOException e) {
			Logger.error(e);
			firePropertyChange("error", null, "Controllare la propria connessione ad internet e riprovare");
			return null;
		}

		

		Volume volume = volumes.getItems().get(0);
		//inutile fare for se tanto cerco per isbn e trovo un solo libro, se lo trovo!
		// for (Volume volume : volumes.getItems()) {
		Volume.VolumeInfo volumeInfo = volume.getVolumeInfo();

		ret.setISBN(ISBN);
		
		ret.setTitle(volumeInfo.getTitle());
		//String TITLE = volumeInfo.getTitle();

		//String AUTHOR = "";
		if (volumeInfo.getAuthors() != null && !volumeInfo.getAuthors().isEmpty()) {
			ret.setAuthor(volumeInfo.getAuthors().stream().collect(Collectors.joining(", ")));
		}

		ret.setPublisher(volumeInfo.getPublisher());
		//String PUBLISHER = volumeInfo.getPublisher();

		ret.setPublishedData(volumeInfo.getPublishedDate());
		//String PUBDATE = volumeInfo.getPublishedDate();

		//String THUMBURL = "";
		ImageLinks IL = volumeInfo.getImageLinks();
		if (IL != null && !IL.isEmpty() && IL.getThumbnail() != null && !IL.getThumbnail().isEmpty()) {
			ret.setThumbnail(volumeInfo.getImageLinks().getThumbnail());
		}
		// } //fine for
		firePropertyChange("done", null, "QWE");
		return ret;
	}
}
