package com.github.sebastiant.jchord.network;

public class Service {

	private boolean running = false;
	private Thread thread;
	private ServiceInterface interf;

	public Service(ServiceInterface si) {
		this.interf = si;
	}

	public void start() {
		thread = new Thread() {
			public void run() {
				while(running) {
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
