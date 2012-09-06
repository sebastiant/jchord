package connection;

public interface ConnectionCallback {
	public void receive(Message message);
	public void register(Host host);
	public void disconnected(Connection con);
}
