// A client-side class that uses a secure TCP/IP socket

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;

public class SecureAdditionClient {
	private InetAddress host;
	private int port;
	// This is not a reserved port number 
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "PIERkeystore.ks";
	static final String TRUSTSTORE = "PIERtruststore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";
  
	// Constructor @param host Internet address of the host where the server is located
	// @param port Port number on the host where the server is listening
	public SecureAdditionClient( InetAddress host, int port ) {
		this.host = host;
		this.port = port;
	}
	
  // The method used to start a client object
	public void run() {
		try 
		{

			/* SSL handshake works this way: 
			1. Client hello. Information that server needs to communicate with client using SSL.
				Includes SSL version, cipher settings and session specific data.

			2. Server hello. Information that client needs to communicate with server using SSL.
				Includes SSL version, cipher settings and session specific data.
				Including server's certificate, a public key.

			3. Client authenticates server certificate. Client creates the pre-master secret for the session.
				Encrypts with server's public key and sends back the encrypted pre-master secret to the server.

			4. Server uses its private key to decrypt the pre-master secret. 
				Both server and client perform steps to generate master secret with agreed cipher.

			5. Both the client and server use the master secret to generate session keys, which are symmetric keys
				used to encrypt/decrypt the information during the session.

			6. Both client and server exchange messages to inform that future messages will be encrypted.
			*/

			//get keystores with specified format
			KeyStore ks = KeyStore.getInstance( "JCEKS" );
			// load correct keystore with password
			ks.load( new FileInputStream( KEYSTORE ), STOREPASSWD.toCharArray() );

			//load truststore with correct password
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), STOREPASSWD.toCharArray() );
			// use SUNX509 as key manager algorithm
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, ALIASPASSWD.toCharArray() );
			// use SUNX509 as trust manager algorithm
			// init with key store
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			// SSL stuff
			// The client sends SSL version, random (used to protect key exchange),
			// session id, and CipherSuite to server
			// Server sends back SSL version, random (a different number), SessionID and CipherSuite
			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket client =  (SSLSocket)sslFact.createSocket(host, port);



			//  cipher suite is a named combination of authentication, 
			// encryption, message authentication code (MAC) and key exchange algorithms
			client.setEnabledCipherSuites( client.getSupportedCipherSuites() );
			System.out.println("\n>>>> SSL/TLS handshake completed");
			// Completed if all four rounds done
			// four rounds:
			// 1. Establish security capabilities
			// 2. Server authentication and key exchange
			// 3. Client authentication and key exchange
			// 4. Finished

			
			BufferedReader socketIn;
			socketIn = new BufferedReader( new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);
			
			int chosenOption=-1;


			printMenu();

				chosenOption = getChosenOption();
				//forward option to server
				socketOut.println(chosenOption);
				switch(chosenOption)
				{
					case 0: 
						System.out.println("WRONG OPTION");
						break;
					case 1:
						System.out.println("Enter the file name: ");
						try
						{
							String fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();
							System.out.println("Downloading the file " + fileName + " from server");
							//forward file name to server
							socketOut.println(fileName);

							String fullText = readStringFromServer(socketIn);
							createTextFile(fileName, fullText);
							//System.out.println(fullText);
						}
						catch (Exception e)
						{
							System.out.println("Something went wrong");
						}
						break;
					case 2:
						System.out.println("Enter the file name: ");
						try
						{
							String fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();
							String fileData= readFile(fileName);
							System.out.println("Uploading the file " + fileName + " to server");
							// forward file name and data to server
							socketOut.println(fileName);
							socketOut.println(fileData);

						}
						catch(Exception e)
						{
							System.out.println("Error uploading file");
							e.printStackTrace();
						}
						break;
					case 3:
						System.out.println("Enter the file name: ");
						try
						{
							String fileName = new BufferedReader(new InputStreamReader(System.in)).readLine();
							System.out.println("Deleting the file " + fileName + " from server");
							//forward file name to server
							socketOut.println(fileName);
						}
						catch (Exception e)
						{
							System.out.println("Error deleting file");
							e.printStackTrace();
						}
						break;
					case 4:
						System.out.println("Exiting...");
						break;

					default: 
						System.out.println("WRONG OPTION");
						break;
				}
			}

		
			
			//socketOut.println( numbers );
			
			
		
		catch( Exception e ) {
			
			e.printStackTrace();
		}
	}
	private void createTextFile(String fileName, String text)
	{
		String clientFileName = "client"+fileName;
		System.out.println(clientFileName);
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(clientFileName, "UTF-8");
			writer.print(text);
			writer.close();
		}
		catch (Exception e)
		{
			System.out.println("Error writing to file");
			e.printStackTrace();
		}

	}
	private String readStringFromServer(BufferedReader socketIn)
	{
		try
		{
			StringBuilder sb = new StringBuilder();
			String line = socketIn.readLine();
			String fullText="";
			while (line!= null)
			{
				sb.append(line);
	        	sb.append(System.lineSeparator());
	        	line = socketIn.readLine();
			}
			fullText = sb.toString();
			return fullText;
		}
		catch (Exception e)
		{
			System.out.println ("Error reading string from server");
			e.printStackTrace();
			return "";

		}
	}
	private String readFile(String fileName)
	{
		try
		{

			BufferedReader br = new BufferedReader(new FileReader(fileName));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			while(line!=null)
			{
				sb.append(line);
	        	sb.append(System.lineSeparator());
	        	line = br.readLine();
	        }
	        // return full text
	        return sb.toString();
	    }
	    catch (Exception e)
	    {
	    	System.out.println("Couldn't read file " + fileName + " Exception: " + e.toString());
	    	e.printStackTrace();
	    	return "";
	    }
	}
	private void printMenu()
	{
		System.out.println("What would you like to do? Please pick an option: ");
		System.out.println("1. Download text file from server: ");
		System.out.println("2. Upload text file to server");
		System.out.println("3. Delete text file from server");
		System.out.println("4. Quit");
	}
	private int getChosenOption()
	{
		int intInput = 0;
		try
		{
			String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
			intInput = Integer.parseInt(input);
		}
		catch (Exception e)
		{
			System.out.println("Wrong option.");
			e.printStackTrace();
			return 0;
		}
		return intInput;

	}
	// The test method for the class @param args Optional port number and host name
	public static void main( String[] args ) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if ( args.length > 0 ) {
				port = Integer.parseInt( args[0] );
			}
			if ( args.length > 1 ) {
				host = InetAddress.getByName( args[1] );
			}
			SecureAdditionClient addClient = new SecureAdditionClient( host, port );
			addClient.run();
		}
		catch ( UnknownHostException uhx ) {
			System.out.println( uhx );
			uhx.printStackTrace();
		}
	}
}
