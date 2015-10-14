public class Voter
{
	//private int validationNumber;
	private int idNumber;
	private String name;
	private long persNumber;
	public Voter(String theName, long thePersNumber)
	{
		name = theName;
		persNumber = thePersNumber;
	}

	/*public int getValidationNumber()
	{
		return validationNumber;
	}*/

	public int getID()
	{
		return idNumber;
	}
	public String getName()
	{
		return name;
	}
	public String toString()
	{
		return (name + ":" + String.valueOf(persNumber));
	}
	public long getPersonalNumber()
	{
		return persNumber;
	}
	/*public void setValidationNumber(int num)
	{
		validationNumber = num;
	}*/


}