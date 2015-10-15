import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;
import java.util.*;

public class VotingClient
{
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
	//private BufferedReader socketInCTF;
	private SSLSocket socketCTF;
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

			Voter v = new Voter("Einar", 9201011111L);
			long validationNum = askCLAForValidationNum(v.getName(), v.getPersonalNumber());

			String party = "Socialdemokraterna";

			v.createIDNumber();
			Vote vote = new Vote(v.getIDNumber(), validationNum, party);
			String msgCTF = vote.createCTFMessage();
			System.out.println(msgCTF);

			sendMessageToCTF(msgCTF);



		}

		catch (Exception e)
		{
			System.out.println("Error" + e.toString());
		}
	}

	private long askCLAForValidationNum(String name, long persNum)
	{
		try
		{
			socketOutCLA.println("validationNumStep");
			socketOutCLA.println(name);
			socketOutCLA.println(persNum);
			long validationNum = Long.parseLong(socketInCLA.readLine());
			System.out.println(validationNum);
			return validationNum;
		}
		catch (Exception e)
		{
			System.out.println("Error in askCLAForValidationNum " + e.toString());
		}
		return -1;
	}
	private void sendMessageToCTF(String msg)
	{
		socketOutCTF.println("msgFromClient");
		socketOutCTF.println(msg);
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