package com.cpfj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class UnitTest
{

	/**
	 * @param args
	 * @throws IOException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException
	{
		newProcess("/com/cpfj/testsock.jbsl");
		//newProcess("/com/cpfj/test1.jbsl");
		/*newProcess("/com/cpfj/test2.jbsl");
		newProcess("/com/cpfj/test3.jbsl");
		newProcess("/com/cpfj/testsock.jbsl");*/
	}
	
	private static void newProcess(String path) throws IOException
	{
		BufferedReader buffr = new BufferedReader(new InputStreamReader(UnitTest.class.getResourceAsStream(path)));
		StringBuffer buf = new StringBuffer();
		String temp = null;
		while((temp=buffr.readLine())!=null)
		{
			buf.append(temp+"\n");
		}
		String cont = buf.toString();
		JBSLProcessor.get().newProcess(new ProcessM(cont));
		buffr.close();
	}

}
