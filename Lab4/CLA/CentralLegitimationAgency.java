import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
public class CentralLegitimationAgency
{
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8190;
	static final String KEYSTORE = "CLAKeyStore.ks";
	static final String TRUSTSTORE = "CLATrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private Map <Long, Voter> voters; // map with validation number as key and voter as value
	BufferedReader socketInClient;
	PrintWriter socketOutClient;
	SSLSocket streamClient;
	private boolean running = true;

	private SSLSocketFactory socketFactory;
	private SSLSocket socketCTF;
	private SSLSocket socketClient;

	PrintWriter socketOutCTF;
	BufferedReader socketInCTF;
	public CentralLegitimationAgency(int thePort)
	{
		port = thePort;
	}
	public void run()
	{
		try
		{

			voters = new HashMap <Long, Voter>();
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
			//SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(port);
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(8190);
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );

			SSLSocketFactory sslFact = sslContext.getSocketFactory();

			//client authentication is required
			sss.setNeedClientAuth(true);
			System.out.println("Central Legitimation Agency is now active");

			//socketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
			socketCTF = (SSLSocket) sslFact.createSocket("localhost", 8192);

			while (running)
			{
				streamClient = (SSLSocket)sss.accept();

				socketOutCTF = new PrintWriter(socketCTF.getOutputStream(), true);
				socketInCTF = new BufferedReader(new InputStreamReader(socketCTF.getInputStream()));

				socketInClient = new BufferedReader(new InputStreamReader(streamClient.getInputStream()));
				socketOutClient = new PrintWriter(streamClient.getOutputStream(), true);
				
				String s = socketInClient.readLine();
				if (s!= null)
				{
					switch (s)
					{
						case "validationNumStep":
							String name = socketInClient.readLine();
							long persNumber = Long.parseLong(socketInClient.readLine());
							long validationNumber = createValidationNumber(name, persNumber);
							if (validationNumber == -1)
							{
								System.out.println("FRAUD!!!");
								break;
							}
							System.out.println(validationNumber);
							sendValidationNumberToClient(validationNumber);
							sendValidationNumberToCTF(validationNumber);

							break;

						default:
							running = false;
							break;
					}
				}

			}
			streamClient.close();
		}
		catch (Exception e)
		{
			System.out.println("Server failed ");
			e.printStackTrace();
		}

	}
	private long createValidationNumber(String name, long persNumber)
	{
		Voter v = new Voter(name, persNumber);
		Random rand = new Random();
		long min = 1L;
		long max = 1000000000L;
		boolean fraud = false;
		// random validation number from 1 to 1000000000
		long validationNum = min + ((long)(rand.nextDouble()*(max-min))); 
		// first voter
		if (voters.isEmpty())
		{
			voters.put(validationNum, v);

			//send validation number to client
			return validationNum;
		}
		else
		{	
			List <Voter> listOfVoters = new ArrayList<Voter>(voters.values());
			//check for fraud by comparing saved person numbers with current person number
			fraud = checkForFraud(listOfVoters, persNumber);

			// if no fraud found, add new voter
			if (!fraud)
			{
				// produce new validation number if another voter already has it
				while(voters.containsKey(validationNum))
				{
					validationNum = min + ((long)(rand.nextDouble()*(max-min)));
				}
				voters.put(validationNum, v);
				System.out.println("NO FRAUD");
				//send validation number to client
				return validationNum;
			}
		}
		System.out.println(v.toString());
		System.out.println(validationNum);

		return -1;
	}
	private void sendValidationNumberToClient(long validationNumber)
	{
		socketOutClient.println(validationNumber);
	}

	private void sendValidationNumberToCTF(long valNum)
	{
		socketOutCTF.println("validationNumToCTF");
		socketOutCTF.println(valNum);
	}

	private boolean checkForFraud(List<Voter> listOfVoters, long persNumber)
	{
		boolean fraud = false;
		for (int i = 0; i < listOfVoters.size(); i++)
		{
			System.out.println(listOfVoters.get(i).getPersonalNumber());
			if (persNumber == listOfVoters.get(i).getPersonalNumber());
			{
				System.out.println("Voter has already voted.");
				socketOutClient.println("Election fraud detected!");
				fraud = true;
				break;
			}
		}
			return fraud;
	}



	public static void main(String[] args) 
	{
		CentralLegitimationAgency claServer = new CentralLegitimationAgency(8190);
		claServer.run();
	}

}