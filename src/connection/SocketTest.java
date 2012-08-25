package connection;

public class SocketTest implements ConnectionCallback {

	private ConnectionListener cl;
	public SocketTest(){
		cl = new ConnectionListener(8080, this);
	}
	@Override
	public void receive(Message msg) {
		System.out.println("received: " + msg.getMsg() + "from: " + msg.getAddr().toString() + ":"+msg.getPort());
		
	}
	
	public static void main(String args[]){
		SocketTest s = new SocketTest();
	}

}
