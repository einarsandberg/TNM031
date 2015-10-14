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

	private List <Voter> voters;
	BufferedReader socketIn;
	PrintWriter socketOut;
	public CentralLegitimationAgency(int thePort)
	{
		port = thePort;
	}
	public void run()
	{
		try
		{

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
			SSLSocket incoming = (SSLSocket)sss.accept();

			socketIn = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			socketOut = new PrintWriter(incoming.getOutputStream(), true);

			if (socketIn.readLine().equals("valnum"))
			{
				System.out.println("HEJJJ");
			}


		}
		catch (Exception e)
		{
			System.out.println("Server failed " + e.toString());
		}

	}
	private void sendValidationNumber()
	{

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