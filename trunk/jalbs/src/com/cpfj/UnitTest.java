/*
 * Copyright 2010, Sumeet Chhetri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
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
