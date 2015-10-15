import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
public class Voter
{
	
	private long idNumber;
	private String name;
	private long personNumber; // ex 9207261111

	public Voter(String theName, long thePersonNumber)
	{
		name = theName;
		personNumber = thePersonNumber;
	}

	public String getName()
	{
		return name;
	}

	public long getPersonalNumber()
	{
		return personNumber;
	}

	public String toString()
	{
		return (name+ ":" + String.valueOf(personNumber));
	}
		public void createIDNumber()
	{
		Random rand = new Random();
		long min = 1;
		long max = 1000000000L;
		// random identification number from 1 to 1000000000
		idNumber = min + ((long)(rand.nextDouble()*(max-min))); 
	}
	public long getIDNumber()
	{
		return idNumber;
	}


}