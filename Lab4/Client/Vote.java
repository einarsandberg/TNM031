import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
import java.math.*;
public class Vote
{
	private String hashedIDNumber;
	private String hashedValidationNumber;
	private String party;
	private String msgToCTF;
	public Vote(String theHashedIDNumber, String theHashedValidationNumber,
				String theParty)
	{
		hashedIDNumber = theHashedIDNumber;
		hashedValidationNumber = theHashedValidationNumber;
		party = theParty;
	}

	public String createCTFMessage()
	{
		//msg will look like id:validationNum:party
		msgToCTF = (hashedIDNumber + ":" + hashedValidationNumber + ":" + party); 
		return msgToCTF;
	}
}