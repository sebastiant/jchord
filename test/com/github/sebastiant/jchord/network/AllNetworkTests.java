package com.github.sebastiant.jchord.network;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestSend.class, 
					TestConnectionRefused.class, 
					TestStop.class,
					TestSimultaneousConnect.class,
					TestMessageSenderMisc.class,
					TestAddress.class})
public class AllNetworkTests {
	 
}
