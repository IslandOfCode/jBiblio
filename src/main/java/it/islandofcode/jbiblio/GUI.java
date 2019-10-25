package it.islandofcode.jbiblio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;
import it.islandofcode.jbiblio.companioapp.qrcode.QrCodeUI;
import it.islandofcode.jbiblio.db.Backup;
import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.settings.PreferenceUI;
import it.islandofcode.jbiblio.stats.LoadingUI;

public class GUI implements IRemoteUpdate{
	
	private static final String TOTALLOANSGAUGEMSG = "<html>\r\n\t<center>\r\n\t\t<h3 style=\"color: #404040\">PRESTITI</h3>\r\n\t\t<p style=\"font-weight: bold; font-size: 150%;\">###</p>\r\n\r\n\t<center>\r\n</html>";
	private static final String TOTALCLIENTGAUGEMSG = "<html>\r\n\t<center>\r\n\t\t<h3 style=\"color: #404040\">CLIENTI</h3>\r\n\t\t<p style=\"font-weight: bold; font-size: 150%;\">###</p>\r\n\r\n\t<center>\r\n</html>";
	private static final String TOTALBOOKGAUGEMSG = "<html>\r\n\t<center>\r\n\t\t<h3 style=\"color: #404040\">&nbsp;&nbsp;TOTALE LIBRI&nbsp;&nbsp;</h3>\r\n\t\t<p style=\"font-weight: bold; font-size: 150%;\">###</p>\r\n\r\n\t<center>\r\n</html>";
	private static final String TOTALBOOKLOANEDGAUGEMSG = "<html>\r\n\t<center>\r\n\t\t<h3 style=\"color: #404040\">LIBRI FUORI</h3>\r\n\t\t<p style=\"font-weight: bold; font-size: 150%;\">###</p>\r\n\r\n\t<center>\r\n</html>";
	
	private static final String APP_STATUS_NOT_CONNECTED = "<html><p style='color:gray;font-style:italic'>App non connessa";
	private static final String APP_STATUS_CONNECTED = "<html><p style='color:green;font-style:italic'>App connessa!";
	
	private JFrame FRAME;
	private JScrollPane SP_latetable;
	private JLabel L_loans;
	private JLabel L_totalclient;
	private JLabel L_totalbook;
	private JLabel L_bookLoaned;
	private JButton B_update;
	private JButton B_resolveSelected;
	
	private JMenuItem MI_connectApp;
	private JLabel L_appStatus;
	
	public HttpHandler HTTPH;
	
	private List<String> openFrames = new ArrayList<>();
	

