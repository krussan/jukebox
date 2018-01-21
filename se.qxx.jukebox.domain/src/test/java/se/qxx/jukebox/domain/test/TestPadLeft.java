package se.qxx.jukebox.domain.test;

import static org.junit.Assert.*;
import org.junit.Test;

import junit.framework.TestCase;
import se.qxx.jukebox.domain.MovieOrSeries;

public class TestPadLeft {

	@Test 
	public void TestPadLeftFunction_two() {
		assertEquals("01", MovieOrSeries.padLeft("1", 2));
	}

	@Test 
	public void TestPadLeftFunction_three() {
		assertEquals("001", MovieOrSeries.padLeft("1", 3));
	}
	
	@Test 
	public void TestPadLeftFunction_four() {
		assertEquals("0001", MovieOrSeries.padLeft("1", 4));
	}
	
	@Test 
	public void TestPadLeftFunction_four_two() {
		assertEquals("0012", MovieOrSeries.padLeft("12", 4));
	}
	
	@Test 
	public void TestPadLeftFunction_two_four() {
		assertEquals("1234", MovieOrSeries.padLeft("1234", 2));
	}	
}
