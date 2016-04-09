package com.github.sebastiant.jchord.network;

public interface Observer<T> {
	
	void notifyObserver(T e);
}
