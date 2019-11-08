package it.islandofcode.jbiblio.settings;

import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.db.DBManager;
import it.islandofcode.jbiblio.settings.Settings.PROPERTIES;
import it.islandofcode.jbiblio.stats.LoadingUI;

import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class PreferenceUI extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_name;
	private JTextField TXT_school;
	private JComboBox<String> CB_title;
	private JComboBox<Integer> CB_lenght;

	private Map<Settings.PROPERTIES, String> oldValue;
	
	/**
	 * Create the frame.
	 */
	public PreferenceUI() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				
				Map<Settings.PROPERTIES, String> newValue = createMap();
				if(!Settings.mapValueEquals(oldValue, newValue)) {
					oldValue = newValue;
					
					int r = JOptionPane.showConfirmDialog(contentPane,
							"Vuoi salvare le impostazioni prima di chiudere?",
							"Attenzione",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
					if(r==0) {
						boolean re = saveProperties();
						if(re) {
							JOptionPane.showMessageDialog(contentPane, "Preferenze salvate con successo!", "Successo!", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(LoadingUI.class.getResource("/icon/done.png")));
						} else {
							JOptionPane.showMessageDialog(contentPane, "Impossibile salvare le preferenze!", "Errore!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				
				dispose();
			}
		});
		setAlwaysOnTop(true);
		setResizable(false);
		setTitle("Preferenze");
		setType(Type.UTILITY);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 366, 325);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("<html>\r\n\tIndica qui le tue preferenze.<br/>\r\n<b>Attenzione!</b> Le modifiche ad alcune impostazioni verranno ignorate se una finestra che ne fa uso è ancora aperta.");
		lblNewLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
		lblNewLabel.setBounds(10, 11, 340, 82);
		contentPane.add(lblNewLabel);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 104, 340, 2);
		contentPane.add(separator);
		
		JLabel lblDurataPrestito = new JLabel("Durata prestito:");
		lblDurataPrestito.setFont(new Font("Dialog", Font.BOLD, 14));
		lblDurataPrestito.setBounds(10, 122, 135, 14);
		contentPane.add(lblDurataPrestito);
		
		CB_lenght = new JComboBox<Integer>();
		CB_lenght.setBounds(155, 117, 65, 25);
		contentPane.add(CB_lenght);
		
		JLabel lblgiorni = new JLabel("<html><i>giorni");
		lblgiorni.setBounds(230, 122, 55, 16);
		contentPane.add(lblgiorni);
		
		JLabel lblTitoloResponsabile = new JLabel("Titolo responsabile:");
		lblTitoloResponsabile.setFont(new Font("Dialog", Font.BOLD, 13));
		lblTitoloResponsabile.setBounds(10, 157, 135, 16);
		contentPane.add(lblTitoloResponsabile);
		
		CB_title = new JComboBox<String>();
		CB_title.setBounds(155, 153, 170, 25);
		contentPane.add(CB_title);
		
		JLabel lblNomeResponsabile = new JLabel("Nome responsabile:");
		lblNomeResponsabile.setFont(new Font("Dialog", Font.BOLD, 13));
		lblNomeResponsabile.setBounds(10, 191, 135, 16);
		contentPane.add(lblNomeResponsabile);
		
		TXT_name = new JTextField();
		TXT_name.setBounds(155, 189, 170, 20);
		contentPane.add(TXT_name);
		TXT_name.setColumns(10);
		
		JLabel lblNewLabel_1 = new JLabel("Nome scuola:");
		lblNewLabel_1.setFont(new Font("Dialog", Font.BOLD, 14));
		lblNewLabel_1.setBounds(10, 222, 135, 16);
		contentPane.add(lblNewLabel_1);
		
		TXT_school = new JTextField();
		TXT_school.setToolTipText("Attualmente non usato");
		TXT_school.setBounds(155, 220, 170, 20);
		contentPane.add(TXT_school);
		TXT_school.setColumns(10);
		
		JButton B_save = new JButton("Salva");
		B_save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Map<Settings.PROPERTIES, String> newValue = createMap();
				if(Settings.mapValueEquals(oldValue, newValue)) {
					JOptionPane.showMessageDialog(contentPane, "Nessuna modifica rilevata.", "Attenzione",
							JOptionPane.INFORMATION_MESSAGE);
					return;
				}
				oldValue = newValue;
				
				boolean r = saveProperties();
				if (r) {
					JOptionPane.showMessageDialog(contentPane, "Preferenze salvate con successo!", "Successo!",
							JOptionPane.INFORMATION_MESSAGE,
							new ImageIcon(LoadingUI.class.getResource("/icon/done.png")));
				} else {
					JOptionPane.showMessageDialog(contentPane, "Impossibile salvare le preferenze!", "Errore!",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		B_save.setBounds(10, 259, 98, 26);
		contentPane.add(B_save);
		
		JButton B_cancel = new JButton("Chiudi");
		B_cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		B_cancel.setBounds(252, 259, 98, 26);
		contentPane.add(B_cancel);

		populateComboBox();
		loadValue();
		oldValue = createMap();
		
		setVisible(true);
	}
	
	private void loadValue() {
		/*if(!Settings.propFileExist()) {
			Settings.initPropFile();
		}*/
		
		Map<PROPERTIES, String> pref = DBManager.getPreferences();
		
		try {
			TXT_name.setText(pref.get(Settings.PROPERTIES.NOME_RESPONSABILE));
			TXT_school.setText(pref.get(Settings.PROPERTIES.NOME_SCUOLA));
			CB_lenght.setSelectedItem(Integer.parseInt(pref.get(Settings.PROPERTIES.DURATA_PRESTITO)));
			CB_title.setSelectedItem(pref.get(Settings.PROPERTIES.TITOLO_RESPONSABILE));
		} catch (NumberFormatException | NullPointerException e) {
			Logger.error(e);
			JOptionPane.showMessageDialog(contentPane, "<html>Errore lettura preferenze!:<br/>Errore: <code>"+e.getMessage(), "Errore!", JOptionPane.ERROR_MESSAGE);
		}
		/*
		try {
			TXT_name.setText(Settings.getValue(Settings.PROPERTIES.NOME_RESPONSABILE));
			TXT_school.setText(Settings.getValue(Settings.PROPERTIES.NOME_SCUOLA));
			CB_lenght.setSelectedItem(Integer.parseInt(Settings.getValue(Settings.PROPERTIES.DURATA_PRESTITO)));
			CB_title.setSelectedItem(Settings.getValue(Settings.PROPERTIES.TITOLO_RESPONSABILE));
		} catch (NumberFormatException | PropertiesException e) {
			Logger.error(e);
			JOptionPane.showMessageDialog(contentPane, "<html>Impossibile leggere file preferenze:<br/>Errore: <code>"+e.getMessage(), "Errore!", JOptionPane.ERROR_MESSAGE);
		}*/
		
	}
	
	private void populateComboBox() {
		Integer[] durate = Arrays.stream(Settings.DURATE).boxed().toArray(Integer[]::new);
		CB_lenght.setModel(new DefaultComboBoxModel<Integer>(durate));
		CB_lenght.setSelectedItem(Integer.valueOf(Settings.DEFAULT_DURATA_PRESTITO));
		
		CB_title.setModel(new DefaultComboBoxModel<String>(Settings.TITOLI));
	}
	
	private Map<Settings.PROPERTIES, String> createMap(){
		HashMap<Settings.PROPERTIES, String> map = new HashMap<>();
		
		map.put(Settings.PROPERTIES.DURATA_PRESTITO, ((Integer)CB_lenght.getSelectedItem()).toString());
		map.put(Settings.PROPERTIES.TITOLO_RESPONSABILE, (String) CB_title.getSelectedItem());
		map.put(Settings.PROPERTIES.NOME_RESPONSABILE, TXT_name.getText().trim());
		map.put(Settings.PROPERTIES.NOME_SCUOLA, TXT_school.getText().trim());
		
		return map;
	}
	
	private boolean saveProperties() {
		
		if(!DBManager.updatePreferences(oldValue)) {
			Logger.error("Errore salvataggio preferenze nel database!");
			JOptionPane.showMessageDialog(contentPane, "Impossibile salvare le preferenze.", "Errore!", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		return true;
		/*
		try {
			//return Settings.setValueMap(oldValue);
		} catch (PropertiesException e) {
			Logger.error(e);
			JOptionPane.showMessageDialog(contentPane, "<html>Impossibile salvare file preferenze:<br/>Errore: <code>"+e.getMessage(), "Errore!", JOptionPane.ERROR_MESSAGE);
		}
		return false;*/
	}
}
