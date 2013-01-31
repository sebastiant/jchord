package junit.overlay;

import overlay.datastorage.StorageInterface;

/*
 * Testobject used during tests.
 */
public class TestString implements StorageInterface {
	private String string;
	
	public TestString(String string)
	{
		this.string = string;
	}
	@Override
	public String toObject() {
		return string;
	}

	@Override
	public String toString() {
		return string;
	}

}
