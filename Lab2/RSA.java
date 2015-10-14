import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Random;
public class RSA
{
	private BigInteger n;
	private BigInteger e;
	private BigInteger d;

	private BigInteger p;
	private BigInteger q;
	//constructor
	public RSA()
	{

		p =  BigInteger.probablePrime(2048, new Random());
		q = BigInteger.probablePrime(2048, new Random());
		n = p.multiply(q);
		e = BigInteger.probablePrime(2048, new Random());
		//calculate decryption exponent d using inverse modulus (private key)

		// PHI = p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)
		d = e.modInverse(p.subtract(BigInteger.ONE).multiply(q.subtract(BigInteger.ONE)));

	}

	public BigInteger encryptMsg(String input, BigInteger exp, BigInteger num)
	{
		//create encryption
		BigInteger c = new BigInteger(input.getBytes());
		//System.out.println(c +  " " +  e + " " + n);
		c = c.modPow(exp,num);
		
		return c;
		
	}
	public String decryptMsg(BigInteger c)
	{
		BigInteger m = c.modPow(d,n);
		String decryptedMsg = new String(m.toByteArray());
		return decryptedMsg;
	}
	public BigInteger getN()
	{
		return n;
	}
	public BigInteger getE()
	{
		return e;
	}

}