	/**
	 * Create the application.
	 */
	public GUI() {
		initialize();
		
		populateHomePage();
		
		FRAME.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		FRAME = new JFrame();
		FRAME.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int r = JOptionPane.showConfirmDialog(FRAME, "Sei sicuro di voler uscire dal programma?", "Conferma", JOptionPane.YES_NO_OPTION);
				if(r==JOptionPane.YES_OPTION) {
					
					if(HTTPH!=null) {
						Logger.info("HttpHandler in chiusura...");
						HTTPH.stop();
					}

					Logger.info("CHIUSURA JBIBLIO");
					System.exit(0);
				}
			}
		});
		FRAME.setTitle("jBiblio");
		FRAME.setBounds(100, 100, 771, 402);
		FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		JMenuBar menuBar = new JMenuBar();
		FRAME.setJMenuBar(menuBar);
		
		JMenu M_action = new JMenu("jBiblio");
		menuBar.add(M_action);
		
		JMenuItem MI_close = new JMenuItem("Esci");
		MI_close.setIcon(new ImageIcon(GUI.class.getResource("/icon/esci.png")));
		MI_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
		MI_close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int r = JOptionPane.showConfirmDialog(FRAME, "Sei sicuro di voler uscire dal programma?", "Conferma", JOptionPane.YES_NO_OPTION);
				if(r==JOptionPane.YES_OPTION) {
					Logger.info("CHIUSURA JBIBLIO");
					System.exit(0);
				}
			}
		});
		
		JMenuItem MI_backup = new JMenuItem("Backup database");
		MI_backup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!openFrames.isEmpty()) {
					JOptionPane.showMessageDialog(FRAME, "Prima di continuare, chiudere tutte le finestre aperte.", "Attenzione!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				FRAME.getContentPane().setEnabled(false);
				try {
					Backup.backup(true);
				} catch (IOException e1) {
					Logger.error(e1);
					JOptionPane.showMessageDialog(FRAME, "<html>Impossibile completare l'operazione.<br/><b>"+e1.getMessage()+"</b>", "Errore!", JOptionPane.WARNING_MESSAGE);
					return;
				}
				
				JOptionPane.showMessageDialog(FRAME, "Backup effettuato, puoi continuare a lavorare.", "Operazione completata!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(LoadingUI.class.getResource("/icon/done.png")));
			}
		});
		
		MI_connectApp = new JMenuItem("Connetti App");
		MI_connectApp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if(HTTPH!=null)
						HTTPH.stop();
					HTTPH = new HttpHandler(6339);
					HTTPH.start();
				} catch (IOException e1) {
					Logger.error(e1);
					JOptionPane.showMessageDialog(FRAME, "<html>Errore creazione server!<br/><center><code>"+e1.getMessage(), "Attenzione!", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				HTTPH.registerUI(REGISTER_MODE.CONNECTION, GUI.this);
				
				QrCodeUI QRUI = new QrCodeUI(HTTPH);
				QRUI.setVisible(true);
				Logger.debug("QrCodeUI visible");
			}
		});
		MI_connectApp.setIcon(new ImageIcon(GUI.class.getResource("/icon/remote.png")));
		M_action.add(MI_connectApp);
		MI_backup.setIcon(new ImageIcon(GUI.class.getResource("/icon/backup.png")));
		M_action.add(MI_backup);
		
		JSeparator separator_2 = new JSeparator();
		M_action.add(separator_2);
		
		JMenuItem MI_settings = new JMenuItem("Preferenze");
		MI_settings.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new PreferenceUI();
			}
		});
		MI_settings.setIcon(new ImageIcon(GUI.class.getResource("/icon/gear.png")));
		M_action.add(MI_settings);
		
		JSeparator separator = new JSeparator();
		M_action.add(separator);
		M_action.add(MI_close);
		
		JMenu M_inventory = new JMenu("Inventario");
		menuBar.add(M_inventory);
		
		JLabel lblPrestiti = new JLabel("        Prestiti   ");
		lblPrestiti.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		lblPrestiti.setHorizontalAlignment(SwingConstants.CENTER);
		M_inventory.add(lblPrestiti);
		
		JMenuItem MI_newLoan = new JMenuItem("Nuovo prestito");
		MI_newLoan.setIcon(new ImageIcon(GUI.class.getResource("/icon/new_prestito.png")));
		MI_newLoan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
		MI_newLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchClient S = new SearchClient();
				S.setMode(SearchClient.AFTERSEARCH.LOAN, GUI.this, HTTPH);
			}
		});
		M_inventory.add(MI_newLoan);
		
		JMenuItem MI_resolveLoan = new JMenuItem("Risolvi prestito");
		MI_resolveLoan.setIcon(new ImageIcon(GUI.class.getResource("/icon/risolvi.png")));
		MI_resolveLoan.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
		MI_resolveLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/*ResolveLoan RL =*/ new ResolveLoan(GUI.this, -1, false);
				//RL.setVisible(true);
			}
		});
		M_inventory.add(MI_resolveLoan);
		
		JMenuItem MI_searchLoan = new JMenuItem("Cerca prestiti");
		MI_searchLoan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SearchLoan(GUI.this, HTTPH);
			}
		});
		MI_searchLoan.setIcon(new ImageIcon(GUI.class.getResource("/icon/cerca.png")));
		M_inventory.add(MI_searchLoan);
		
		JLabel lblInventario = new JLabel("        Inventario   ");
		lblInventario.setHorizontalAlignment(SwingConstants.CENTER);
		lblInventario.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		M_inventory.add(lblInventario);
		
		JMenuItem MI_newBook = new JMenuItem("Nuovo libro");
		MI_newBook.setIcon(new ImageIcon(GUI.class.getResource("/icon/add_book.png")));
		MI_newBook.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, KeyEvent.CTRL_DOWN_MASK));
		MI_newBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditBook E = new EditBook();
				E.SetMode(EditBook.BOOKMODE.ADD, "", HTTPH, GUI.this);
			}
		});
		M_inventory.add(MI_newBook);
		
		JMenuItem MI_editBook = new JMenuItem("Modifica libro");
		MI_editBook.setIcon(new ImageIcon(GUI.class.getResource("/icon/cerca.png")));
		MI_editBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchBook S = new SearchBook();
				S.setMode(SearchBook.AFTERSEARCH.EDIT, HTTPH, GUI.this);
			}
		});
		M_inventory.add(MI_editBook);
		
		JMenuItem MI_removeBook = new JMenuItem("Elimina libro");
		MI_removeBook.setIcon(new ImageIcon(GUI.class.getResource("/icon/elimina.png")));
		MI_removeBook.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchBook S = new SearchBook();
				S.setMode(SearchBook.AFTERSEARCH.DELETE, HTTPH, GUI.this);
			}
		});
		M_inventory.add(MI_removeBook);
		
		JLabel lblClienti = new JLabel("        Clienti   ");
		lblClienti.setHorizontalAlignment(SwingConstants.CENTER);
		lblClienti.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		M_inventory.add(lblClienti);
		
		JMenuItem MI_newClient = new JMenuItem("Nuovo cliente");
		MI_newClient.setIcon(new ImageIcon(GUI.class.getResource("/icon/new_client.png")));
		MI_newClient.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
		MI_newClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EditClient C = new EditClient();
				C.setMode(EditClient.CLIENTMODE.ADD, -1, GUI.this);
			}
		});
		M_inventory.add(MI_newClient);
		
		JMenuItem MI_editClient = new JMenuItem("Modifica Cliente");
		MI_editClient.setIcon(new ImageIcon(GUI.class.getResource("/icon/cerca.png")));
		MI_editClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchClient S = new SearchClient();
				S.setMode(SearchClient.AFTERSEARCH.EDIT, GUI.this, HTTPH);
			}
		});
		M_inventory.add(MI_editClient);
		
		JMenuItem MI_removeClient = new JMenuItem("Elimina Cliente");
		MI_removeClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SearchClient S = new SearchClient();
				S.setMode(SearchClient.AFTERSEARCH.DELETE, GUI.this, HTTPH);
			}
		});
		MI_removeClient.setIcon(new ImageIcon(GUI.class.getResource("/icon/elimina.png")));
		M_inventory.add(MI_removeClient);
		
		JMenu M_stats = new JMenu("Statistiche");
		menuBar.add(M_stats);
		
		JMenuItem M_printBookList = new JMenuItem("Genera lista libri");
		M_printBookList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String basePath = System.getProperty("user.home") + "/Desktop";
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(basePath));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Salva lista libri");				
				int res = chooser.showSaveDialog(FRAME);
				
				if(res!=JFileChooser.APPROVE_OPTION) {
					return;
				}

				File destination = new File(chooser.getSelectedFile().getAbsolutePath());
								
				new LoadingUI(GUI.this.FRAME, LoadingUI.WORKTYPE.BOOKSLIST, destination);
			}
		});
		M_printBookList.setIcon(new ImageIcon(GUI.class.getResource("/icon/genera.png")));
		M_stats.add(M_printBookList);
		
		JMenuItem M_printGenericStats = new JMenuItem("Genera statistiche");
		M_printGenericStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String basePath = System.getProperty("user.home") + "/Desktop";
				JFileChooser chooser = new JFileChooser();
				chooser.setCurrentDirectory(new File(basePath));
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setDialogTitle("Salva file Statistiche");		
				int res = chooser.showSaveDialog(FRAME);
				
				if(res!=JFileChooser.APPROVE_OPTION) {
					return;
				}

				File destination = new File(chooser.getSelectedFile().getAbsolutePath());
								
				new LoadingUI(GUI.this.FRAME, LoadingUI.WORKTYPE.STATISTICS, destination);
			}
		});
		M_printGenericStats.setIcon(new ImageIcon(GUI.class.getResource("/icon/genera.png")));
		M_stats.add(M_printGenericStats);
		
		JMenu M_about = new JMenu("?");
		menuBar.add(M_about);
		
		JMenuItem MI_about = new JMenuItem("About");
		MI_about.setIcon(new ImageIcon(GUI.class.getResource("/icon/info.png")));
		MI_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		MI_about.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(FRAME, "<html><center>"
						+ "<span style=\"font-size:150%; font-weight:bold\">jBiblio</span><br/>"
						+ "<span style=\"font-size:120%; font-weight:bold\">di Pier Riccardo Monzo</span><br/>"
						+ "<i>www.islandofcode.it</i><hr/>"
						+ "Un semplice gestore per piccole librerie scolastiche.<br/><br/>Questo programma è distribuito sotto licenza <i>GPLv3</i><br/>"
						+ "https://www.gnu.org/licenses/gpl-3.0.txt</center>", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		M_about.add(MI_about);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		menuBar.add(separator_1);
		
		L_appStatus = new JLabel("<html><p style=\"color:gray;font-style: italic\">App non connessa");
		L_appStatus.setHorizontalAlignment(SwingConstants.TRAILING);
		menuBar.add(L_appStatus);
		FRAME.getContentPane().setLayout(new BorderLayout(0, 0));
		
		JPanel P_gauge = new JPanel();
		FRAME.getContentPane().add(P_gauge, BorderLayout.WEST);
		P_gauge.setLayout(new GridLayout(4, 1, 0, 0));
		
		JPanel P_totalbook = new JPanel();
		P_totalbook.setBorder(new MatteBorder(0, 0, 2, 2, (Color) new Color(0, 0, 0)));
		P_gauge.add(P_totalbook);
		P_totalbook.setLayout(new BorderLayout(0, 0));
		
		L_totalbook = new JLabel();
		L_totalbook.setHorizontalAlignment(SwingConstants.CENTER);
		P_totalbook.add(L_totalbook);
		
		JPanel P_totalclient = new JPanel();
		P_totalclient.setBorder(new MatteBorder(0, 0, 2, 2, (Color) new Color(0, 0, 0)));
		P_gauge.add(P_totalclient);
		P_totalclient.setLayout(new BorderLayout(0, 0));
		
		L_totalclient = new JLabel();
		L_totalclient.setHorizontalAlignment(SwingConstants.CENTER);
		P_totalclient.add(L_totalclient);
		
		JPanel P_loans = new JPanel();
		P_loans.setBorder(new MatteBorder(0, 0, 2, 2, (Color) new Color(0, 0, 0)));
		P_gauge.add(P_loans);
		P_loans.setLayout(new BorderLayout(0, 0));
		
		L_loans = new JLabel();
		L_loans.setHorizontalAlignment(SwingConstants.CENTER);
		P_loans.add(L_loans);
		
		JPanel P_rentedbook = new JPanel();
		P_rentedbook.setBorder(new MatteBorder(0, 0, 0, 2, (Color) new Color(0, 0, 0)));
		P_gauge.add(P_rentedbook);
		P_rentedbook.setLayout(new BorderLayout(0, 0));
		
		L_bookLoaned = new JLabel();
		L_bookLoaned.setHorizontalAlignment(SwingConstants.CENTER);
		P_rentedbook.add(L_bookLoaned);
		
		JPanel P_resultResolve = new JPanel();
		FRAME.getContentPane().add(P_resultResolve, BorderLayout.CENTER);
		P_resultResolve.setLayout(new BorderLayout(0, 0));
		
		SP_latetable = new JScrollPane();
		P_resultResolve.add(SP_latetable, BorderLayout.CENTER);
		SP_latetable.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		JPanel P_resolveCommand = new JPanel();
		P_resultResolve.add(P_resolveCommand, BorderLayout.SOUTH);
		
		B_resolveSelected = new JButton("Risolvi prestito");
		B_resolveSelected.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTable resultTable = (JTable) SP_latetable.getViewport().getView();

				if (resultTable.getSelectedRow() < 0) {
					JOptionPane.showMessageDialog(FRAME, "Nessuna riga selezionata", "Attenzione!",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				DefaultTableModel M = (DefaultTableModel) resultTable.getModel();
				int row = resultTable.getSelectedRow();
				int code = (int) M.getValueAt(row, 0); // suppongo che ISBN sia sempre all'inizio!

				/*ResolveLoan RL = */new ResolveLoan(GUI.this, code, false);
				//RL.setVisible(true);
			}
		});
		B_resolveSelected.setEnabled(false);
		P_resolveCommand.add(B_resolveSelected);
		
		B_update = new JButton("Aggiorna home");
		B_update.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				populateHomePage();
			}
		});
		P_resolveCommand.add(B_update);
	}
	
	private void populateHomePage() {
		if (DBManager.countActiveLoans() <= 0) {
			JPanel P_nores = new JPanel();
			BorderLayout borderL = new BorderLayout();
			P_nores.setLayout(borderL);
			JLabel L_nores = new JLabel(
					"<html><span style=\"font-weight: bold; font-size: 24pt;\">&#x1f4d6;</span> <span style=\"font-style: italic; font-size: 18pt;\">Ancora nessun libro in prestito</span> <span style=\"font-weight: bold; font-size: 24pt;\">&#128530</span>");
			L_nores.setHorizontalAlignment(SwingConstants.CENTER);
			P_nores.add(L_nores, BorderLayout.NORTH);
			SP_latetable.setViewportView(P_nores);
			B_resolveSelected.setEnabled(false);
		} else {
			DefaultTableModel DTM = DBManager.generateOngoingLoanTableModel(true);
			JTable resultTable = new JTable(DTM);
			
			resultTable.setAutoCreateRowSorter(true);
			resultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			SP_latetable.setViewportView(resultTable);
			B_resolveSelected.setEnabled(true);
		}
		L_totalbook.setText(TOTALBOOKGAUGEMSG.replace("###", DBManager.countTotalRow("Books")+""));
		L_totalclient.setText(TOTALCLIENTGAUGEMSG.replace("###", DBManager.countTotalRow("Clients")+""));
		
		
		L_loans.setText(TOTALLOANSGAUGEMSG.replace("###", DBManager.countActiveLoans()+""));
		L_bookLoaned.setText(TOTALBOOKLOANEDGAUGEMSG.replace("###", DBManager.countBookLoaned()+""));
		
		Logger.info("Home page aggiornata.");
	}
	
	protected void signalFrameClosed(String title) {
		this.openFrames.remove(title);
		Logger.debug("FRAME ["+title+"] RIMOSSO, TOTALE #"+openFrames.size());
		populateHomePage();
		/*if(openFrames.isEmpty()) {
			populateHomePage();
		}*/
	}
	
	protected void signalFrameOpened(String title) {
		if(openFrames==null)
			openFrames = new ArrayList<>();
		this.openFrames.add(title);
		Logger.debug("FRAME ["+title+"] AGGIUNTO, TOTALE #"+openFrames.size());
	}

	@Override
	public void appStatusNotification(STATUS status) {

		if(STATUS.CONNECTED.equals(status)) {
			MI_connectApp.setEnabled(false);
			L_appStatus.setText(APP_STATUS_CONNECTED);
			
			Logger.debug("GUI RECEIVED A CONNECT NOTIFICATION");
			
		} else if (STATUS.DISCONNECTED.equals(status)){
			MI_connectApp.setEnabled(true);
			L_appStatus.setText(APP_STATUS_NOT_CONNECTED);
			
			/*
			 * Attenzione, questo codice fa impazzire jbiblio
			 * Sono arrivato a creare 2,4GB di file di log in 2 secondi
			 */
			
			/*while(HTTPH!=null && !HTTPH.isSomeoneConnected()) {
				HTTPH.stop();
			}
			HTTPH = null;*/
			
			Logger.debug("GUI RECEIVED A DISCONNECT NOTIFICATION");
		} else {
			Logger.debug("GUI RECEIVED STATUS: " + status.toString());
		}
		
	}

	@Override
	public void receiveAppMessage(String msg) {
		//do nothing
	}

	@Override
	public String receiveAndRespondAppMessage(String msg) {
		//do nothing
		return null;
	}

	@Override
	public String getRegisterId() {
		return "GUI_frame";
	}

}
