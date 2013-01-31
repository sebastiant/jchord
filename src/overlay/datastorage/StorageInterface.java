package overlay.datastorage;

/* 
 * Interface for objects stored in the datastore.
 * Ensures that the object can be (un)serialized, as is required from the transmission-protocol.
 */
public interface StorageInterface {
	public Object toObject();
	public String toString();
}
