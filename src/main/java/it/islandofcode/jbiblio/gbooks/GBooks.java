package it.islandofcode.jbiblio.gbooks;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;

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

public class GBooks {
	public static Book searchByISBN(String ISBN) throws GeneralSecurityException, IOException, BookNotFound {
		Book ret = new Book();

		final Books books = new Books.Builder(GoogleNetHttpTransport.newTrustedTransport(),
				JacksonFactory.getDefaultInstance(), null).setApplicationName(GBooksCredential.APPLICATION_NAME)
						.setGoogleClientRequestInitializer(new BooksRequestInitializer(GBooksCredential.getKEY()))
						.build();

		String query = "isbn:" + ISBN;
		List volumesList = books.volumes().list(query);

		Volumes volumes = volumesList.execute();
		if (volumes.getTotalItems() == 0 || volumes.getItems() == null) {
			Logger.error("Libro cercato [" + ISBN + "] non trovato");
			throw new BookNotFound();
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

		return ret;
	}
}
