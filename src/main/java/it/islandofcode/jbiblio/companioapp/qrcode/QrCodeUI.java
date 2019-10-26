package it.islandofcode.jbiblio.companioapp.qrcode;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.companioapp.HttpHandler;
import it.islandofcode.jbiblio.companioapp.IRemoteUpdate;
import it.islandofcode.jbiblio.companioapp.HttpHandler.REGISTER_MODE;

public class QrCodeUI extends JFrame implements IRemoteUpdate{

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	
	private JLabel L_qrcode;
	
	private HttpHandler HTTPH;

	/**
	 * Create the frame.
	 */
	public QrCodeUI(HttpHandler ht) {
		setAlwaysOnTop(true);
		this.HTTPH = ht;
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(HTTPH!=null && (!HTTPH.isSomeoneConnected()) ) {
					HTTPH.stop();
				}
				dispose();
			}
		});
		setResizable(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 285, 397);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		L_qrcode = new JLabel("");
		L_qrcode.setHorizontalAlignment(SwingConstants.CENTER);
		L_qrcode.setBounds(10, 11, 256, 256);
		contentPane.add(L_qrcode);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(10, 278, 259, 2);
		contentPane.add(separator);
		
		JLabel lblDescription = new JLabel("<html><center>Questa finestra si chiuderà automaticamente alla connessione dell'app<br/>\r\nChiudi la finestra se vuoi annullare l'operazione.</center></html>");
		lblDescription.setVerticalAlignment(SwingConstants.TOP);
		lblDescription.setBounds(10, 291, 256, 71);
		contentPane.add(lblDescription);
		
		try {
			if(HTTPH!=null) {
				HTTPH.registerUI(REGISTER_MODE.CONNECTION, QrCodeUI.this);
				QrCode qr0 = QrCode.encodeText("http://"+HTTPH.getIP()+":"+HttpHandler.PORT, QrCode.Ecc.LOW);
				L_qrcode.setIcon(new ImageIcon(qr0.toImage(10, 4)));
			} else 
				throw new IOException("Server web non attivo.");

		} catch (IOException e1) {
			Logger.error(e1);
			JOptionPane.showMessageDialog(contentPane, "<html>Non è stato possibile creare il codice QR.<br/>"
					+ "<center><code>"+e1.getMessage()+"</code></center></html>",
					"Errore!",
					JOptionPane.ERROR_MESSAGE);
			dispose();
		}
		this.setLocationRelativeTo(null);
	}

	@Override
	public void appStatusNotification(STATUS status) {
		Logger.info("Nuovo client connesso! Unregister & Dispose!");
		if(HTTPH!=null) {
			this.HTTPH.unregisterUI(QrCodeUI.this);
		}
		dispose();
	}

	@Override
	public void receiveAppMessage(String msg) {	
	}

	@Override
	public String receiveAndRespondAppMessage(String msg) {
		return null;
	}

	@Override
	public String getRegisterId() {
		return "QRCODEUI_frame";
	}
}
