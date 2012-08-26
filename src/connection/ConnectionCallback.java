package connection;

public interface ConnectionCallback {
	public void register(Host host);
	public void receive(Message message);
}
