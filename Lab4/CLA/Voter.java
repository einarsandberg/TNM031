import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
public class Voter
{
	//private int validationNumber;
	//private long idNumber;
	private String name;
	private long persNumber;
	public Voter(String theName, long thePersNumber)
	{
		name = theName;
		persNumber = thePersNumber;
	}

	/*public int getValidationNumber()
	{
		return validationNumber;
	}*/

	public String getName()
	{
		return name;
	}
	public String toString()
	{
		return (name + ":" + String.valueOf(persNumber));
	}
	public long getPersonalNumber()
	{
		return persNumber;
	}
	/*public void setValidationNumber(int num)
	{
		validationNumber = num;
	}*/


}