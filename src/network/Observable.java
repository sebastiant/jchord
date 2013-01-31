package network;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
	/** An observable object, that notifies all registered observers
	 * when notifyObservers is called*/
	
	protected List<Observer<T>> observers = new ArrayList<Observer<T>>();
	
	public void register(Observer<T> observer) {
		observers.add(observer);
	}
	
	public void unregister(Observer<T> observer) {
		observers.remove(observer);
	}
	
	public void notifyObservers(T e) {
		for(Observer<T> o:observers) {
			o.notifyObserver(e);
		}
	}
}
