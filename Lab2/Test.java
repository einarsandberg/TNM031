import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
public class Test
{
	public static void main(String []args) 
	{
		RSA test = new RSA();
		System.out.println("Enter your message: ");
		String input = "";
		try
		{
			input = (new BufferedReader(new InputStreamReader(System.in))).readLine();
		}
		catch (IOException ex)
		{
			System.out.println("Wrong input!");
		}
		// e and n are the public keys available for user
		BigInteger encryptedMsg = test.encryptMsg(input, test.getE(), test.getN());
		String decryptedMsg = test.decryptMsg(encryptedMsg);
		System.out.println("Original message: " + input);
		System.out.println("Encrypted message: " +  encryptedMsg);
		System.out.println("Decrypted message: " + decryptedMsg);
	}
}
