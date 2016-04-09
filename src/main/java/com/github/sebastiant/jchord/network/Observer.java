package com.github.sebastiant.jchord.network;

public interface Observer<T> {

	/** Interface to be implemented by observers.*/
	
	public void notifyObserver(T e);
}
