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

	BufferedReader socketIn;
	PrintWriter socketOut;
	SSLSocket streamCLA;
	private boolean running = true;

	private SSLServerSocket serverSocketCLA;
	private SSLServerSocket serverSocketClient;
	private SSLSocket socketCLA;
	private SSLSocket socketClient;

	private List <Long> validationNumList;
	/*public CentralTabulatingFacility(int thePort)
	{
		port = thePort;
	}*/
	public void run()
	{
		try
		{
			validationNumList = new ArrayList<Long>();
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

				socketIn = new BufferedReader(new InputStreamReader(streamCLA.getInputStream()));
				socketOut = new PrintWriter(streamCLA.getOutputStream(), true);
				String s = socketIn.readLine();

				if (s!= null)
				{
					switch (s)
					{
						case "validationNumToCTF":
							long valNum= Long.parseLong(socketIn.readLine());
							validationNumList.add(valNum);
							System.out.println(valNum);
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

	public static void main(String[] args) 
	{

		CentralTabulatingFacility ctfServer = new CentralTabulatingFacility();
		ctfServer.run();
	}

}