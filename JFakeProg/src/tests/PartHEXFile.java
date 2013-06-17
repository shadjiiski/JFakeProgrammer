/**
 * 
 */
package tests;

import java.io.PrintWriter;

import org.jfakeprog.hex.IHEX8Record;
import org.jfakeprog.hex.IHEX8Record.Type;
import org.jfakeprog.hex.util.HEXFileParser;
import org.jfakeprog.hex.util.HEXRecordSet;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class PartHEXFile
{

	public static void main(String[] args) throws Exception
	{
		HEXRecordSet records = new HEXFileParser("Programmer.hex").parse();
		records.shortenRecords(8);
		for(int i = 0; i < records.size(); i++)
		{
			IHEX8Record record = records.get(i);
			if(record.getRecordType() == Type.EOF)
				continue;
			PrintWriter pw = new PrintWriter(String.format("prog_parts/part%03d.hex", (i + 1)));
			pw.println(record.toHEXString());
			pw.close();
		}
	}

}
