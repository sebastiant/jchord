package overlay;

import java.math.BigInteger;

public class FingerTable {

	/*
	 * inBetween/3
	 * returns true if the third argument is in between the first and the second in a logical ring.
	 */
	public static boolean inBetween(long start, long end, long number) {		
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
