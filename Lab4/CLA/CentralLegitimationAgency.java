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
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "CLAKeyStore.ks";
	static final String TRUSTSTORE = "CLATrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private Map <Long, Voter> voters; // map with validation number as key and voter as value
	BufferedReader socketIn;
	PrintWriter socketOut;
	SSLSocket incoming;
	private boolean running = true;
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
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket(port);

			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );

			//client authentication is required
			sss.setNeedClientAuth(true);
			System.out.println("Central Legitimation Agency is now active");
			
			while (running)
			{
				incoming = (SSLSocket)sss.accept();
				socketIn = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
				socketOut = new PrintWriter(incoming.getOutputStream(), true);
				String s = socketIn.readLine();
				if (s!= null)
				{
					switch (s)
					{
						case "validationNumStep":
							String name = socketIn.readLine();
							long persNumber = Long.parseLong(socketIn.readLine());
							sendValidationNumber(name, persNumber);
							break;

						default:
							running = false;
							break;

					}
				}

			}
			incoming.close();


		}
		catch (Exception e)
		{
			System.out.println("Server failed ");
			e.printStackTrace();
		}

	}
	private void sendValidationNumber(String name, long persNumber)
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
			socketOut.println(validationNum);
		}
		else
		{	
			
			List <Voter> listOfVoters = new ArrayList<Voter>(voters.values());
			//check for fraud
			fraud = checkForFraud(listOfVoters, persNumber);

			//no fraud found, add new voter
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
				socketOut.println(validationNum);

			}

		}
		System.out.println(v.toString());
		System.out.println(validationNum);

	}
	
	public boolean checkForFraud(List<Voter> listOfVoters, long persNumber)
	{
		boolean fraud = false;
			for (int i = 0; i < listOfVoters.size(); i++)
			{
				System.out.println(listOfVoters.get(i).getPersonalNumber());
				if (persNumber == listOfVoters.get(i).getPersonalNumber());
				{
					System.out.println("Voter has already voted.");
					socketOut.println("Election fraud detected!");
					fraud = true;
					break;
				}
			}
			return fraud;

	}


	public static void main(String[] args) 
	{
		int port = DEFAULT_PORT;
		if (args.length > 0) 
		{
			port = Integer.parseInt(args[0]);
		}	
		CentralLegitimationAgency claServer = new CentralLegitimationAgency(port);
		claServer.run();
	}

}