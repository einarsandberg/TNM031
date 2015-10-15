import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
public class Vote
{
	private long idNumber;
	private long validationNumber;
	private String party;
	private String msgToCTF;
	public Vote(long theIDNumber, long theValidationNumber,
				String theParty)
	{
		idNumber = theIDNumber;
		validationNumber = theValidationNumber;
		party = theParty;
	}

	public String createCTFMessage()
	{
		msgToCTF = (String.valueOf(idNumber) + ":" + String.valueOf(validationNumber) + ":" + party); 
		return msgToCTF;
	}
}