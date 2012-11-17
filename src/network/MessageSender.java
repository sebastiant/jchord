package network;

import network.events.ControlEvent;

public class MessageSender {
	
	private Server server;
	private ConnectionHandler handler;
	
	public MessageSender(int port) {
		server = new Server(port);
		handler = new ConnectionHandler(server);
	}
	
	public void registerMessageObserver(Observer<Message> mobs) 
	{
		handler.registerMessageObserver(mobs);
	}
	
	public void registerControlObserver(Observer<ControlEvent> evt) 
	{
		handler.registerControlObserver(evt);
	}
	
	public void send(Message m) {
		m.setId("app");
		handler.send(m);
	}
	
	public void start() {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
}
