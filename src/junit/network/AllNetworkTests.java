package junit.network;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestSend.class, 
					TestConnectionRefused.class, 
					TestStop.class,
					TestSimultaneousConnect.class,
					TestMessageSenderMisc.class,
					TestAddress.class})
public class AllNetworkTests {
	 
}