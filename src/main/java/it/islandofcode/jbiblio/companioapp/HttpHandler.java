package it.islandofcode.jbiblio.companioapp;

import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.internalServerError;
import static spark.Spark.ipAddress;
import static spark.Spark.notFound;
import static spark.Spark.port;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.tinylog.Logger;

import it.islandofcode.jbiblio.companioapp.IRemoteUpdate.STATUS;
import spark.Spark;

public class HttpHandler {
	
	public static final String URI_CONNECT = "connect";
	
	public static final String PARAM_DISCONNECT = "disconnect";
	
	public static final String PARAM_ISBN = "isbn";
	
	public static final String URI_PING = "ping";
	
	public static final String PARAM_KEY = "key";
	
	private static final String RESPONSE_ERROR = "ERROR";
	private static final String RESPONSE_OK = "RECEIVED";
	private static final String RESPONSE_UNAUTORIZED = "UNKNOW";
	private static final String RESPONSE_POSITIVE_DISCONNECTION = "BYE";
	private static final String RESPONSE_PONG = "PONG";
	
	private static final String[] DNS_SVR_ADDR = {"8.8.8.8","8.8.4.4","1.1.1.1"};
	

	public static enum REGISTER_MODE {
		CONNECTION, INPUT_DATA
	}
	
	public static final int PORT = 6339;
	private String IP;
	
	//private List<String> clients = new ArrayList<>();
	private String CLIENT = "";

	private ConcurrentHashMap<IRemoteUpdate, REGISTER_MODE> registered = new ConcurrentHashMap<>();

	public HttpHandler() throws IOException {
		
		this.IP = "";
		int i = 0;
		while (IP.isEmpty()) {
			Logger.debug("TEST REMOTE DNS :" + DNS_SVR_ADDR[i]);
			try (final DatagramSocket dtgrm = new DatagramSocket()) {
				dtgrm.connect(InetAddress.getByName(DNS_SVR_ADDR[i]), 10002);
				this.IP = dtgrm.getLocalAddress().getHostAddress().replace("/", "");
			}
			i++;
		}
		
		//Se tutti i server sono offline.
		if(IP==null || IP.isEmpty()) {
			throw new IOException("Missing external IP");
		}
		
		Logger.info("HTTPHANDLER istanziato (ma non avviato) su URL: http://" + IP + ":" +PORT);
	}

	public void registerUI(REGISTER_MODE mode, IRemoteUpdate UI) {
		registered.put(UI, mode);
		Logger.debug("ServerRunner: Registering a " + mode.toString() + " for "+UI.getRegisterId());
	}

	public void unregisterUI(IRemoteUpdate UI) {
		REGISTER_MODE mode = registered.remove(UI);
		Logger.debug("ServerRunner: Unregister " + mode.toString() + " for "+UI.getRegisterId());
	}
	
	public void unregisterAll() {
		registered.clear();
		Logger.debug("ServerRunner unregister ALL");
	}

	private String notifyRegisterd(STATUS event, String data) {

		Logger.debug("Frame registrati " + registered.size());

		try {
			for (Entry<IRemoteUpdate, REGISTER_MODE> E : registered.entrySet()) {
				switch (E.getValue()) {
				case CONNECTION:
					if(event == STATUS.CONNECTED || event ==STATUS.DISCONNECTED || event == STATUS.TIMEOUT) {
						E.getKey().appStatusNotification(event);
					}
					break;
				case INPUT_DATA:
					if(event == STATUS.DATA_RECEIVED) {
						E.getKey().receiveAppMessage(data);
					}
					break;
				default:
					Logger.error("Non dovresti vedere questo errore.");
				}
			}
		} catch (java.util.ConcurrentModificationException CE) {
			Logger.error(CE);
		}
		// TODO finchè non gestisco la risposta, questo sarà null fisso
		return null;
	}
	
	public boolean isSomeoneConnected() {
		//return clients.size()>0;
		return (CLIENT!=null) && !CLIENT.isEmpty();
	}	
	
	public String getIP() {
		return this.IP;
	}

	public void start() {
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		initExceptionHandler((e) -> Logger.error(e));

		ipAddress(IP);
		port(6339);
		
		notFound(RESPONSE_ERROR);			 //404
		internalServerError(RESPONSE_ERROR); //500
		
		get("/connect", (request, response) -> {
			Logger.debug("HTTP [GET] /CONNECT from " + request.ip());
			
			String UUID = java.util.UUID.randomUUID().toString().replace("-", "");
			//clients.add(UUID);
			CLIENT = UUID;
			notifyRegisterd(STATUS.CONNECTED, request.ip()+"#"+UUID);
			return UUID;
		});
		
		get("/disconnect/:key", (request, response) -> {
			Logger.debug("HTTP [GET] /DISCONNECT->KEY: " + request.params(PARAM_KEY));

			//if (checkIfClientConnected(request.params(PARAM_KEY))) {
			if(CLIENT.equals(request.params(PARAM_KEY))) {
				//clients.remove(request.params(PARAM_KEY));
				CLIENT = "";
				notifyRegisterd(STATUS.DISCONNECTED, request.ip()+"#"+request.params(PARAM_KEY));
				
				Logger.debug("RESPOND TO [" + request.ip() + "] WITH {" + RESPONSE_POSITIVE_DISCONNECTION + "}");
				return RESPONSE_POSITIVE_DISCONNECTION;
			} else {
				Logger.debug("RESPOND TO [" + request.ip() + "] WITH {" + RESPONSE_UNAUTORIZED + "}");
				return RESPONSE_UNAUTORIZED;
			}
		});
		
		get("/ping", (request, response) -> {
			Logger.debug("HTTP [GET] /PING from " + request.ip());
			return RESPONSE_PONG;
		});
		
		get("/isbn/:isbn/:key", (request, response) -> {
			Logger.debug("HTTP [GET] /ISBN WITH ISBN=" + request.params(PARAM_ISBN) +" & KEY="+request.params(PARAM_KEY));
			
			//if (checkIfClientConnected(request.params(PARAM_KEY))) {
			if(CLIENT.equals(request.params(PARAM_KEY))) {
            	Logger.debug("ISBN ["+request.params(PARAM_ISBN)+"] ACCETTATO, KEY CONFERMATA");
            	notifyRegisterd(STATUS.DATA_RECEIVED, request.params(PARAM_ISBN));
				return RESPONSE_OK;
			} else {
				Logger.info("RESPOND TO [" + request.ip() + "] WITH {" + RESPONSE_UNAUTORIZED + "}");
				return RESPONSE_UNAUTORIZED;
			}
		});

		Logger.info("HttpHandler avviato.");
	}
	
	public void stop() {
		Logger.debug("SERVER STOP REQUESTED");
		Spark.stop();
	}
	/*
	private boolean checkIfClientConnected(String uuid) {
		if(uuid==null || uuid.trim().isEmpty())
			return false;
		for(String S : clients) {
			if(S.equals(uuid.trim())) {
				Logger.debug("TROVATO : " +uuid);
				return true;
			}
		}
		Logger.debug("NON trovato : " +uuid);
		return false;
	}
	*/

}
