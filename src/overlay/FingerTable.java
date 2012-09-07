package overlay;

import java.math.BigInteger;

public class FingerTable {

	public static boolean inBetween(long start, long end, long number, long overlaySize) {		
		long shiftedNumber = number-start;
		long shiftedEnd = end-start;
		
		if(shiftedNumber > 0 && shiftedNumber <= shiftedEnd) {
			// Number is in between start and end.
			return true;
		} else {
			return false;
		}
	}
}
