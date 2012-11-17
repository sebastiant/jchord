package network;

public class Service {
	boolean running;
	Thread thread;
	ServiceInterface interf;
	
	
	public Service() {}
	
	public Service(ServiceInterface si) {
		this.interf = si;
	}

	public void start() {
		thread = new Thread() {
			public void run() {
				while(running ) {
					service();
				}
			}
		};
		running = true;
		thread.start();
	}
	
	public void service() {
		interf.service();
	}
	
	public void stop() {
		running = false;
		thread = null;
	}
	
	public boolean isRunning() {
		return running;
	}
}