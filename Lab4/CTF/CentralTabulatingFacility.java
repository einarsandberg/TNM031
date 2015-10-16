import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
public class CentralTabulatingFacility
{
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8191;
	static final String KEYSTORE = "CTFKeyStore.ks";
	static final String TRUSTSTORE = "CTFTrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private BufferedReader socketInFromCLA;
	private PrintWriter socketOutToCLA;
	private SSLSocket streamCLA;
	private SSLSocket streamClient;
	private boolean running = true;

	private SSLServerSocket serverSocketCLA;
	private SSLServerSocket serverSocketClient;
	private SSLSocket socketCLA;
	private SSLSocket socketClient;
	private BufferedReader socketInFromClient;
	private PrintWriter socketOutToClient;

	private List <Long> validationNumList;
	private Map <String, String> hashedValidationNumbers;
	
	
	// party as key and list of hashed id numbers as value
	private Map <String, List<String> > votes;

	private Map <String, Long> partyCount;
	/*public CentralTabulatingFacility(int thePort)
	{
		port = thePort;
	}*/
	public void run()
	{
		try
		{
			// validation number as key with checked/unchecked as value
			hashedValidationNumbers = new HashMap<String, String>();
			
			votes = new HashMap <String, List<String> >();
			partyCount = new HashMap <String, Long>();

			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load( new FileInputStream(KEYSTORE), STOREPASSWD.toCharArray());

			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load( new FileInputStream(TRUSTSTORE), STOREPASSWD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ALIASPASSWD.toCharArray());

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();

			SSLServerSocket socketClient = (SSLServerSocket) sslServerFactory.createServerSocket(8191);
			SSLServerSocket socketCLA = (SSLServerSocket) sslServerFactory.createServerSocket(8192);


			socketClient.setEnabledCipherSuites(socketClient.getSupportedCipherSuites());
			socketCLA.setEnabledCipherSuites(socketCLA.getSupportedCipherSuites());
			//client authentication is required
			socketClient.setNeedClientAuth(true);
			socketCLA.setNeedClientAuth(true);
			System.out.println("Central Tabulating Facility is now active");
			
			streamCLA = (SSLSocket)socketCLA.accept();
			streamClient = (SSLSocket)socketClient.accept();
			socketInFromCLA = new BufferedReader(new InputStreamReader(streamCLA.getInputStream()));
			socketOutToCLA = new PrintWriter(streamCLA.getOutputStream(), true);

			socketInFromClient = new BufferedReader(new InputStreamReader(streamClient.getInputStream()));
			socketOutToClient = new PrintWriter(streamClient.getOutputStream(), true);

			while (running)
			{

				String stringFromCLA = socketInFromCLA.readLine();
				String stringFromClient = socketInFromClient.readLine();
				if (stringFromCLA!= null)
				{
					switch (stringFromCLA)
					{
						case "hashedValidationNumToCTF":
							String hashedValidationNumber= socketInFromCLA.readLine();
							if (hashedValidationNumber.equals("FRAUD"))
							{
								System.out.println("Election fraud detected!");
							}
							else
							{
								hashedValidationNumbers.put(hashedValidationNumber, "unchecked");
							}
							
							break;

						default:
							running = false;
							break;
					}
				}

				if (stringFromClient != null)
				{
					switch(stringFromClient)
					{
						case "msgFromClient":
							//msg format is id:validationNum:party
							String msgFromClient = socketInFromClient.readLine();
							System.out.println(msgFromClient);
							String hashedIDNumber = getIDNumberFromMsg(msgFromClient);
							String party = getPartyFromMsg(msgFromClient);
							String hashedValidationNumber = getValidationNumberFromMsg(msgFromClient);
							hashedValidationNumbers.put(hashedValidationNumber, "unchecked");
							System.out.println(hashedIDNumber);
							System.out.println(hashedValidationNumber);
							System.out.println(party);
							checkValidationNumberInMap(hashedValidationNumber);
							if (partyCount.get(party) == null)
							{
								String prevVoteCount = "0";
								socketOutToClient.println("Previous vote count: " + prevVoteCount);
							}
							else
							{
								socketOutToClient.println("Previous vote count: " + 
									String.valueOf(partyCount.get(party)));
							}
							
							socketOutToClient.println("Voting...");
							addVote(party);
							associateIDWithParty(hashedIDNumber, party);
							socketOutToClient.println("Updated vote count: " + 
									String.valueOf(partyCount.get(party)));
							break;

						default:
							running = false;
							break;
					}

				}

			}
			streamCLA.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	private String getIDNumberFromMsg(String msg)
	{
		try
		{
			int index = msg.lastIndexOf(':');
			// get characters before last colon, will be id:validationNum
			String subStr = msg.substring(0, index-1);
			int index2 = subStr.lastIndexOf(':');
			String hashedIDNumber = subStr.substring(0,index2);
			return hashedIDNumber;
		}
		catch (Exception e)
		{
			System.out.println("Error getting id number");
			e.printStackTrace();
		}
		return "";

	}
	private String getPartyFromMsg(String msg)
	{
		int index = msg.lastIndexOf(':');
		String party = msg.substring(index+1);
		return party;
	}
	private String getValidationNumberFromMsg(String msg)
	{
		try
		{
			int index = msg.lastIndexOf(':');
			// get characters before last colon, will be id:validationNum
			String subStr = msg.substring(0, index);
			int index2 = subStr.lastIndexOf(':');
			String hashedValidationNumber = subStr.substring(index2+1);
			return hashedValidationNumber;
		}
		catch (Exception e)
		{
			System.out.println("Error getting validation number");
			e.printStackTrace();
		}
		return "";
	}
	private  void checkValidationNumberInMap(String hashedValidationNumber)
	{
			if (hashedValidationNumbers.get(hashedValidationNumber) == "checked")
			{
				System.out.println("Election fraud detected! Validation number already checked.");
			}
			else
			{
				if (hashedValidationNumbers.containsKey(hashedValidationNumber))
				{
					System.out.println("Validation number found and unchecked! Checking...");
					//update check status
					hashedValidationNumbers.put(hashedValidationNumber, "checked");
					System.out.println("Vote registered!");
				}
			}
		

	}
	private void addVote(String party)
	{
		if (partyCount.containsKey(party))
		{
			long numberOfVotes = partyCount.get(party);
			numberOfVotes++;
			partyCount.put(party, numberOfVotes);
		}
		// if new party found
		else
		{
			partyCount.put(party, 1L);
		}
	}
	private void associateIDWithParty(String hashedIDNumber, String party)
	{
		List<String> hashedIDNumbers;
		if (!votes.containsKey(party))
		{
			System.out.println("Does not contain key");
			hashedIDNumbers = new ArrayList<String>();
			hashedIDNumbers.add(hashedIDNumber);
			votes.put(party, hashedIDNumbers);
		}
		else
		{
			System.out.println("Contains key");
			hashedIDNumbers = new ArrayList<String>(votes.get(party));
			hashedIDNumbers.add(hashedIDNumber);
			votes.put(party, hashedIDNumbers);
		}

		for (int k = 0; k < votes.get(party).size(); k++)
		{
			System.out.println("HEJ1");
			System.out.println(votes.get(party).get(k));
			System.out.println("HEJ2");
		}
		

	}

	public static void main(String[] args) 
	{

		CentralTabulatingFacility ctfServer = new CentralTabulatingFacility();
		ctfServer.run();
	}

}