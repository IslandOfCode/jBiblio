package it.islandofcode.jbiblio;

import java.awt.Color;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
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

import it.islandofcode.jbiblio.artefact.Client;
import it.islandofcode.jbiblio.db.DBManager;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class EditClient extends JFrame {

	private static final String CLIENTIDLABEL = "<html><i>User ID: </i>###";

	public static enum CLIENTMODE {
		ADD, // aggiunge nuovo cliente
		EDIT // modifica cliente esistente
	};

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField TXT_nome;
	private JTextField TXT_cognome;
	private JButton B_saveClient;
	private JLabel L_userID;
	private JComboBox<String> CB_classe;
	private JComboBox<String> CB_sezione;

	private CLIENTMODE MODE;
	private Client client;

	/**
	 * Create the frame.
	 */
	public EditClient(CLIENTMODE mode, int ID, GUI parent) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(parent!=null)
					parent.signalFrameClosed(getTitle());
				dispose();
			}
		});
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 287, 207);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JLabel lblNome = new JLabel("Nome");
		lblNome.setBounds(12, 12, 90, 16);
		contentPane.add(lblNome);

		TXT_nome = new JTextField();
		TXT_nome.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_nome.setBounds(120, 10, 149, 20);
		contentPane.add(TXT_nome);
		TXT_nome.setColumns(13);

		JLabel lblCognome = new JLabel("Cognome");
		lblCognome.setBounds(12, 40, 90, 16);
		contentPane.add(lblCognome);

		TXT_cognome = new JTextField();
		TXT_cognome.setFont(new Font("Dialog", Font.BOLD, 14));
		TXT_cognome.setBounds(120, 38, 149, 20);
		contentPane.add(TXT_cognome);
		TXT_cognome.setColumns(13);

		JLabel lblClasse = new JLabel("Classe");
		lblClasse.setBounds(12, 68, 90, 16);
		contentPane.add(lblClasse);

		CB_classe = new JComboBox<String>();
		CB_classe.setFont(new Font("Dialog", Font.BOLD, 14));
		CB_classe.setModel(new DefaultComboBoxModel<String>(new String[] { "1", "2", "3" }));
		CB_classe.setSelectedIndex(0);
		CB_classe.setBounds(120, 64, 40, 25);
		contentPane.add(CB_classe);

		JLabel lblSezione = new JLabel("Sezione");
		lblSezione.setBounds(12, 101, 90, 16);
		contentPane.add(lblSezione);

		CB_sezione = new JComboBox<String>();
		CB_sezione.setFont(new Font("Dialog", Font.BOLD, 14));
		CB_sezione.setModel(new DefaultComboBoxModel<String>(new String[] { "A", "B", "C", "D", "E", "F", "G" }));
		CB_sezione.setBounds(120, 97, 40, 25);
		contentPane.add(CB_sezione);

		JSeparator separator = new JSeparator();
		separator.setBounds(12, 129, 257, 2);
		contentPane.add(separator);

		B_saveClient = new JButton("");
		B_saveClient.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (MODE == CLIENTMODE.ADD) {
					client = retriveDataFromForm();
					DBManager.addNewClient(client);
					Logger.info("Nuovo cliente aggiunto, {" + client.toString() + "}");
					if(parent!=null)
						parent.signalFrameClosed(getTitle());
					dispose();
				} else { // EDIT
					Client clientUpdate = retriveDataFromForm();
					if (!client.equals(clientUpdate)) {
						DBManager.updateClient(client.getID(), clientUpdate);
						JOptionPane.showMessageDialog(contentPane, "Cliente aggiornato", "Aggiornamento",
								JOptionPane.INFORMATION_MESSAGE);
						Logger.info("Cliente [" + client.getID() + "], aggiornato");
						if(parent!=null)
							parent.signalFrameClosed(getTitle());
						dispose();
					} // else do nothing
					Logger.warn("nessun aggiornamento effetturato [" + client.hashCode() + "]=["
							+ clientUpdate.hashCode() + "]");
				}
			}
		});
		B_saveClient.setBounds(130, 143, 139, 26);
		contentPane.add(B_saveClient);

		L_userID = new JLabel("<html><i>User ID: </i>###");
		L_userID.setForeground(Color.GRAY);
		L_userID.setBounds(12, 143, 100, 26);
		contentPane.add(L_userID);

		this.MODE = mode;

		switch (MODE) {
		case ADD:
			this.setTitle("Aggiungi nuovo Cliente");
			B_saveClient.setText("Aggiungi cliente");
			L_userID.setVisible(false);
			break;
		case EDIT:
			client = DBManager.getClientByID(ID);

			populateForm();

			this.setTitle("Modifica Cliente");
			B_saveClient.setText("Modifica Cliente");
			break;
		}

		this.setVisible(true);
		parent.signalFrameOpened(getTitle());
	}

	private Client retriveDataFromForm() {

		int ID = (client != null && client.getID() > -1) ? client.getID() : -1;

		int removed = (client != null && client.getID() > -1) ? client.getRemoved() : 0;

		return new Client(ID, this.TXT_nome.getText(), this.TXT_cognome.getText(),
				Integer.parseInt((String) CB_classe.getSelectedItem()), (String) CB_sezione.getSelectedItem(), removed);
	}

	private void populateForm() {
		this.TXT_nome.setText(client.getNome());
		this.TXT_cognome.setText(client.getCognome());
		CB_classe.setSelectedIndex(client.getClasse() - 1);
		// Attenzione, con gli interi sembra non funzionare
		// CB_classe.setSelectedItem(client.getClasse()-1);
		CB_sezione.setSelectedItem(client.getSezione());

		L_userID.setText(CLIENTIDLABEL.replace("###", "" + client.getID()));

	}
}
