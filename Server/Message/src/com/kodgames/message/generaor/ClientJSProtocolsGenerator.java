package com.kodgames.message.generaor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.dom4j.DocumentException;

import com.kodgames.message.generaor.Paragraph.ProtocolInfo;

public class ClientJSProtocolsGenerator 
{
	private String descriptionFile = null;
	private String generatedFile = null;
	
	public ClientJSProtocolsGenerator(String descriptionFile, String generatedFile)
	{
		this.descriptionFile = descriptionFile;
		this.generatedFile = generatedFile;
	}
	
	public void generateFile() throws DocumentException, IOException
	{
		ProtocolParser parser = new ProtocolParser(descriptionFile);
		List<Paragraph> paragraphs =  parser.parseFile();
		FileWriter fWriter = new FileWriter(generatedFile);
		BufferedWriter bWriter = new BufferedWriter(fWriter);
		
//		bWriter.write("///<reference path=\"Util.ts\" />");
//		bWriter.newLine();
		bWriter.write("module kodUtil {");
		bWriter.newLine();
		bWriter.write("\t//This file is generated by program. No not edit it manually.");
		bWriter.newLine();
		bWriter.write("\t\texport class PlatformErrorCode{");
		bWriter.newLine();
		bWriter.write("\t\t\tpublic static codeMap:kodUtil.Map<number,string> = new kodUtil.HashMap<number,string>();");
		
		for(Paragraph info : paragraphs){
			
			long protocolId = info.getProtocolStartID();
			bWriter.newLine();
			bWriter.newLine();
			bWriter.write("\t\t\t//" + info.getDescription());
			for(ProtocolInfo protocol : info.getProtocols())
			{
				bWriter.newLine();
				bWriter.write("\t\t\t//" + protocol.getProtocolComment());
				bWriter.newLine();
				bWriter.write("\t\t\tpublic static " + protocol.getProtocolName() + ":number = 0x" + Long.toHexString(protocolId) + ";");
				long resultSuccessStart = 1;
				long resultFailStart = 16;
				for(int i = 0 ; i < protocol.getResultNames().size(); i++)
				{
					Result result = protocol.getResultNames().get(i);
					bWriter.newLine();
					bWriter.write("\t\t\t//" + protocol.getDescNames().get(i));
					bWriter.newLine();
					if(result.isSuccess())
					{
						bWriter.write("\t\t\tpublic static " + result.getName() + ":number = 0x" + Long.toHexString(protocolId + (resultSuccessStart << 20)) + ";");
						++ resultSuccessStart;
					}else{
						bWriter.write("\t\t\tpublic static " + result.getName() + ":number = 0x" + Long.toHexString(protocolId + (resultFailStart << 20)) + ";");
						++ resultFailStart;
					}
				}
				++protocolId;
			}
			bWriter.newLine();
		}
		bWriter.newLine();
		bWriter.newLine();
		
		bWriter.write("\t\t\tpublic static initMap():void{");
		bWriter.newLine();
		for(Paragraph info : paragraphs){
//			long protocolId = info.getProtocolStartID();
			for(ProtocolInfo protocol : info.getProtocols())
			{
				for(int i = 0 ; i < protocol.getResultNames().size(); i++)
				{
					Result result = protocol.getResultNames().get(i);
					bWriter.newLine();
					if(!result.isSuccess())
					{
						bWriter.write("\t\t\t\tPlatformErrorCode.codeMap.put(PlatformErrorCode."+result.getName()+", \""+protocol.getDescNames().get(i)+"\");");
					}
				}
//				++protocolId;
			}
			
			bWriter.newLine();	
		}
		bWriter.write("\t\t}");
		bWriter.newLine();
		bWriter.write("\t}");
		bWriter.newLine();
		bWriter.write("}");
		
		bWriter.close();
	}
}
