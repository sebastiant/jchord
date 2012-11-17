package network;


public class DisconnectEvent implements ControlEvent{

	private Address source;
	
	public DisconnectEvent(Address source) {
		this.source = source;
	}
	public Address getSource(){
		return source;
	}
	
}
