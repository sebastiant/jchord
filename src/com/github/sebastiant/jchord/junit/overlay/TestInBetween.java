package com.github.sebastiant.jchord.junit.overlay;

import static org.junit.Assert.*;

import org.junit.Test;

import com.github.sebastiant.jchord.overlay.Node;

public class TestInBetween {

	@Test
	public void testInBetween1(){
		long l_1 = 40;
		long l_2 = 4;
		long l_3 = 50;
		assertTrue(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween2(){
		long l_1 = 3;
		long l_2 = 8000;
		long l_3 = 9000;
		assertFalse(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween3(){
		long l_1 = 950;
		long l_2 = 951;
		long l_3 = 1;
		assertFalse(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween4(){
		long l_1 = 77;
		long l_2 = 4;
		long l_3 = 90;
		assertTrue(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween5(){
		long l_1 = 1000;
		long l_2 = 5;
		long l_3 = 20;
		assertFalse(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween6(){
		long l_1 = 1000;
		long l_2 = 20;
		long l_3 = 5;
		assertTrue(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween7(){
		long l_1 = 10;
		long l_2 = 5;
		long l_3 = 10;
		assertTrue(Node.isBetween(l_1, l_2, l_3)); 
	}
	
	@Test
	public void testInBetween8(){
		long l_1 = 11;
		long l_2 = 1000;
		long l_3 = 11;
		assertTrue(Node.isBetween(l_1, l_2, l_3)); 
	}
}
