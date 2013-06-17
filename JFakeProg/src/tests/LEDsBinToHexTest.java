/**
 * 
 */
package tests;

import java.util.Scanner;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class LEDsBinToHexTest
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Scanner scanner = new Scanner(System.in);
		String output = "";
		boolean exit = false;
		while(!exit)
		{
			String line = scanner.nextLine();
			if(line.equalsIgnoreCase("exit"))
				exit = true;
			else
			{
				int num = Integer.parseInt(line, 2);
				output += String.format("%02x", num);
			}
		}
		System.out.println(output);
		scanner.close();
	}

}
