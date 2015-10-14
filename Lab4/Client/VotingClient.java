import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.*;
import java.lang.Object;

public class VotingClient
{
	private InetAddress host;
	private int port;
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "clientKeyStore.ks";
	static final String TRUSTSTORE = "clientTrustStore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";

	private BufferedReader socketIn;
	private PrintWriter socketOut;

	//constructor
	public VotingClient(InetAddress theHost, int thePort)
	{
		host = theHost;
		port = thePort;
	}
	public void run()
	{
		try
		{
			/* 
			The client, ctf and cla contains each others certificates
			in the trust stores. Think it is the correct way of doing it.
			*/

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
			SSLSocket client = (SSLSocket)sslFact.createSocket(host, port);

			client.setEnabledCipherSuites(client.getSupportedCipherSuites());
			System.out.println("\n>>>> SSL/TLS handshake completed");

			//initialize sockets

			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			socketOut = new PrintWriter(client.getOutputStream(), true);

			Voter v = new Voter("Einar");
			askCLAForValidationNum(v);

		}

		catch (Exception e)
		{
			System.out.println("Error" + e.toString());
		}
	}
	private void askCLAForValidationNum(Voter v)
	{
		System.out.println("HEJ KLIENT");
		socketOut.println("valnum");			
	}


	public static void main(String[] args)
	{
		try
		{

			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			VotingClient client = new VotingClient(host, port);
			client.run();
		}
		catch (Exception e)
		{
			System.out.println("Error" + e.toString());
		}
	}
}