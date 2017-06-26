package it.andreacioni.sdrive.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

public class RegexTest {

	@Test
	public void test() {
		String path = "usr/local/bin", regex = "/", expected[] = new String[] {"usr",  "local", "bin"};
		
		String parts[] = path.split(regex);
		
		System.out.println("Path: " + Arrays.toString(parts));
		System.out.println("Expe: " + Arrays.toString(expected));
		
		assertArrayEquals(expected,parts);
	}

}
