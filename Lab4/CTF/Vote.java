import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;

public class Vote
{
	long idNumber;
	long validationNumber;
	String party;
	public Vote(long theIDNumber, long theValidationNumber,
				String theParty)
	{
		idNumber = theIDNumber;
		validationNumber = theValidationNumber;
		party = theParty;
	}

}