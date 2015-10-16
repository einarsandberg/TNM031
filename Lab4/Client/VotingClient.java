import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;

public class VotingClient
{
	private boolean running;
	private InetAddress host;
	private int port;
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "clientKeyStore.ks";
	static final String TRUSTSTORE = "clientTrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private BufferedReader socketInCLA;
	private PrintWriter socketOutCLA;

	private SSLSocket socketCLA;

	private PrintWriter socketOutCTF;
	private BufferedReader socketInCTF;
	private SSLSocket socketCTF;

	private String name;
	private long persNum;
	//constructor
/*	public VotingClient(InetAddress theHost, int thePort)
	{
		host = theHost;
		port = thePort;
	}*/
	public void run()
	{
		try
		{

			Voter v;

			KeyStore ks = KeyStore.getInstance("JCEKS");
			
			ks.load(new FileInputStream(KEYSTORE), STOREPASSWD.toCharArray());

			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load(new FileInputStream(TRUSTSTORE ), STOREPASSWD.toCharArray());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, ALIASPASSWD.toCharArray() );

			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);	

			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	

			socketCLA = (SSLSocket) sslFact.createSocket("localhost", 8190);

			socketCTF = (SSLSocket) sslFact.createSocket("localhost", 8191);

			socketCLA.setEnabledCipherSuites(socketCLA.getSupportedCipherSuites());
			

			socketCTF.setEnabledCipherSuites(socketCTF.getSupportedCipherSuites());

			System.out.println("\n>>>> SSL/TLS handshake completed");
			//initialize sockets

			socketInCLA = new BufferedReader(new InputStreamReader(socketCLA.getInputStream()));
			socketOutCLA = new PrintWriter(socketCLA.getOutputStream(), true);

			socketOutCTF = new PrintWriter(socketCTF.getOutputStream(), true);
			socketInCTF = new BufferedReader(new InputStreamReader(socketCTF.getInputStream()));
			running = true;
			int option = -1;

			while(running)
			{
				displayMenu();
				option = Integer.parseInt(new BufferedReader(new InputStreamReader(System.in)).readLine());
				switch(option)
				{
					case 1:
						System.out.println("Please enter your name: ");
						name = new BufferedReader(new InputStreamReader(System.in)).readLine();
						System.out.println("Please enter your social security number in format yymmddxxxx:");
						persNum = Long.parseLong(new BufferedReader(new InputStreamReader(System.in)).readLine());
						v = new Voter(name, persNum);
						boolean authorized = checkIfAuthorizedVoter(v);
						if (!authorized)
						{
							System.out.println("You're not authorized to vote!");
							break;
						}
						else
						{
							String hashedValidationNumber = askCLAForValidationNum(v.getName(), v.getPersonalNumber());
							if (hashedValidationNumber.equals("FRAUD"))
							{
								System.out.println("You have already voted!");
							}
							else
							{
								System.out.println("Please enter party: ");
								String party = new BufferedReader(new InputStreamReader(System.in)).readLine();
								v.createIDNumber();
								Vote vote = new Vote(v.getIDNumber(), hashedValidationNumber, party);
								String msgCTF = vote.createCTFMessage();
								System.out.println(msgCTF);
								sendMessageToCTF(msgCTF);
								String previousVoteCount = socketInCTF.readLine();
								System.out.println(previousVoteCount);
								String msg = socketInCTF.readLine();
								System.out.println(msg);
								String currentVoteCount = socketInCTF.readLine();
								System.out.println(currentVoteCount);
							}
						}

						break;
					case 2:
						break;

					case 3:
						break;

					case 4:
						running = false;
						break;
				}
			}
			
		}

		catch (Exception e)
		{
			System.out.println("Error");
			e.printStackTrace();
		}
	}

	private String askCLAForValidationNum(String name, long persNum)
	{
		try
		{
			socketOutCLA.println("validationNumStep");
			socketOutCLA.println(name);
			socketOutCLA.println(persNum);
			String hashedValidationNumber = socketInCLA.readLine();

			return hashedValidationNumber;
		}
		catch (Exception e)
		{
			System.out.println("Error in askCLAForValidationNum " + e.toString());
		}
		return "";
	}

	private void sendMessageToCTF(String msg)
	{
		socketOutCTF.println("msgFromClient");
		socketOutCTF.println(msg);
	}

	private void displayMenu()
	{
		System.out.println("What would you like to do? Please pick an option.");
		System.out.println("1. Vote");
		System.out.println("2. Show voters");
		System.out.println("3. Check my vote");
		System.out.println("4. Quit");

	}

	private boolean checkIfAuthorizedVoter(Voter v)
	{
		boolean authorized = false;
		try
		{
			socketOutCLA.println("authVoterCheck");
			socketOutCLA.println(v.getPersonalNumber());
			socketOutCLA.println(v.getName());
			authorized = Boolean.parseBoolean(socketInCLA.readLine());	
		}
		catch (Exception e)
		{
			System.out.println("Error checking if authorized voter");
			e.printStackTrace();
		}
		return authorized;
	}



	// hash with the SHA1 algorithm
	private String hash(String validation)
	{
		return "";
	}

	public static void main(String[] args)
	{
		try
		{

			/*InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			VotingClient client = new VotingClient(host, port);*/
			VotingClient client = new VotingClient();
			client.run();
		}
		catch (Exception e)
		{
			System.out.println("Error" + e.toString());
		}
	}
}