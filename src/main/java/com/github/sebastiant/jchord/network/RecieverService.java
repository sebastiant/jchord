package com.github.sebastiant.jchord.network;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.github.sebastiant.jchord.network.events.Message;

public class RecieverService implements ServiceInterface{

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

	public void register(Observer<Message> observer, String messageId) {
		List<Observer<Message>> list = observers.get(messageId);
		if(list == null) {
			list = new ArrayList<Observer<Message>>();
		}
		list.add(observer);
		observers.put(messageId, list);
	}

	@Override
	public void service() {
		Message msg;
		try {
			msg = con.recieve();
			notifyObservers(msg);
		} catch (SocketException e) {
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
	
	public void stop() {
		this.service.stop();
	}
}
