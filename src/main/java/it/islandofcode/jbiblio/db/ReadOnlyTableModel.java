package it.islandofcode.jbiblio.db;

import javax.swing.table.DefaultTableModel;

/**
 * Identico a {@linkplain DefaultTableModel}, ma impedisce la modifica di qualsiasi cella.
 * @author Pier Riccardo Monzo
 */
public class ReadOnlyTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;
	
	public boolean isCellEditable(int rowIndex, int mColIndex) {
        return false;
      }
	
	public static int indexOfColumnByName(DefaultTableModel model, String name) {
		for(int i=0; i<model.getColumnCount(); i++) {
			if(model.getColumnName(i).equals(name)) {
				return  i;
			}
		}
		return -1;
	}
}
