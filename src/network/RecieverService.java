package network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;

import network.events.Message;

public class RecieverService implements ServiceInterface{
	
	private Connection con;
	private HashMap<String, List<Observer<Message>>> observers = new HashMap<String, List<Observer<Message>>>();
	private Service service;
	
	public RecieverService(Connection c) {
		this.con = c;
		this.service = new Service(this);
		this.service.start();
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
		Message msg = con.recieve();
		notifyObservers(msg);
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
