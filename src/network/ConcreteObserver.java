package network;

public abstract class ConcreteObserver<T> implements Observer<T>{

	@Override
	public abstract void notifyObserver(T e);

}
