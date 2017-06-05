/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kodgames.message.generaor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.dom4j.DocumentException;

import com.kodgames.message.generaor.Paragraph.ProtocolInfo;

/**
 *
 * @author Elvin
 */
public class ClientHelperProtocolsGenerator 
{
	private String descriptionFile = null;
	private String generatedFile = null;
	private String variableType = "public const int";
	private List<Paragraph> paragraphCollection = null;
	private List<Long> allErrorCode = new ArrayList<Long>();

	public ClientHelperProtocolsGenerator(String descriptionFile, String generatedFile)
	{
		this.descriptionFile = descriptionFile;
		this.generatedFile = generatedFile;
	}
	
	public void generateFile() throws DocumentException, IOException
	{
		ProtocolParser protocolParser = new ProtocolParser(descriptionFile);
		this.paragraphCollection = protocolParser.parseFile();
		
		FileWriter fileWriter = new FileWriter(this.generatedFile, false);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		bufferedWriter.write("//This file is generated by program. Do not edit it manually.\n\n");
		bufferedWriter.write("using System;\n");
		bufferedWriter.write("using System.Collections.Generic;\n");
		bufferedWriter.write("using System.Text;\n\n");

		bufferedWriter.write("namespace com.kodgames.corgi.protocol\n");
		bufferedWriter.write("{\n");

		bufferedWriter.write("\tpublic class Protocols\n");
		bufferedWriter.write("\t{\n");
//		bufferedWriter.write("\t\tprivate List<long> allErrorCode = new List<long>();\n");
//		bufferedWriter.write("\t\tpublic List<long> AllErrorCode\n");
//		bufferedWriter.write("\t\t{\n");
//		bufferedWriter.write("\t\t\tget { return allErrorCode; }\n");
//		bufferedWriter.write("\t\t}\n\n");
		bufferedWriter.write("\t\tpublic static int Type(int id)\n");
		bufferedWriter.write("\t\t{\n");
		bufferedWriter.write("\t\t\treturn (int)(0xffff0000 & id);\n");
		bufferedWriter.write("\t\t}\n\n");

		bufferedWriter.write("\t\tpublic static int IdWithoutType(int id)\n");
		bufferedWriter.write("\t\t{\n");
		bufferedWriter.write("\t\t\treturn 0x0000ffff & id;\n");
		bufferedWriter.write("\t\t}\n\n");

		bufferedWriter.write("\t\tpublic static Boolean isSuccess(int id)\n");
		bufferedWriter.write("\t\t{\n");
		bufferedWriter.write("\t\t\tif(id <= 0)\n");
		bufferedWriter.write("\t\t\t\treturn true;\n");
		bufferedWriter.write("\t\t\tuint newId = (uint)id;\n");
		bufferedWriter.write("\t\t\treturn ((id & 0xfff00000)>>20) < 0x10;\n");
		bufferedWriter.write("\t\t}\n\n");
		
		bufferedWriter.write("\t\t//ServerType 0xxxxx0000\n");
		bufferedWriter.write("\t\tpublic const int SERVER_TYPE_AUTH = 0x00020000;\n");
		bufferedWriter.write("\t\tpublic const int SERVER_TYP_GAME = 0x00030000;\n");
		bufferedWriter.write("\t\tpublic const int SERVER_TYPE_BATTLE = 0x00040000;\n");

		bufferedWriter.write("\t\t//Protocol Type 0xxxxx0000\n");
		bufferedWriter.write("\t\tpublic const uint PROTOCOL_TYPE_ALL = 0xffff0000;\n");
		bufferedWriter.write("\t\tpublic const int PROTOCOL_TYPE_AUTH = 0x00020000;\n");
		bufferedWriter.write("\t\tpublic const int PROTOCOL_TYPE_GAME = 0x00030000;\n");
		bufferedWriter.write("\t\tpublic const int PROTOCOL_TYPE_BATTLE = 0x00040000;\n");

		for(Paragraph paragraph : this.paragraphCollection)
		{
			long protocolID = paragraph.getProtocolStartID();
			bufferedWriter.write("\t\t//" + paragraph.getDescription() + "\n");
			for(ProtocolInfo protocol : paragraph.getProtocols())
			{
				bufferedWriter.write("\t\t" + this.variableType + " " + protocol.getProtocolName() + " = 0x" + Long.toHexString(protocolID) + ";" +
						"\t//" + protocolID + "\n");
				
				long resultSuccessStartIndex=1;
				long resultFailureStartIndex=16;
				for(Result result : protocol.getResultNames())
				{
					if(result.isSuccess())
					{
						bufferedWriter.write("\t\t\t" + this.variableType + " " + result.getName() + " = 0x" + Long.toHexString(protocolID+(resultSuccessStartIndex<<20)) + 
								";" + "\t//" + (protocolID+(resultSuccessStartIndex<<20)));
						allErrorCode.add(protocolID+(resultSuccessStartIndex<<20));
						++resultSuccessStartIndex;
					}
					else
					{
						bufferedWriter.write("\t\t\t" + this.variableType + " " + result.getName() + " = 0x" + Long.toHexString(protocolID+(resultFailureStartIndex<<20)) + 
								";" + "\t//" + (protocolID+(resultFailureStartIndex<<20)));
						allErrorCode.add(protocolID+(resultFailureStartIndex<<20));
						++resultFailureStartIndex;
					}
					bufferedWriter.newLine();
				}
				
				++protocolID;
			}
			bufferedWriter.write("\n");
		}
		
//		bufferedWriter.write("\n\t\tpublic void initErrorCode()\n");
//		bufferedWriter.write("\t\t{\n");
//		bufferedWriter.write("\t\t\t#if UNITY_EDITOR\n");
//		for(Long elem : allErrorCode)
//			bufferedWriter.write("\t\t\tallErrorCode.add(" + elem + ");\n");
//		bufferedWriter.write("\t\t\t#endif\n");
//		bufferedWriter.write("\t\t}\n");

		bufferedWriter.write("\t}\n");
		bufferedWriter.write("}\n");

		bufferedWriter.close();
	}

}