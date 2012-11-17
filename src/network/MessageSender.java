package network;

public class MessageSender extends Observable<Message> {
	
	private Server server;
	private ConnectionHandler handler;
	
	public MessageSender(int port) {
		server = new Server(port);
		handler = new ConnectionHandler(server);
		handler.register(new ConcreteObserver<Message>(){
			@Override
			public void notifyObserver(Message m) {
				MessageSender.this.notifyObservers(m);
			}
		});
	}
	
	public void send(Message m) {
		handler.send(m);
	}
	
	public void start() {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
}
