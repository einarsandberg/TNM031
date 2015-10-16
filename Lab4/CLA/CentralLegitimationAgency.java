import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;
import java.math.*;
public class CentralLegitimationAgency
{
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8190;
	static final String KEYSTORE = "CLAKeyStore.ks";
	static final String TRUSTSTORE = "CLATrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private Map <String, Voter> voters; // map with validation number as key and voter as value
	BufferedReader socketInClient;
	PrintWriter socketOutClient;
	SSLSocket streamClient;
	private boolean running = true;

	private SSLSocketFactory socketFactory;
	private SSLSocket socketCTF;
	private SSLSocket socketClient;

	PrintWriter socketOutCTF;
	BufferedReader socketInCTF;

	private List<Voter> authorizedVoters;

	public CentralLegitimationAgency(int thePort)
	{
		port = thePort;
	}
	public void run()
	{
		try
		{
			authorizedVoters = new ArrayList<Voter>();
			// add a few authorized voters
			authorizedVoters.add(new Voter("Einar", 9207261111L));
			authorizedVoters.add(new Voter("Glenn", 7701011234L));
			authorizedVoters.add(new Voter("Anna", 5005055555L));
			authorizedVoters.add(new Voter("Maria", 9302029999L));

			// voters uses hashed validation number as key, and a voter as value
			voters = new HashMap <String, Voter>();
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

			streamClient = (SSLSocket)sss.accept();
			socketOutCTF = new PrintWriter(socketCTF.getOutputStream(), true);
			socketInCTF = new BufferedReader(new InputStreamReader(socketCTF.getInputStream()));

			socketInClient = new BufferedReader(new InputStreamReader(streamClient.getInputStream()));
			socketOutClient = new PrintWriter(streamClient.getOutputStream(), true);
			String name="";
			while (running)
			{
				String s = socketInClient.readLine();
				if (s!= null)
				{
					switch (s)
					{
						case "authVoterCheck":
							long personNumber = Long.parseLong(socketInClient.readLine());
							name = socketInClient.readLine();
							boolean authorized = checkIfAuthorizedVoter(name, personNumber);
							System.out.println(authorized);
							socketOutClient.println(authorized);
							break;

						case "validationNumStep":
							name = socketInClient.readLine();
							long persNumber = Long.parseLong(socketInClient.readLine());
							String hashedValidationNumber = createValidationNumber(name, persNumber);
							
							if (hashedValidationNumber.equals("FRAUD"))
							{
								System.out.println("Election fraud detected!");
							}
			
							System.out.println(hashedValidationNumber);
							sendValidationNumberToClient(hashedValidationNumber);
							sendValidationNumberToCTF(hashedValidationNumber);

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

	private boolean checkIfAuthorizedVoter(String name, long personNumber)
	{
		System.out.println(name);
		System.out.println(personNumber);
		Voter v = new Voter(name, personNumber);

		for (int i = 0; i < authorizedVoters.size(); i++)
		{
			if (v.equals(authorizedVoters.get(i)))
			{
				return true;
			}
		}
		return false;
	}

	// uses hash function to encrypt the validation number
	private String createValidationNumber(String name, long persNumber)
	{
		Voter v = new Voter(name, persNumber);
		Random rand = new Random();
		long min = 1L;
		long max = 1000000000L;
		boolean fraud = false;
		// random validation number from 1 to 1000000000
		String hashedValidationNumber = hash(min + ((long)(rand.nextDouble()*(max-min)))); 
		// first voter
		if (voters.isEmpty())
		{
			voters.put(hashedValidationNumber, v);
			//send validation number to client
			return hashedValidationNumber;
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
				while(voters.containsKey(hashedValidationNumber))
				{
					hashedValidationNumber = hash(min + ((long)(rand.nextDouble()*(max-min))));
				}
				voters.put(hashedValidationNumber, v);
				System.out.println("NO FRAUD");
				//send validation number to client
				return hashedValidationNumber;
			}
		}

		return "FRAUD";
	}
	private void sendValidationNumberToClient(String hashedValidationNumber)
	{
		socketOutClient.println(hashedValidationNumber);
	}

	private void sendValidationNumberToCTF(String hashedValidationNumber)
	{
		socketOutCTF.println("hashedValidationNumToCTF");
		socketOutCTF.println(hashedValidationNumber);
	}

	private boolean checkForFraud(List<Voter> listOfVoters, long persNumber)
	{
		boolean fraud = false;
		for (int i = 0; i < listOfVoters.size(); i++)
		{
			if (persNumber == listOfVoters.get(i).getPersonalNumber())
			{
				System.out.println("Voter has already voted.");
				fraud = true;
				break;
			}
		}
			return fraud;
	}
	// hash with the sha1 algorithm
	private String hash(long validationNumber)
	{	
		try
		{
			if (validationNumber == -1)
				return "FRAUD";

			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
			crypt.reset();
			crypt.update(String.valueOf(validationNumber).getBytes("UTF-8"));

			return new BigInteger(1, crypt.digest()).toString(16);
		}
		catch (Exception e)
		{
			System.out.println("Error when hashing");
			e.printStackTrace();
		}
		return "FRAUD";
	}	


	public static void main(String[] args) 
	{
		CentralLegitimationAgency claServer = new CentralLegitimationAgency(8190);
		claServer.run();
	}

}