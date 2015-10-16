import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
import java.math.*;
public class Voter
{
	
	private String hashedIDNumber;
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
		return (name + ":" + String.valueOf(personNumber));
	}

	public void createIDNumber()
	{
		Random rand = new Random();
		long min = 1;
		long max = 1000000000L;
		// random identification number from 1 to 1000000000
		hashedIDNumber = hash(min + ((long)(rand.nextDouble()*(max-min)))); 
	}

	private String hash(long idNumber)
	{	
		try
		{

			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(String.valueOf(idNumber).getBytes("UTF-8"));

			return new BigInteger(1, crypt.digest()).toString(16);
		}
		catch (Exception e)
		{
			System.out.println("Error when hashing");
			e.printStackTrace();
		}
		return "";

	}	

	public String getIDNumber()
	{
		return hashedIDNumber;
	}
	public boolean equals(Voter v)
	{
		if (name.equals(v.name) && personNumber == v.personNumber)
			return true;

		return false;
	}


}