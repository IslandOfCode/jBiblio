package it.islandofcode.jbiblio.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableModel;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.artefact.Book;
import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.artefact.Loan;

public class DBManager {

	public static final String DBFILEPATH = "db/";
	public static final String DBFILENAME = "jbiblio.db";
	
	public static void createDB() {
		String URL = "jdbc:sqlite:"+DBFILEPATH+DBFILENAME;
		
		try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                DatabaseMetaData meta = conn.getMetaData();
                Logger.info("Nuovo database creato, con driver ["+meta.getDriverName()+"]");
            }
 
        } catch (SQLException e) {
            Logger.error(e);
        }
	}
	
	public static final void initDB() {		
		String URL = "jdbc:sqlite:"+DBFILEPATH+DBFILENAME;

		//String BOOKS = "CREATE TABLE `Books` ( `ISBN` TEXT NOT NULL UNIQUE, `title` TEXT, `author` TEXT, `publisher` TEXT, `publishdate` TEXT, `thumbnail` TEXT, `collocation` TEXT, `removed` INTEGER NOT NULL DEFAULT 0 );";
		String BOOKS = "CREATE TABLE `Books` ( `ISBN` TEXT NOT NULL, `title` TEXT, `author` TEXT, `publisher` TEXT, `publishdate` TEXT, `thumbnail` TEXT, `collocation` TEXT NOT NULL UNIQUE, `removed` INTEGER NOT NULL DEFAULT 0 );";
		String CLIENTS = "CREATE TABLE `Clients` ( `ID` INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE, `nome` TEXT, `cognome` TEXT, `classe` INTEGER, `sezione` TEXT, `removed` INTEGER NOT NULL DEFAULT 0 );";
		String LOANS = "CREATE TABLE \"Loans\" ( `ID` INTEGER PRIMARY KEY AUTOINCREMENT, `client` INTEGER, `dataS` TEXT, `dataE` TEXT, `dataR` TEXT, `returned` INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(`client`) REFERENCES `Clients`(`ID`) )";
		String BOOKLOANED = "CREATE TABLE `BookLoaned` ( `loanID` INTEGER NOT NULL, `bookISBN` TEXT NOT NULL, FOREIGN KEY(`loanID`) REFERENCES `Loans`(`ID`), FOREIGN KEY(`bookISBN`) REFERENCES `Books`(`ISBN`) )";
        
        try (Connection conn = DriverManager.getConnection(URL);
                Statement stmt = conn.createStatement()) {
            stmt.execute(BOOKS);
            conn.createStatement();
            stmt.execute(CLIENTS);
            conn.createStatement();
            stmt.execute(LOANS);
            conn.createStatement();
            stmt.execute(BOOKLOANED);
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            Logger.error(e);
        }
	}
	
	public static Connection connectDB() {
        try {
            return DriverManager.getConnection("jdbc:sqlite:"+DBFILEPATH+DBFILENAME);           
        } catch (SQLException e) {
            Logger.error(e);
        }
		return null;
	}
		
	private static int countTotalRow(String table, String where) {
		String sql = "SELECT COUNT() FROM " + table;
		if(where!=null && !where.isEmpty()) {
			sql += " WHERE " + where;
		}
		try (Connection conn = connectDB();) {

			ResultSet rs = conn.createStatement().executeQuery(sql);

			if(rs.next()) {
				return rs.getInt(1);
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return 0;
	}
	
	/**
	 * Questo metodo fa da filtro al metodo originale, nel caso in cui si volesse passare una sql
	 * malevola al metodo. I vari replace in table servono per evitare proprio questo.
	 * @param table
	 * @return
	 */
	public static int countTotalRow(String table) {
		return DBManager.countTotalRow(table.replace(" ", "").replace("(", ""),null);
	}
	
	public static int countBookAvailable() {
		return DBManager.countTotalRow("Books", "removed=0");
	}
	
	public static int countBookRemoved() {
		return DBManager.countTotalRow("Books", "removed=1");
	}
	
	public static int countBookLoaned() {
		return DBManager.countTotalRow("Loans inner join BookLoaned on Loans.ID = BookLoaned.loanID", "Loans.returned=0");
	}
	
	public static int countActiveLoans() {
		return DBManager.countTotalRow("Loans", "Loans.returned=0");
	}
	
	/**
	 * FALSE, verranno considerati ANCHE i prestiti già risolti. TRUE solo i prestiti attivi (anche in ritardo).
	 * @param getAll
	 * @return DefaultTableModel
	 */
	public static DefaultTableModel generateOngoingLoanTableModel(boolean getAll) {
		//Se devi modificare questa query, accendi un cero ad ogni chiesa nel raggio di 50 km.
		//Poi, bestemmia.
		String sql = "select Loans.ID as \"Codice Prestito\", Clients.ID as 'ID Cliente', ( Clients.cognome || \", \" || Clients.nome ) as 'Nominativo', ( Clients.classe || Clients.sezione ) as 'Classe', strftime('%d/%m/%Y',Loans.dataE) as \"Fine prestito\", ( julianday(date('now'))-julianday(Loans.dataE) ) as DAYS_LATE, Loans.returned as 'RETURNED' from Clients inner join Loans on Loans.client=Clients.ID";
		
		if(getAll)
			sql += " where Loans.returned=0";
		
		int DAYS_INDEX = 5;
		int DATE_END_INDEX = 4;
		int RETURNED_INDEX = 6;
		String DAYS_LATE_NAME = "DAYS_LATE";
		String RETURNED_NAME = "RETURNED";
		
		try (Connection conn = connectDB()) {
			
			ResultSet rs = conn.createStatement().executeQuery(sql);
			
			ResultSetMetaData metaData = rs.getMetaData();

		    // names of columns
		    Vector<String> columnNames = new Vector<String>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		    	if(metaData.getColumnName(column).equals(DAYS_LATE_NAME)) {
		    		columnNames.add("Stato prestito");
		    	} else if(metaData.getColumnName(column).equals(RETURNED_NAME)) {
		    		//questa colonna non verrà considerata, serve solo per generare dati aggiuntivi
		    		continue;
		    	} else {
		    		columnNames.add(metaData.getColumnName(column));
		    	}
		    }
		    
			
		    List<Vector<Object>> data = new ArrayList<>();
			//Costruisci la lista
			while (rs.next()) {
				Vector<Object> vector = new Vector<Object>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		            vector.add(rs.getObject(columnIndex));
		        }
		        data.add(vector);
			}
			
			//Ordina la lista
			data = data.stream().sorted(new Comparator<Vector<Object>>() {

				@Override
				public int compare(Vector<Object> o1, Vector<Object> o2) {
					if(o1.get(DAYS_INDEX).equals(o2.get(DAYS_INDEX))) { //se il numero di giorni è lo stesso
						//compara le date
						return (new DBDate((String)o1.get(DATE_END_INDEX))).toString().compareTo((new DBDate((String)o2.get(DATE_END_INDEX))).toString());
					} else { //altrimenti, ordina in modo decrescente (i numeri positivi sono i giorni di ritardo)
						return Double.compare((double)o2.get(DAYS_INDEX), (double)o1.get(DAYS_INDEX));
					}
				}

			}).collect(Collectors.toCollection(ArrayList::new));
			
			//Costruisci il vettore di vettori per il modello
			Vector<Vector<Object>> dataForModel = new Vector<Vector<Object>>();
			
			for(int e=0; e<data.size(); e++) {
				Vector<Object> E = data.get(e);
				//modifica la colonna DAYS_LATE
				double days = ((double)E.get(DAYS_INDEX));
				
				if( !getAll && ((int)E.get(RETURNED_INDEX))==1) {
					E.set(DAYS_INDEX, "<html><span style='color:#666666; font-size: 14pt; font-style:italic'>Risolto</span></html>");
				} else if( days==0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#FF8C00; font-size: 14pt; font-weight:bold'>Ultimo giorno</span></html>");
				} else if( days<0 && days>=-3.0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#CAD100; font-size: 14pt; font-weight:bold'>In scadenza</span></html>");
				} else if( days<=-4.0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#00A800; font-size: 14pt; font-weight:bold'>In orario</span></html>"); //; background-color:#000000
				} else {
					E.set(DAYS_INDEX, "<html><span style='color:#FF0000; font-size: 14pt; font-weight:bold'>Scaduto!</span></html>");
				}
				
				dataForModel.add(e, E);
			}
			
			DefaultTableModel DTM = new ReadOnlyTableModel();
			
			if(data.size()>0) {
				DTM.setColumnIdentifiers(columnNames);
				DTM.setDataVector(dataForModel, columnNames);
				return DTM;
			}
			
			
		} catch (SQLException e) {
			Logger.error(e);
		}

		return null;
	}

	/*
	 * ######################## LIBRI
	 */
	public static DefaultTableModel searchBooksAsTableModel(String ISBN, String title, String collocation, boolean getall) {
		boolean I = false,
				T = false,
				C = false;
		String sql = "SELECT ISBN, title as Titolo, author as Autore, publisher as 'Casa ed.', publishdate as Data, thumbnail as Anteprima, collocation as 'Coll.', removed as Presente FROM Books WHERE";

		if (!getall) {
			if (ISBN != null && !ISBN.isEmpty()) {
				sql += " ISBN LIKE ?";
				I = true;
			}

			if (title != null && !title.isEmpty()) {
				if (I)
					sql += " AND";
				sql += " title LIKE ?";
				T = true;
			}

			if (collocation != null && !collocation.isEmpty()) {
				if (I || T)
					sql += " AND";
				sql += " collocation LIKE ?";
				C = true;
			}
		} else {
			sql = sql.replace(" WHERE", ";");
		}

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			//pstmt
			int i=1;
			if(I) {
				pstmt.setString(i, "%"+ISBN+"%");
				i++;
			}
			if(T) {
				pstmt.setString(i, "%"+title+"%");
				i++;
			}
			if(C) {
				pstmt.setString(i, "%"+collocation.toUpperCase()+"%");
			}
			
			ResultSet rs = pstmt.executeQuery();

			
			ResultSetMetaData metaData = rs.getMetaData();
			
		    // names of columns
		    Vector<String> columnNames = new Vector<String>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		        columnNames.add(metaData.getColumnName(column));
		    }
			
		    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			// loop through the result set
			while (rs.next()) {
				Vector<Object> vector = new Vector<Object>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		        	if(metaData.getColumnName(columnIndex).equals("Anteprima")) {
		        		if(rs.getObject(columnIndex)!=null && !((String)rs.getObject(columnIndex)).isEmpty()) {
		        			vector.add("Presente");
		        		} else {
		        			vector.add("Assente");
		        		}
		        	} else if(metaData.getColumnName(columnIndex).equals("Presente")) {
		        		if(((int)rs.getObject(columnIndex))==0) {
		        			vector.add("Si");
		        		} else {
		        			vector.add("RIMOSSO");
		        		}
		        	} else {
		        		vector.add(rs.getObject(columnIndex));
		        	}
		        }
		        data.add(vector);
			}
			
			//impedisci la modifica delle celle
			/*DefaultTableModel DTM = new DefaultTableModel() {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
			        return false;
			      }
			};*/
			DefaultTableModel DTM = new ReadOnlyTableModel();
			
			if(data.size()>0) {
				DTM.setColumnIdentifiers(columnNames);
				DTM.setDataVector(data, columnNames);
				return DTM;
			}
			
			//if(data.size()>0)
			//	return new DefaultTableModel(data, columnNames);
			
		} catch (SQLException e) {
			Logger.error(e);
		}

		return null;
	}
	
	/**
	 * Il parametro permette di scegliere se ritornare tutti i libri (TRUE),
	 * oppure ignorare quelli che sono stati marcati come scartati
	 * (cioè persi, cestinati perchè troppo rovinati oppure distrutti, ma
	 * che comunque sono ancora associati ad un prestito e quindi non del tutto
	 * rimuovibili).
	 * @param flag
	 * @return
	 */
	public static List<Book> getAllBooksAsList(boolean AllBook){
		ArrayList<Book> L = new ArrayList<>();
		
		String sql = "SELECT * FROM Books";
		
		if(!AllBook) {
			sql += " WHERE removed=0";
		}
		
		try (Connection conn = connectDB()) {
			
			ResultSet rs = conn.createStatement().executeQuery(sql);

			while(rs.next()) {
				L.add(new Book(
							rs.getString("ISBN"),
							rs.getString("title"),
							rs.getString("author"),
							rs.getString("publisher"),
							rs.getString("publishdate"),
							rs.getString("thumbnail"),
							rs.getString("collocation"),
							rs.getInt("removed")
						)
					);
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return L;
	}
	
	public static Book getBookByISBN(String iSBN) {
		String sql = "SELECT * FROM Books WHERE ISBN=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setString(1, iSBN);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			if(rs.next()) {

				return new Book(
						rs.getString("ISBN"),
						rs.getString("title"),
						rs.getString("author"),
						rs.getString("publisher"),
						rs.getString("publishdate"),
						rs.getString("thumbnail"),
						rs.getString("collocation"),
						rs.getInt("removed")
						);

			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static List<Book> getBookListByISBN(String iSBN) {
		ArrayList<Book> L = new ArrayList<>();
		String sql = "SELECT * FROM Books WHERE ISBN=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setString(1, iSBN);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			while(rs.next()) {

				L.add( new Book(
						rs.getString("ISBN"),
						rs.getString("title"),
						rs.getString("author"),
						rs.getString("publisher"),
						rs.getString("publishdate"),
						rs.getString("thumbnail"),
						rs.getString("collocation"),
						rs.getInt("removed")
						)
					);

			}
			if(!L.isEmpty())
				return L;
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static Book getBookByCollocation(String collocation) {
		String sql = "SELECT * FROM Books WHERE collocation=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setString(1, collocation);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			if(rs.next()) {

				return new Book(
						rs.getString("ISBN"),
						rs.getString("title"),
						rs.getString("author"),
						rs.getString("publisher"),
						rs.getString("publishdate"),
						rs.getString("thumbnail"),
						rs.getString("collocation"),
						rs.getInt("removed")
						);

			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static Book getThatPreciseBook(String ISBN, String collocation) {
		String sql = "SELECT * FROM Books WHERE ISBN=? and collocation=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setString(1, ISBN);
			pstmt.setString(2, collocation);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			if(rs.next()) {

				return new Book(
						rs.getString("ISBN"),
						rs.getString("title"),
						rs.getString("author"),
						rs.getString("publisher"),
						rs.getString("publishdate"),
						rs.getString("thumbnail"),
						rs.getString("collocation"),
						rs.getInt("removed")
						);

			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static boolean addNewBook(Book B) {
		String sql = "INSERT INTO Books(ISBN,title,author,publisher,publishdate,thumbnail,collocation) VALUES(?,?,?,?,?,?,?)";
		 
        try (Connection conn = connectDB();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, B.getISBN());
            pstmt.setString(2, B.getTitle());
            pstmt.setString(3, B.getAuthor());
            pstmt.setString(4, B.getPublisher());
            pstmt.setString(5, B.getPublishedData());
            pstmt.setString(6, B.getThumbnail());
            pstmt.setString(7, B.getCollocation());
            //Non c'è bisogno di inserire removed perchè di default va a 0
            pstmt.executeUpdate();
            
            return pstmt.getUpdateCount()>0;
            
        } catch (SQLException e) {
            Logger.error(e);
        }
		return false;
	}
	
	public static boolean updateBook(String ISBN, Book B) {
		String sql = "UPDATE Books SET ISBN = ? ,title = ? ,author = ? ,publisher = ? ,publishdate = ? ,thumbnail = ? ,collocation = ? WHERE ISBN = ?";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, B.getISBN());
            pstmt.setString(2, B.getTitle());
            pstmt.setString(3, B.getAuthor());
            pstmt.setString(4, B.getPublisher());
            pstmt.setString(5, B.getPublishedData());
            pstmt.setString(6, B.getThumbnail());
            pstmt.setString(7, B.getCollocation());
          //Non c'è bisogno di inserire removed perchè questo viene aggiornato solo dalla funzione di cancellazione libro
            pstmt.setString(8, ISBN);
			// update
			pstmt.executeUpdate();
			
			return pstmt.getUpdateCount()>0;
		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}

	public static int removeBook(String ISBN)/* throws EntityAlreadyReferencedException */{
		String sqlremove = "DELETE FROM Books WHERE ISBN = ?";
		String sqlupdate = "UPDATE Books SET removed = 1 WHERE ISBN = ?";
		String sqlcheck = "SELECT COUNT() FROM BookLoaned WHERE bookISBN = \""+ISBN+"\";";
		
		try (Connection conn = connectDB();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlcheck);
				PreparedStatement pstDel = conn.prepareStatement(sqlremove);
				PreparedStatement pstUpd = conn.prepareStatement(sqlupdate)) {

			if(rs.next() && rs.getInt(1)>0) {
				pstUpd.setString(1, ISBN);
				return pstUpd.executeUpdate();
			}
			
			// set the corresponding param
			pstDel.setString(1, ISBN);
			// execute the delete statement
			return pstDel.executeUpdate();

		} catch (SQLException e) {
			Logger.error(e);
		}
		return -1;
	}
	
	/**
	 * Ritorna TRUE se il libro indicato dalla collocazione passata come parametro è già presente
	 * nel database. FALSE altrimenti.
	 * @param ISBN
	 * @return boolean
	 */
	public static boolean checkBookAlreadyPresent(String collocation) {
		String sql = "SELECT ISBN FROM Books WHERE Collocation=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, collocation);
			
			ResultSet rs = pstmt.executeQuery();

			return rs.next();
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}
	
	public static boolean checkISBNRemoved(String ISBN) {
		String sql = "SELECT removed FROM Books WHERE ISBN=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, ISBN);
			
			ResultSet rs = pstmt.executeQuery();
			
			if(rs.next()) {
				return (rs.getInt("removed")==1);
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}
	
	/*
	 * ######################## CLIENTI
	 */
	
	public static DefaultTableModel searchClientsAsTableModel(String nome, String cognome, int classe, String sezione, boolean getall) {
		boolean N = false,
				CG = false,
				C = false,
				S = false;
		
		String sql = "SELECT ID, nome as Nome, cognome as Cognome, classe as Classe, sezione as 'Sez.', removed as Presente FROM Clients WHERE";

		if (!getall) {
			if (nome != null && !nome.isEmpty()) {
				sql += " nome LIKE ?";
				N = true;
			}

			if (cognome != null && !cognome.isEmpty()) {
				if (N)
					sql += " AND";
				sql += " cognome LIKE ?";
				CG = true;
			}
			
			if(classe!=0) {
				if(N||CG)
					sql += " AND";
				sql += " classe=?";
				C = true;
			}

			if (sezione != null && !sezione.isEmpty()) {
				if (N||CG||C)
					sql += " AND";
				sql += " sezione=?";
				S = true;
			}
		} else {
			sql = sql.replace(" WHERE", ";");
		}

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			//pstmt
			int i=1;
			if(N) {
				pstmt.setString(i, "%"+nome+"%");
				i++;
			}
			if(CG) {
				pstmt.setString(i, "%"+cognome);
				i++;
			}
			
			if(C) {
				pstmt.setInt(i, classe);
				i++;
			}
			
			if(S) {
				pstmt.setString(i, sezione.toUpperCase());
			}
			
			ResultSet rs = pstmt.executeQuery();
			
			ResultSetMetaData metaData = rs.getMetaData();

		    // names of columns
		    Vector<String> columnNames = new Vector<String>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		        columnNames.add(metaData.getColumnName(column));
		    }
			
		    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
			// loop through the result set
			while (rs.next()) {
				Vector<Object> vector = new Vector<Object>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		        	if(metaData.getColumnName(columnIndex).equals("Presente")) {
		        		if(rs.getInt(columnIndex)==1) {
		        			vector.add("RIMOSSO");
		        		} else {
		        			vector.add("Si");
		        		}
		        		continue;
		        	}
		            vector.add(rs.getObject(columnIndex));
		        }
		        data.add(vector);
			}
			
			//impedisci la modifica delle celle
			/*DefaultTableModel DTM = new DefaultTableModel() {
				private static final long serialVersionUID = 1L;

				public boolean isCellEditable(int rowIndex, int mColIndex) {
			        return false;
			      }
			};*/
			DefaultTableModel DTM = new ReadOnlyTableModel();
			
			if(data.size()>0) {
				DTM.setColumnIdentifiers(columnNames);
				DTM.setDataVector(data, columnNames);
				return DTM;
			}
			
			//if(data.size()>0)
			//	return new DefaultTableModel(data, columnNames);
			
		} catch (SQLException e) {
			Logger.error(e);
		}

		return null;
	}
	
	public static Client getClientByID(int ID) {
		String sql = "SELECT * FROM Clients WHERE ID=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setInt(1, ID);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			if(rs.next()) {

				return new Client(
						rs.getInt("ID"),
						rs.getString("nome"),
						rs.getString("cognome"),
						rs.getInt("classe"),
						rs.getString("sezione"),
						rs.getInt("removed")
						);
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static boolean addNewClient(Client C) {
		String sql = "INSERT INTO Clients(nome,cognome,classe,sezione) VALUES(?,?,?,?)";
		 
        try (Connection conn = connectDB();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, C.getNome());
            pstmt.setString(2, C.getCognome());
            pstmt.setInt(3, C.getClasse());
            pstmt.setString(4, C.getSezione());

            pstmt.executeUpdate();
            
            return pstmt.getUpdateCount()>0;
            
        } catch (SQLException e) {
            Logger.error(e);
        }
		return false;
	}
	
	public static boolean updateClient(int ID, Client C) {
		String sql = "UPDATE Clients SET ID = ?, nome = ? ,cognome = ? ,classe = ? ,sezione = ? WHERE ID = ?";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setInt(1, C.getID());
            pstmt.setString(2, C.getNome());
            pstmt.setString(3, C.getCognome());
            pstmt.setInt(4, C.getClasse());
            pstmt.setString(5, C.getSezione());
            pstmt.setInt(6, ID);
			// update
			pstmt.executeUpdate();
			
			return pstmt.getUpdateCount()>0;
		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}
	
	public static int removeClient(int i) /*throws EntityAlreadyReferencedException */{
		String sqlremove = "DELETE FROM Clients WHERE ID = ?";
		String sqlupdate = "UPDATE Clients SET removed = 1 WHERE ID = ?";
		String sqlcheck = "SELECT COUNT() FROM Loans WHERE client = "+i;
		
		try (Connection conn = connectDB();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sqlcheck);
				PreparedStatement pstDel = conn.prepareStatement(sqlremove);
				PreparedStatement pstUpd = conn.prepareStatement(sqlupdate)) {

			if(rs.next() && rs.getInt(1)>0) {
				pstUpd.setInt(1, i);
				return pstUpd.executeUpdate();
			}
			
			// set the corresponding param
			pstDel.setInt(1, i);
			// execute the delete statement
			return pstDel.executeUpdate();

		} catch (SQLException e) {
			Logger.error(e);
		}
		return -1;
	}

	/*
	 * ######################## PRESTITO
	 */
	
	public static boolean checkIfLoanReturned(int loanID) {
		String sql = "select Loans.returned from Loans where Loans.ID=?";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

			pstmt.setInt(1, loanID);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				return rs.getInt(1)==1;
			}

		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}

	public static boolean checkIfBookLoaned(String ISBN, String collocation) {
		String sql = "select BookLoaned.bookISBN as 'ISBN', Books.collocation as 'collocation' from Loans inner join ( BookLoaned inner join Books on BookLoaned.bookISBN = Books.ISBN) on Loans.ID = BookLoaned.loanID where Loans.returned=0 and (BookLoaned.bookISBN=? or Books.collocation=?)";
		
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {

			pstmt.setString(1, ISBN);
			pstmt.setString(2, collocation);

			ResultSet rs = pstmt.executeQuery();

			return rs.next();

		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}
	
	public static int addNewLoan(Loan L) {
		/*"CREATE TABLE \"Loans\" (
			`ID` INTEGER PRIMARY KEY AUTOINCREMENT,
			 `client` INTEGER,
			  `dataS` TEXT,
			   `dataE` TEXT,
			    `returned` INTEGER NOT NULL DEFAULT 0,
			     FOREIGN KEY(`client`) REFERENCES `Clients`(`ID`)
			)";
		*/
		/*
		 "CREATE TABLE `BookLoaned` (
		  `loanID` INTEGER NOT NULL,
		   `bookISBN` TEXT NOT NULL,
		    FOREIGN KEY(`loanID`) REFERENCES `Loans`(`ID`),
		     FOREIGN KEY(`bookISBN`) REFERENCES `Books`(`ISBN`) )";
		*/
		String sql = "INSERT INTO Loans(client,dataS,dataE,dataR,returned) VALUES(?,?,?,?,?)";
		//String sqlID = "SELECT last_insert_rowid()";
		String sqlBook = "INSERT INTO BookLoaned(loanID,bookISBN) VALUES(?,?)";
		int returnedID = -1;
		 
        try (Connection conn = connectDB();
                PreparedStatement pstmt = conn.prepareStatement(sql);
        		PreparedStatement pstmB = conn.prepareStatement(sqlBook);
                		/*Statement stmt = conn.createStatement()*/) {
            pstmt.setInt(1, L.getClient());
            pstmt.setString(2, L.getDateStart());
            pstmt.setString(3, L.getDateEnd());
            pstmt.setString(4, L.getDateReturned()); //o, in alternativa, stringa vuota
            pstmt.setInt(5, L.getReturned());

            pstmt.executeUpdate();
            
            ResultSet rs = pstmt.getGeneratedKeys();
            if(rs.next()) {
            	returnedID = rs.getInt(1);
            	int HOWMANY = rs.getMetaData().getColumnCount();
            	Logger.debug("ID GENERATO -> ID:"+returnedID+" (colonne:"+HOWMANY+")");
            }
            
            
            /*stmt.execute(sqlID);
            rs =  stmt.getResultSet();
            if(rs.next()) {
            	returnedID = rs.getInt(1);
            	Logger.debug("RETURNEDID è stato impostato a "+returnedID);
            }*/
           
            //aggiungi i libri al dB
            int upcount = 0;
            for(Book B : L.getBooks()){
            	pstmB.setInt(1,returnedID);
            	pstmB.setString(2, B.getISBN());
            	upcount = pstmB.executeUpdate();
            	Logger.debug(B.getISBN() + " aggiunto? ["+upcount+"]");
            }
            
        } catch (SQLException e) {
            Logger.error(e);
        }
		return returnedID;
	}
	
	public static List<Book> getBookLoanedAsList(int loanID) {
		ArrayList<Book> AL = new ArrayList<>();
		
		String sql = "select Books.ISBN from BookLoaned inner join Books on BookLoaned.bookISBN = Books.ISBN and BookLoaned.loanID = ?;";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql);) {
			
			pstmt.setInt(1, loanID);
			
			ResultSet rs = pstmt.executeQuery();
			
			while(rs.next()) {
				AL.add(	DBManager.getBookByISBN(rs.getString(1)) );
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}

		return AL;
	}
	
	public static Loan getLoan(int loanID) {
		String sql = "SELECT * FROM Loans WHERE ID=?";
		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			// set the value
			pstmt.setInt(1, loanID);
			
			ResultSet rs = pstmt.executeQuery();

			// loop through the result set
			if(rs.next()) {

				List<Book> BS = DBManager.getBookLoanedAsList(loanID);
				return new Loan(
						rs.getInt("ID"),
						rs.getInt("client"),
						rs.getString("dataS"),
						rs.getString("dataE"),
						rs.getString("dataR"),
						BS,
						rs.getInt("returned")
						);
			}
			
		} catch (SQLException e) {
			Logger.error(e);
		}
		return null;
	}
	
	public static boolean resolveLoan(Loan L) {
		String sql = "UPDATE Loans SET dataR = ?, returned = ? WHERE ID = ?";

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

			
			pstmt.setString(1, L.getDateReturned());
			pstmt.setInt(2, L.getReturned());
			pstmt.setInt(3, L.getID());
			// update
			pstmt.executeUpdate();

			return pstmt.getUpdateCount() > 0;
		} catch (SQLException e) {
			Logger.error(e);
		}
		return false;
	}

	public static DefaultTableModel searchLoansAsTableModel(String ISBN, String title, String collocation, String name, int classe, String sezione, boolean resolved) {
		
		int DAYS_INDEX = 5;
		int DATE_END_INDEX = 4;
		int RETURNED_INDEX = 6;
		String DAYS_LATE_NAME = "DAYS_LATE";
		String RETURNED_NAME = "RETURNED";
		
		String sql = "select" + 
				"	Loans.ID as 'Codice Prestito'," + 
				"	Clients.ID as 'ID Cliente'," + 
				"	( Clients.cognome || ', ' || Clients.nome ) as 'Nominativo'," + 
				"	( Clients.classe || Clients.sezione ) as 'Classe'," + 
				"	strftime('%d/%m/%Y',Loans.dataE) as 'Fine prestito'," + 
				"	( julianday(date('now'))-julianday(Loans.dataE) ) as DAYS_LATE," + 
				"	Loans.returned as 'RETURNED'" + 
				" from Clients inner join Loans" + 
				"	on Loans.client=Clients.ID {RESOLVED}" + 
				" where" + 
				"	Loans.ID = (" + 
				"		select BookLoaned.loanID as LOANID" + 
				"		from Books inner join BookLoaned" + 
				"			on Books.ISBN == BookLoaned.bookISBN" + 
				"		where Books.ISBN like ? and Books.title like ? and Books.collocation like ?" + 
				"	) and" + 
				"	( Clients.cognome || ' ' || Clients.nome ) like ? and" + 
				"	Clients.classe like ? and" + 
				"	Clients.sezione like ?";

		if(resolved)
			sql = sql.replace("{RESOLVED}", "");
		else
			sql = sql.replace("{RESOLVED}", "and Loans.returned=0");

		try (Connection conn = connectDB(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
			
			pstmt.setString(1, "%"+ISBN+"%");
			pstmt.setString(2, "%"+title+"%");
			pstmt.setString(3, "%"+collocation+"%");
			pstmt.setString(4, "%"+name+"%");
			pstmt.setString(5, "%"+( (classe<=0)?"":classe )+"%");
			pstmt.setString(6, "%"+sezione+"%");
			
			ResultSet rs = pstmt.executeQuery();
			
			ResultSetMetaData metaData = rs.getMetaData();

			// names of columns
		    Vector<String> columnNames = new Vector<String>();
		    int columnCount = metaData.getColumnCount();
		    for (int column = 1; column <= columnCount; column++) {
		    	if(metaData.getColumnName(column).equals(DAYS_LATE_NAME)) {
		    		columnNames.add("Stato prestito");
		    	} else if(metaData.getColumnName(column).equals(RETURNED_NAME)) {
		    		//questa colonna non verrà considerata, serve solo per generare dati aggiuntivi
		    		continue;
		    	} else {
		    		columnNames.add(metaData.getColumnName(column));
		    	}
		    }
		    
			
		    List<Vector<Object>> data = new ArrayList<>();
			//Costruisci la lista
			while (rs.next()) {
				Vector<Object> vector = new Vector<Object>();
		        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
		            vector.add(rs.getObject(columnIndex));
		        }
		        data.add(vector);
			}
			
			//Ordina la lista
			data = data.stream().sorted(new Comparator<Vector<Object>>() {

				@Override
				public int compare(Vector<Object> o1, Vector<Object> o2) {
					if(o1.get(DAYS_INDEX).equals(o2.get(DAYS_INDEX))) { //se il numero di giorni è lo stesso
						//compara le date
						return (new DBDate((String)o1.get(DATE_END_INDEX))).toString().compareTo((new DBDate((String)o2.get(DATE_END_INDEX))).toString());
					} else { //altrimenti, ordina in modo decrescente (i numeri positivi sono i giorni di ritardo)
						return Double.compare((double)o2.get(DAYS_INDEX), (double)o1.get(DAYS_INDEX));
					}
				}

			}).collect(Collectors.toCollection(ArrayList::new));
			
			//Costruisci il vettore di vettori per il modello
			Vector<Vector<Object>> dataForModel = new Vector<Vector<Object>>();
			
			for(int e=0; e<data.size(); e++) {
				Vector<Object> E = data.get(e);
				//modifica la colonna DAYS_LATE
				double days = ((double)E.get(DAYS_INDEX));
				
				if( resolved && ((int)E.get(RETURNED_INDEX))==1) {
					E.set(DAYS_INDEX, "<html><span style='color:#666666; font-size: 14pt; font-style:italic'>Risolto</span></html>");
				} else if( days==0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#FF8C00; font-size: 14pt; font-weight:bold'>Ultimo giorno</span></html>");
				} else if( days<0 && days>=-3.0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#CAD100; font-size: 14pt; font-weight:bold'>In scadenza</span></html>");
				} else if( days<=-4.0 ) {
					E.set(DAYS_INDEX, "<html><span style='color:#00A800; font-size: 14pt; font-weight:bold'>In orario</span></html>"); //; background-color:#000000
				} else {
					E.set(DAYS_INDEX, "<html><span style='color:#FF0000; font-size: 14pt; font-weight:bold'>Scaduto!</span></html>");
				}
				
				dataForModel.add(e, E);
			}
			
			DefaultTableModel DTM = new ReadOnlyTableModel();
			
			if(data.size()>0) {
				DTM.setColumnIdentifiers(columnNames);
				DTM.setDataVector(dataForModel, columnNames);
				return DTM;
			}
			
			//if(data.size()>0)
			//	return new DefaultTableModel(data, columnNames);
			
		} catch (SQLException e) {
			Logger.debug(sql);;
			Logger.error(e);
		}

		return null;
	}
}
