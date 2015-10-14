public class Voter
{
	private int validationNumber;
	private int idNumber;
	private String name;
	private long personNumber; // ex 9207261111

	public Voter(String theName, long thePersonNumber)
	{
		name = theName;
		personNumber = thePersonNumber;
	}

	public int getValidationNumber()
	{
		return validationNumber;
	}

	public int getID()
	{
		return idNumber;
	}
	public String getName()
	{
		return name;
	}

	public void setValidationNumber(int num)
	{
		validationNumber = num;
	}

	public long getPersonalNumber()
	{
		return personNumber;
	}

	public String toString()
	{
		return (name+ ":" + String.valueOf(personNumber));
	}


}