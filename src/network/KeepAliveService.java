package network;

import java.io.IOException;

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
				//System.out.println("Recived hb");
			}
		};
		mr.register(hbObs, "hb");
		hbService = new Service(this);
		hbService.start();
	}
	

	public void service() {
		if((System.currentTimeMillis() - lastHB) > KeepAliveService.this.timeout)  {
			System.err.println("Connection down");
			this.notifyObservers(new DisconnectEvent(con.getAddress(), con));
		}
		try {
			Thread.sleep(KeepAliveService.this.timeout/3);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isRunning()) {
			Message hb = new Message();
			hb.setId("hb");
			if(!con.send(hb)) {
				this.notifyObservers(new DisconnectEvent(con.getAddress(), con));
			}
		}
	}
	
	public boolean isRunning() {
		return this.hbService.isRunning();
	}
	
	public void stop() {
		this.hbService.stop();
	}
}
