package network;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import network.events.Message;

public class RecieverService implements ServiceInterface{
	
	private Connection con;
	private HashMap<String, List<Observer<Message>>> observers = new HashMap<String, List<Observer<Message>>>();
	private Service service;
	private MessageSender sender;
	
	public RecieverService(Connection c, MessageSender sender) {
		this.con = c;
		this.service = new Service(this);
		this.service.start();
		this.sender = sender;
	}
	
	/* messageId == all -> all messages */
	public void register(Observer<Message> observer, String messageId) {
		List<Observer<Message>> list = observers.get(messageId);
		if(list == null) {
			list = new ArrayList<Observer<Message>>();
		}
		list.add(observer);
		observers.put(messageId, list);
	}
	
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
			sender.removeConnection(con);
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
	
	public Connection getConnection() {
		return con;
	}
	
	public void stop() {
		this.service.stop();
	}
	
	public boolean isRunning() {
		return service.isRunning();
	}
}
