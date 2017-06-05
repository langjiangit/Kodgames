package com.kodgames.message.generaor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;

import com.kodgames.message.generaor.Paragraph.ProtocolInfo;

public class ClientLuaProtocolsGenerator 
{
	private String descriptionFile = null;
	private String generatedFile = null;
	private String extralGeneratedFile = null;
	
	public ClientLuaProtocolsGenerator(String descriptionFile, String generatedFile, String extralGenerateedFile)
	{
		this.descriptionFile = descriptionFile;
		this.generatedFile = generatedFile;
		this.extralGeneratedFile = extralGenerateedFile;
	}
	
	public void generateFile() throws DocumentException, IOException
	{
		ProtocolParser parser = new ProtocolParser(descriptionFile);
		List<Paragraph> paragraphs =  parser.parseFile();
		FileWriter fWriter = new FileWriter(generatedFile);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		
		FileWriter exWriter = new FileWriter(extralGeneratedFile);
		BufferedWriter exBWriter = new BufferedWriter(exWriter);
		
		bWriter.write("-- This file is generated by program. No not edit it manually.");
		bWriter.newLine();
		bWriter.newLine();
		bWriter.write("local protocode = {");
		
		exBWriter.write("-- This file is generated by program. No not edit it manually.");
		exBWriter.newLine();
		exBWriter.write("local protocodetext = {");
		exBWriter.newLine();
		exBWriter.newLine();
		
	    Paragraph info = paragraphs.get(0);
		
	    long protocolId = info.getProtocolStartID();
		bWriter.newLine();
		bWriter.newLine();
		bWriter.write("\t\t--" + info.getDescription());
		for(ProtocolInfo protocol : info.getProtocols())
		{
			bWriter.newLine();
			bWriter.write("\t\t--" + protocol.getProtocolComment());
			bWriter.newLine();
			bWriter.write("\t\t" + protocol.getProtocolName() + " = 0x" + Long.toHexString(protocolId) + ",");
			
			long resultSuccessStart = 1;
			long resultFailStart = 16;

			for(int i = 0 ; i < protocol.getResultNames().size(); i++)
			{
				Result result = protocol.getResultNames().get(i);
				bWriter.newLine();
				bWriter.write("\t\t--" + protocol.getDescNames().get(i));
				bWriter.newLine();
				exBWriter.newLine();
				if(result.isSuccess())
				{
					bWriter.write("\t\t" + result.getName() + " = 0x" + Long.toHexString(protocolId + (resultSuccessStart << 20)) + ",");
					//exBWriter.write("\t\t[0x" + Long.toHexString(protocolId + (resultSuccessStart << 20)) + "] = " + "\"" +protocol.getDescNames().get(i) +  "\",");
					++ resultSuccessStart;
				}
				else
				{
					bWriter.write("\t\t" + result.getName() + " = 0x" + Long.toHexString(protocolId + (resultFailStart << 20)) + ",");
					exBWriter.write("\t\t[0x" + Long.toHexString(protocolId + (resultFailStart << 20)) + "] = " + "\"" +protocol.getDescNames().get(i) +  "\",");
					++ resultFailStart;
				}
			}
			++protocolId;
		}
		
		bWriter.newLine();
		bWriter.newLine();
		bWriter.write("}");
		bWriter.newLine();
		bWriter.newLine();
		bWriter.write("return protocode");
		bWriter.close();
		
		exBWriter.newLine();
		exBWriter.write("}");
		exBWriter.newLine();
		exBWriter.write("return protocodetext");
		exBWriter.close();
	}
}
