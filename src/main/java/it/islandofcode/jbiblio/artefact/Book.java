package it.islandofcode.jbiblio.artefact;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import javax.swing.ImageIcon;

import org.tinylog.Logger;


public class Book {
	private String isbn;
	private String title;
	private String author;
	private String publisher;
	private String publishedData;
	private String thumbnail;
	private String collocation;
	private int removed;
	private int damaged;
	
	
	public Book(String iSBN, String title, String author, String publisher, String publishedData, String thumbnail, String collocation, int removed, int damaged) {
		isbn = iSBN;
		this.title = title;
		this.author = author;
		this.publisher = publisher;
		this.publishedData = publishedData;
		this.thumbnail = thumbnail;
		this.collocation = collocation;
		this.removed = removed;
		this.damaged = damaged;
	}

	/**
	 * Così puoi popolare l'oggetto campo per campo
	 */
	public Book() {
		super();
	}

	public String getISBN() {
		return isbn;
	}

	public void setISBN(String iSBN) {
		isbn = iSBN;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublishedData() {
		return publishedData;
	}

	public void setPublishedData(String publishedData) {
		this.publishedData = publishedData;
	}
	
	public String getCollocation() {
		return collocation;
	}

	public void setCollocation(String collocation) {
		this.collocation = collocation;
	}

	public String getThumbnail() {
		return thumbnail;
	}
	
	public ImageIcon getThumbnailAsImage() {
		return Book.base642ImageIcon(thumbnail);
	}

	public void setThumbnail(String thumbnail) {
		//this.thumbnail = thumbnail;
		this.thumbnail = Book.URLimage2Base64(thumbnail);
	}

	public int getRemoved() {
		return removed;
	}

	public void setRemoved(int removed) {
		if(removed>=1)
			this.removed = 1;
		else
			this.removed = 0;
	}
	
	public boolean isRemoved() {
		return this.removed==1;
	}
	
	public boolean isPresent() {
		return this.removed==0;
	}
	
	public int getDamaged() {
		return removed;
	}

	public void setDamaged(int damaged) {
		if(damaged>=1)
			this.damaged = 1;
		else
			this.damaged = 0;
	}
	
	public boolean isdamaged() {
		return this.damaged==1;
	}

	public static String URLimage2Base64(String url) {
		try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
				ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			byte dataBuffer[] = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
				out.write(dataBuffer, 0, bytesRead);
			}
			
			return Base64.getEncoder().encodeToString(out.toByteArray());
		} catch (IOException e) {
			Logger.error(e);
		}
		return "";

	}
	
	/**
	 * return new ImageIcon(b);
	 * return new ImageIcon(b).getImage();
	 * @param url
	 * @return
	 */
	public static ImageIcon base642ImageIcon(String strImg) {
		return new ImageIcon(Base64.getDecoder().decode(strImg));
	}

	@Override
	public String toString() {
		return "Book [ISBN=" + isbn + ", title=" + title + ", collocation=" + collocation + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((collocation == null) ? 0 : collocation.hashCode());
		result = prime * result + ((publishedData == null) ? 0 : publishedData.hashCode());
		result = prime * result + ((publisher == null) ? 0 : publisher.hashCode());
		result = prime * result + ((thumbnail == null) ? 0 : thumbnail.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (isbn == null) {
			if (other.isbn != null)
				return false;
		} else if (!isbn.equals(other.isbn))
			return false;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (collocation == null) {
			if (other.collocation != null)
				return false;
		} else if (!collocation.equals(other.collocation))
			return false;
		if (publishedData == null) {
			if (other.publishedData != null)
				return false;
		} else if (!publishedData.equals(other.publishedData))
			return false;
		if (publisher == null) {
			if (other.publisher != null)
				return false;
		} else if (!publisher.equals(other.publisher))
			return false;
		if (thumbnail == null) {
			if (other.thumbnail != null)
				return false;
		} else if (!thumbnail.equals(other.thumbnail))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

}
