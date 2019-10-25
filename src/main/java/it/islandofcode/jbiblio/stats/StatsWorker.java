package it.islandofcode.jbiblio.stats;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.time.LocalDate;
import java.util.ArrayList;
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
		List<Book> books = DBManager.getAllBooksAsList(everyBook);
		
		int numOfBooks = (everyBook)?DBManager.countTotalRow("Books"):DBManager.countBookAvailable();
		
		//TODO radix.put("totalelibri", DBManager.countBookAvailable());
		radix.put("totalelibri", numOfBooks);
		radix.put("libririmossi", ", di cui <b>" + DBManager.countBookRemoved() + "</b> ritirati (<i>NB: quelli ritirati non sono mostrati in questa lista</i>)");
		radix.put("listaLibri", books);

		return radix;
	}
	
	private static Map<String, Object> populateStatistics(){
		Map<String, Object> radix = new HashMap<>();
		
		radix.put("totLibri", DBManager.countTotalRow("Books"));
		radix.put("totClienti", DBManager.countTotalRow("Clients"));
		radix.put("totPrestiti", DBManager.countTotalRow("Loans"));
		radix.put("totLibriPrestati", DBManager.countTotalRow("BookLoaned"));
		
		radix.put("totLibriPresenti", DBManager.countBookAvailable());
		radix.put("totClientiAttivi", DBManager.countActiveClient());
		radix.put("totPrestitiRisolti", DBManager.countResolvedLoans());
		
		int year = LocalDate.now().getYear();
		
		radix.put("AS2", String.valueOf(year));
		radix.put("AS1", String.valueOf(year-1));
		
		//TODO sostituisci con ciclo e array nome mese
		radix.put("prestitiGEN", DBManager.countLoanMonthYear(1,year));
		radix.put("prestitiFEB", DBManager.countLoanMonthYear(2,year));
		radix.put("prestitiMAR", DBManager.countLoanMonthYear(3,year));
		radix.put("prestitiAPR", DBManager.countLoanMonthYear(4,year));
		radix.put("prestitiMAG", DBManager.countLoanMonthYear(5,year));
		radix.put("prestitiGIU", DBManager.countLoanMonthYear(6,year));
		radix.put("prestitiLUG", DBManager.countLoanMonthYear(7,year));
		radix.put("prestitiAGO", DBManager.countLoanMonthYear(8,year));
		radix.put("prestitiSET", DBManager.countLoanMonthYear(9,year));
		radix.put("prestitiOTT", DBManager.countLoanMonthYear(10,year));
		radix.put("prestitiNOV", DBManager.countLoanMonthYear(11,year));
		radix.put("prestitiDEC", DBManager.countLoanMonthYear(12,year));
		
		radix.put("totPrestitiAnnoCorrente", DBManager.countLoanYear(year));
		
		ArrayList<String> TB = new ArrayList<>();
		Map<String, Integer> tbm = DBManager.top10Book(year);
		int i = 0;
		for(String T : tbm.keySet()) {
			TB.add(i, "<i>"+T+"</i> [prestato <b>"+tbm.get(T)+"</b> volte]");
			i++;
		}
		if(i<9) { //se meno di 10, aggiungi vuoti
			for(int j=i; j<10; j++) {
				TB.add(j, "<i>nd</i>");
			}
		}
		radix.put("topLibri",TB);
		
		ArrayList<String> TC = new ArrayList<>();
		Map<String, Integer> tcm = DBManager.top10Class(year);
		i = 0;
		for(String T : tcm.keySet()) {
			TC.add(i, T.toUpperCase()+" [<b>"+tcm.get(T)+"</b> prestiti]");
			i++;
		}
		if(i<9) { //se meno di 10, aggiungi vuoti
			for(int j=i; j<10; j++) {
				TC.add(j, "<i>nd</i>");
			}
		}
		radix.put("topClassi",TC);
		
		radix.put("avgDurataPrestito", DBManager.averageLoanDuration(year));
		radix.put("avgRitardo", DBManager.averageLoanLateDays(year));
		radix.put("totPrestitiRitardo", DBManager.countLateLoanYear(year));
		
		return radix;
	}

}
