package network;

import network.events.DisconnectEvent;
import network.events.Message;

public class KeepAliveService extends Observable<DisconnectEvent> implements ServiceInterface {
	
	private RecieverService mr;
	private Connection con;
	private long timeout;
	private long  lastHB;
	private Service hbService;
	
	public KeepAliveService(RecieverService mr, long timeout) {
		this.timeout = timeout;
		this.mr = mr;
		this.con = mr.getConnection();
		this.lastHB = System.currentTimeMillis();
		Observer<Message> hbObs = new Observer<Message>() {
			@Override
			public void notifyObserver(Message e) {
				lastHB = System.currentTimeMillis();
				Message hb = new Message();
				con.send(hb);
			}
		};
		mr.register(hbObs, "hb");
		hbService = new Service(this);
		hbService.start();
	}
	

	public void service() {
		if((System.currentTimeMillis() - lastHB) > KeepAliveService.this.timeout)  {
			this.notifyObservers(new DisconnectEvent(con.getAddress(), con));
		}
		try {
			Thread.sleep(KeepAliveService.this.timeout/3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message hb = new Message();
		hb.setId("hb");
		con.send(hb);
	}
	
	public boolean isRunning() {
		return this.hbService.isRunning();
	}
	
	public void stop() {
		this.hbService.stop();
	}
}
