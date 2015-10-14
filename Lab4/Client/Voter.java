public class Voter
{
	private int validationNumber;
	private int idNumber;
	private String name;
	/*public Voter(int valNum, int idNum)
	{
		validationNumber = valNum;
		idNumber = idNum;
	}*/

	/*public Voter(int id)
	{
		idNumber = id;
	}*/
	public Voter(String theName)
	{
		name = theName;
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


}