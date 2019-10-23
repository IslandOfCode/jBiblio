package it.islandofcode.jbiblio.db;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.tinylog.Logger;

public class Backup {
	
	private static final String BACKUPS = "backups/";
	
	public static void backup(boolean hashing) throws IOException {
		//String sourceFile = "db/jbiblio.db";
		String destinationFile = BACKUPS+"jbiblio_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")).toString() + ".zip";
		
		File destDir = new File(BACKUPS);
		if(!destDir.exists())
			destDir.mkdir();
		
		Backup.zipTheDB(DBManager.DBFILEPATH+DBManager.DBFILENAME, destinationFile);
		
		if (hashing) {
			File hashFile = new File(destinationFile.replace(".zip", ".txt"));
			BufferedWriter writer = new BufferedWriter(new FileWriter(hashFile));

			String hash = null;
			try {
				hash = sha1(new File(destinationFile));
				writer.write("SHA1 " + hash);
				writer.close();
			} catch (NoSuchAlgorithmException e) {
				Logger.error(e);
				hashFile.delete();
			}
		}
	}

	/**
	 * Se si passa TRUE, verrà creato un file con lo stesso nome dell'archivio ma
	 * con estensione txt, che contiene l'hash dell'archivio, nel formato:<br/>
	 * <center>SHA1 [HASH_DEL_FILE]</center>
	 * 
	 * @param hashing
	 * @throws IOException
	 */
	public static void zipTheDB(String source, String destination) throws IOException {
		
		FileOutputStream fos = new FileOutputStream(destination);
		ZipOutputStream zipOut = new ZipOutputStream(fos);

		File fileToZip = new File(source);
		FileInputStream fis = new FileInputStream(fileToZip);
		ZipEntry zipEntry = new ZipEntry(fileToZip.getName());

		zipOut.putNextEntry(zipEntry);
		byte[] bytes = new byte[1024];
		int length;

		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}

		zipOut.close();
		fis.close();
		fos.close();

	}

	public static String sha1(final File file) throws NoSuchAlgorithmException, IOException {
		final MessageDigest messageDigest = MessageDigest.getInstance("SHA1");

		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			final byte[] buffer = new byte[1024];
			for (int read = 0; (read = is.read(buffer)) != -1;) {
				messageDigest.update(buffer, 0, read);
			}
		}

		// Convert the byte to hex format
		try (Formatter formatter = new Formatter()) {
			for (final byte b : messageDigest.digest()) {
				formatter.format("%02x", b);
			}
			return formatter.toString().toUpperCase();
		}
	}

	public static void copyFile(File source, File dest) throws IOException {
	    Files.copy(source.toPath(), dest.toPath());
	}
}
