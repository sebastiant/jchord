package junit.overlay;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestRing.class,
						TestInBetween.class,
						TestStorage.class
					})
public class AllOverlayTests {
	 
}
