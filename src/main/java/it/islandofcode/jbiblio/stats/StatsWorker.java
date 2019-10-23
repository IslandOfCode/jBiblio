package it.islandofcode.jbiblio.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingWorker;

import org.tinylog.Logger;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.settings.Settings;
import it.islandofcode.jbiblio.stats.LoadingUI.WORKTYPE;

public class StatsWorker extends SwingWorker<Object, Object> {
	
	private static final String BOOKSLIST_FILE = "listalibri.ftl";
	private static final String STATISTICS_FILE = "statistiche.ftl";
	
	private WORKTYPE work;
	private File destination;
	
	public StatsWorker(WORKTYPE work, File destination) {
		this.work = work;
		this.destination = new File(destination.getAbsolutePath()+File.separator+generateFileName(work));
	}

	@Override
	protected Object doInBackground() throws Exception {
		Logger.info("JOB " + work.toString() + ", PATH->" + destination.getPath());
		
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_29);
		
		File template = null;
		if(this.work.equals(WORKTYPE.BOOKSLIST)) {
			template = extract2tempFile(BOOKSLIST_FILE);
		} else {
			template = extract2tempFile(STATISTICS_FILE);
		}
		
		cfg.setDirectoryForTemplateLoading(template.toPath().getParent().toFile());

		cfg.setDefaultEncoding("ISO-8859-1");// ("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
		cfg.setLogTemplateExceptions(false);
		cfg.setWrapUncheckedExceptions(true);
		
		Map<String, Object> root = null;
		if(this.work.equals(WORKTYPE.BOOKSLIST)) {
			root = populateBooksList();
		} else {
			root = populateStatistics();
		}
		
		//Altre variabili, in comune ad entrambi i template
		root.put("titoloOwner", Settings.getValue(Settings.PROPERTIES.TITOLO_RESPONSABILE));
		root.put("nomeOwner", Settings.getValue(Settings.PROPERTIES.NOME_RESPONSABILE));
		root.put("schoolName", Settings.getValue(Settings.PROPERTIES.NOME_SCUOLA));
				
		
		Template temp = cfg.getTemplate(template.getName());
		
		File htmlfile = new File(destination.getPath().replace(".pdf", ".html"));
		
		FileOutputStream fos = new FileOutputStream(htmlfile);
		Writer out = new OutputStreamWriter(fos);
		try {
			temp.process(root, out);
		} catch (TemplateException e) {
			Logger.error(e);
			out.close();
			fos.close();
			htmlfile.delete();
		}
		out.close();
		fos.close();
		
		Logger.info("Template processato, file output popolato");

		fos = new FileOutputStream(destination);
		
		ITextRenderer renderer = new ITextRenderer();
		SharedContext sharedContext = renderer.getSharedContext();
		sharedContext.setPrint(true);
		sharedContext.setInteractive(false);
		sharedContext.setReplacedElementFactory(new B64ImgReplacedElementFactory());
		sharedContext.getTextRenderer().setSmoothingThreshold(0);
		try {
			Logger.debug("FILE->"+htmlfile.toURI().toURL().toString() + " | EXIST? " + htmlfile.exists() + " | SIZE->" + htmlfile.length());
			//non ci sono santi, lo vuole proprio così..... File.getPath() non va bene
			renderer.setDocument(htmlfile.toURI().toURL().toString());
			renderer.layout();
			renderer.createPDF(fos);
		} catch (org.xhtmlrenderer.util.XRRuntimeException e) {
			Logger.error(e);
			firePropertyChange("error", null, e.getLocalizedMessage());
			return null;
		} finally {
			try {
				if (fos != null)
					fos.close();

				htmlfile.delete();
				Logger.debug("file html eliminato? " + htmlfile.exists());
			} catch (IOException e) {
				Logger.error(e);
			}
		}

		Logger.info("Rendering pdf completato, PDF size:" + destination.length() + " Bytes");
		
		//Thread.sleep(1000);
		firePropertyChange("done", null, "ASD");
		return null;
	}
	
	private String generateFileName(WORKTYPE work) {
		String toret;
		switch(work) {
		case BOOKSLIST:
			toret = "LISTA_LIBRI_"+LocalDate.now().toString().replace("-", ".")+".pdf";
			break;
		case STATISTICS:
			toret =  "STATISTICHE_"+LocalDate.now().toString().replace("-", ".")+".pdf";
			break;
		default:
			toret = "SCONOSCIUTO_"+LocalDate.now().toString().replace("-", ".")+".pdf";
		}
		return toret;
	}
	
	private static File extract2tempFile(String filename) {
		try (
				InputStream fileStream = StatsWorker.class.getResourceAsStream("/"+filename)
			){
			
			String prefix = filename.substring(0, filename.lastIndexOf("."));
			String suffix = filename.substring(filename.lastIndexOf("."));
			
			File tempFile = File.createTempFile(prefix, suffix);
			tempFile.deleteOnExit();
			
			OutputStream out = new FileOutputStream(tempFile);

			// Write the file to the temp file
			byte[] buffer = new byte[1024];
			int len = fileStream.read(buffer);
			while (len != -1) {
				out.write(buffer, 0, len);
				len = fileStream.read(buffer);
			}
			fileStream.close();
			out.close();
			
			return tempFile;
			
		} catch (IOException e) {
			Logger.error(e);
		}
		
		return null;
	}
	
	private static Map<String, Object> populateBooksList(){
		boolean everyBook = false;
		Map<String, Object> radix = new HashMap<>();
		List<Book> books = DBManager.getBooksAsList(everyBook);
		
		int numOfBooks = (everyBook)?DBManager.countTotalRow("Books"):DBManager.countBookAvailable();
		
		//TODO radix.put("totalelibri", DBManager.countBookAvailable());
		radix.put("totalelibri", numOfBooks);
		radix.put("libririmossi", ", di cui <b>" + DBManager.countBookRemoved() + "</b> ritirati (<i>NB: quelli ritirati non sono mostrati in questa lista</i>)");
		radix.put("listaLibri", books);

		return radix;
	}
	
	private static Map<String, Object> populateStatistics(){
		
		return null;
	}

}
