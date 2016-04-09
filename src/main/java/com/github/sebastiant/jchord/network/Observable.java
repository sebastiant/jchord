package com.github.sebastiant.jchord.network;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
	
	protected List<Observer<T>> observers = new ArrayList<Observer<T>>();
	
	public void register(Observer<T> observer) {
		observers.add(observer);
	}

	public void notifyObservers(T e) {
		for(Observer<T> o:observers) {
			o.notifyObserver(e);
		}
	}
}
