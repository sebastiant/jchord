package network;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import network.events.Message;

public class RecieverService implements ServiceInterface{
	
	/** Continuously receives messages from the given connections.
	 *  Calls back to message sender in case the socket is closed while receiving,
	 *  removing the connection and stopping itself.
	 *  
	 *  When a message is received, it is delivered only to the observers registered
	 *  on the particular message type of the message.
	 *  */
	
	private Connection con;
	private HashMap<String, List<Observer<Message>>> observers = new HashMap<String, List<Observer<Message>>>();
	private Service service;
	private MessageSender sender;
	
	public RecieverService(Connection c, MessageSender sender) {
		this.con = c;
		this.sender = sender;
		this.service = new Service(this);
		this.service.start();
	}
	
	/* messageId == all -> all messages */
	
	/** Register an observer to receive messages with the given messageId,
	 * from this RevieverService. The string "all" means all messages.
	 * Other common ids is "app" for applicaion layer, and "con" for connection layer messages.*/
	public void register(Observer<Message> observer, String messageId) {
		List<Observer<Message>> list = observers.get(messageId);
		if(list == null) {
			list = new ArrayList<Observer<Message>>();
		}
		list.add(observer);
		observers.put(messageId, list);
	}
	
	/** Unregisters the given observer from receiving messages from this RecieverService */
	public void unregister(Observer<Message> observer) {
		for(Entry<String, List<Observer<Message>>> e : observers.entrySet()) {
			if(e.getValue().remove(observer)) {
				break;
			}
		}
	}

	@Override
	public void service() {
		Message msg;
		try {
			msg = con.recieve();
			notifyObservers(msg);
		} catch (SocketException e) { // Socket closed
			//System.out.println("Reciever: Socket closed, stopping");
			if(service.isRunning()) {
				sender.removeConnection(con);
			}
		}
	}

	private void notifyObservers(Message msg) {
		if(msg == null) return;
		for(Entry<String, List<Observer<Message>>> e: observers.entrySet()) {
			if(e.getKey().equals("all")) {
				for(Observer<Message> o: e.getValue()) {
					o.notifyObserver(msg);
				}
			} else if(e.getKey().equals(msg.getId())) {
				for(Observer<Message> o: e.getValue()) {
					o.notifyObserver(msg);
				}
			}
		}
	}
	
	/** Get the underlying connection */
	public Connection getConnection() {
		return con;
	}
	
	/** Stop the service */
	public void stop() {
		this.service.stop();
	}
	
	/** Check if the service is running */
	public boolean isRunning() {
		return service.isRunning();
	}
}
