
// An example class that uses the secure server socket class

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.util.StringTokenizer;
import java.lang.Object;


public class SecureAdditionServer {
	private int port;
	// This is not a reserved port number
	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "LIUkeystore.ks";
	static final String TRUSTSTORE = "LIUtruststore.ks";
	static final String STOREPASSWD = "123456";
	static final String ALIASPASSWD = "123456";
	
	/** Constructor
	 * @param port The port where the server
	 *    will listen for requests
	 */
	SecureAdditionServer( int port ) {
		this.port = port;
	}
	
	/** The method that does the work for the class */
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
			// load correct keystor with password
			ks.load( new FileInputStream( KEYSTORE ), STOREPASSWD.toCharArray() );

			//load truststore with correct password
			KeyStore ts = KeyStore.getInstance( "JCEKS" );
			ts.load( new FileInputStream( TRUSTSTORE ), STOREPASSWD.toCharArray() );
			// use SUNX509 as key manager algorithm
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
			kmf.init( ks, ALIASPASSWD.toCharArray() );
			// use SUNx509 as trust manager algorithm
			// init with key store
			TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
			tmf.init( ts );
			

			SSLContext sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket sss = (SSLServerSocket) sslServerFactory.createServerSocket( port );

			//  cipher suite is a named combination of authentication, 
			// encryption, message authentication code (MAC) and key exchange algorithms
			sss.setEnabledCipherSuites( sss.getSupportedCipherSuites() );

			//client authentication is required
			sss.setNeedClientAuth(true);
			System.out.println("\n>>>> SecureAdditionServer: active ");
			SSLSocket incoming = (SSLSocket)sss.accept();

      		BufferedReader in = new BufferedReader( new InputStreamReader( incoming.getInputStream() ) );
			PrintWriter out = new PrintWriter( incoming.getOutputStream(), true );			
			int option=-1;
			String fileName;
			String fileData;
			option = Integer.parseInt(in.readLine());


			if (option == 1)
			{
				fileName = in.readLine();
				
				System.out.println(fileName);

				fileData = readFile(fileName);
				out.println(fileData);
			}
			else if (option == 2)
			{
				fileName = in.readLine();
				fileData = readStringFromClient(in);

				createTextFile(fileName, fileData);
			}

			else if (option == 3)
			{
				fileName = in.readLine();
				deleteFile(fileName);

			}
			incoming.close();
		}

		catch( Exception e ) 
		{
			e.printStackTrace();
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
	private void createTextFile(String fileName, String text)
	{
		String serverFileName = "server"+fileName;
		System.out.println(serverFileName);
		PrintWriter writer;
		try
		{
			writer = new PrintWriter(serverFileName, "UTF-8");
			writer.print(text);
			writer.close();
		}
		catch (Exception e)
		{
			System.out.println("Error writing to file");
			e.printStackTrace();
		}

	}
		private String readStringFromClient(BufferedReader socketIn)
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
	private void deleteFile(String fileName)
	{
		try
		{
			File file = new File(fileName);
			file.delete();
		}
		catch (Exception e)
		{
			System.out.println("Error deleting file");
			e.printStackTrace();
		}
	}

		/** The test method for the class
	 * @param args[0] Optional port number in place of
	 *        the default
	 */
	public static void main( String[] args ) {
		int port = DEFAULT_PORT;
		if (args.length > 0 ) {
			port = Integer.parseInt( args[0] );
		}
		SecureAdditionServer addServe = new SecureAdditionServer( port );
		addServe.run();
	}
}

