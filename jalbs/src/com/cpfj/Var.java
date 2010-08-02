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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unchecked")
public class Var
{
	private String name;
	private String type;
	private String visiblity;
	private String level;
	private String value;
	private int arrEle;
	private Object valObj; 
	private Map array;
	public Var()
	{
		
	}
	
	public static Var getCopy(Var var)
	{
		Var var1 = new Var();
		var1.name = var.name;
		var1.type = var.type;
		var1.visiblity = var.visiblity;
		var1.level = var.level;
		if(var.value!=null)
		var1.value = new String(var.value);
		var1.arrEle = var.arrEle;
		if(var.valObj!=null)
			var1.valObj.procm = var.valObj.procm;
		var1.array = new HashMap();
		if(var.array!=null)
		{
			for (Iterator iter = var.array.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry entry = (Map.Entry)iter.next();
				var1.array.put(entry.getKey(), entry.getValue());
			}	
		}
		return var1;
	}
	public boolean isDefinedObject()
	{
		return this.type.equals("boolean") || this.type.equals("number") || this.type.equals("string")
				|| this.type.indexOf("bounded-")!=-1 ;
	}
	
	@SuppressWarnings("unchecked")
	public static void initializeVals(Var var,Map<String,Object> objs)
	{
		if(var.type.equals("boolean") && (var.getValue()==null || var.getValue().equals("")))
			var.value = "false";
		else if(var.type.equals("number") && (var.getValue()==null || var.getValue().equals("")))
			var.value = "0";
		else if(var.type.equals("bounded-string-array") || var.type.equals("unbounded-string-array"))
		{
			var.array = new HashMap();
			var.value = "<arr type=\"string\"/>";
		}
		else if(var.type.equals("bounded-number-array") || var.type.equals("unbounded-number-array"))
		{
			var.array = new HashMap();
			var.value = "<arr type=\"number\"/>";
		}
		else if(var.type.equals("bounded-boolean-array") || var.type.equals("unbounded-boolean-array"))
		{
			var.array = new HashMap();
			var.value = "<arr type=\"boolean\"/>";
		}
		else if(var.type.equals("file"))
		{
			var.value = "FILE";
		}
		else if(var.type.equals("socket"))
		{
			var.value = "SOCKET";
		}
		else if(var.valObj!=null)
		{
			for (Iterator iter = var.valObj.getAllVars().entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,Var> entr = (Map.Entry<String,Var>)iter.next();
				entr.getValue().valObj = objs.get(entr.getValue().type);
				if(entr.getValue().valObj!=null)
					entr.getValue().value = entr.getValue().valObj.string();
				initializeVals(entr.getValue(),objs);
			}
		}
	}
	public String getValue()
	{
		if(valObj!=null)
			return valObj.string();
		else if(array!=null && !type.equals("socket") && !type.equals("file"))
			return toArray();
		else
			return value;
	}
	private String toArray()
	{
		if(value.equals("FILE") || value.equals("SOCKET"))
			return "";
		String arr = "<arr type=\""+type+"\"><entries>";
		for (Iterator iter = array.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry entry = (Map.Entry)iter.next();
			arr += "<entry key=\""+(String)entry.getKey()+"\"><value>"+(String)entry.getValue()+"</value></entry>";
		}
		arr += "</entries></arr>";
		return arr;
	}
	public void setValue(String val,ProcessM proc)
	{
		if(this.type.equals("boolean") && val!=null)
		{
			if(val.equalsIgnoreCase("false") || val.equalsIgnoreCase("true"))
				this.value = val;
			else
			{/*Check invalid boolean value*/System.out.println("Invalid assignment for variable "+this.name);proc.setShouldProcRun(false);}	
		}
		else if(this.type.equals("number") && val!=null)
		{
			try{Long.valueOf(val);this.value = val;}
			catch(Exception e){/*Check invalid numerals*/System.out.println("Invalid assignment for variable "+this.name);proc.setShouldProcRun(false);}			
		}
		else
			value = val;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public String getType()
	{
		return type;
	}
	public void setType(String type)
	{
		this.type = type;
	}
	public Object getValObj()
	{
		return valObj;
	}
	public void setValObj(Object valObj)
	{
		this.valObj = valObj;
	}
	public String getLevel()
	{
		return level;
	}
	public void setLevel(String level)
	{
		this.level = level;
	}
	public String getVisiblity()
	{
		return visiblity;
	}
	public void setVisiblity(String visiblity)
	{
		this.visiblity = visiblity;
	}
	public int getArrEle()
	{
		return arrEle;
	}
	public void setArrEle(int arrEle)
	{
		this.arrEle = arrEle;
	}
	public Map getArray()
	{
		return array;
	}
	public void setArray(Map array)
	{
		this.array = array;
	}
	
	public void addVarToArr(String key,String val,ProcessM procm)
	{
		if(this.type.equals("bounded-number-array") || this.type.equals("unbounded-number-array"))
		{
			try{Long.valueOf(val);array.put(key,val);}
			catch(Exception e){/*Check invalid  numerals*/System.out.println("Invalid assignment for variable "+this.name+" and key "+key);procm.setShouldProcRun(false);}
		}
		else if(this.type.equals("bounded-boolean-array") || this.type.equals("unbounded-boolean-array"))
		{
			if(val.equalsIgnoreCase("false") || val.equalsIgnoreCase("true"))
				array.put(key,val);
		}
		else
			array.put(key,val);
	}
	public String getValFromArr(String key)
	{
		return (String)array.get(key);
	}
}
