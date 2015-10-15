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
	private Map <Long, String> validationNumberMap;
	private Map <Long, Vote> votes;

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
			validationNumberMap = new HashMap<Long, String>();
			votes = new HashMap <Long, Vote>();
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
			
			while (running)
			{
				streamCLA = (SSLSocket)socketCLA.accept();
				streamClient = (SSLSocket)socketClient.accept();
				socketInFromCLA = new BufferedReader(new InputStreamReader(streamCLA.getInputStream()));
				socketOutToCLA = new PrintWriter(streamCLA.getOutputStream(), true);

				socketInFromClient = new BufferedReader(new InputStreamReader(streamClient.getInputStream()));
				socketOutToClient = new PrintWriter(streamCLA.getOutputStream(), true);

				String stringFromCLA = socketInFromCLA.readLine();
				String stringFromClient = socketInFromClient.readLine();
				if (stringFromCLA!= null)
				{
					switch (stringFromCLA)
					{
						case "validationNumToCTF":
							long valNum= Long.parseLong(socketInFromCLA.readLine());
							validationNumberMap.put(valNum, "unchecked");
							System.out.println(valNum);
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
							long idNumber = getIDNumberFromMsg(msgFromClient);
							String party = getPartyFromMsg(msgFromClient);
							long validationNumber = getValidationNumberFromMsg(msgFromClient);
							validationNumberMap.put(validationNumber, "unchecked");
							System.out.println(idNumber);
							System.out.println(validationNumber);
							System.out.println(party);
							checkValidationNumberInMap(validationNumber);
							addVote(party);
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

	private long getIDNumberFromMsg(String msg)
	{
		try
		{
			int index = msg.lastIndexOf(':');
			// get characters before last colon, will be id:validationNum
			String subStr = msg.substring(0, index-1);
			int index2 = subStr.lastIndexOf(':');
			long idNumber = Long.parseLong(subStr.substring(0,index2));
			return idNumber;
		}
		catch (Exception e)
		{
			System.out.println("Error getting id number");
			e.printStackTrace();
		}
		return -1;

	}
	private String getPartyFromMsg(String msg)
	{
		int index = msg.lastIndexOf(':');
		String party = msg.substring(index+1);
		return party;
	}
	private long getValidationNumberFromMsg(String msg)
	{
		try
		{
			int index = msg.lastIndexOf(':');
			// get characters before last colon, will be id:validationNum
			String subStr = msg.substring(0, index);
			int index2 = subStr.lastIndexOf(':');
			long validationNumber = Long.parseLong(subStr.substring(index2+1));
			return validationNumber;
		}
		catch (Exception e)
		{
			System.out.println("Error getting validation number");
			e.printStackTrace();
		}
		return -1;
	}
	private  void checkValidationNumberInMap(long validationNumber)
	{
			if (validationNumberMap.get(validationNumber) == "checked")
			{
				System.out.println("Election fraud detected! Validation number already checked.");
			}
			else
			{
				if (validationNumberMap.containsKey(validationNumber))
				{
					System.out.println("Validation number found and unchecked! Checking...");
					//update check status
					validationNumberMap.put(validationNumber, "checked");
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

	public static void main(String[] args) 
	{

		CentralTabulatingFacility ctfServer = new CentralTabulatingFacility();
		ctfServer.run();
	}

}