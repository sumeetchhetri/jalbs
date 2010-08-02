package com.cpfj;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;


@SuppressWarnings("unchecked")
public class Object
{
	private String name;
	private Map<String,Var> privVars,pubVars;
	private Map<String,Function> privFuncs,pubFuncs;
	ProcessM procm = null;
	public Object(ProcessM proc)
	{
		privVars = new HashMap<String, Var>();
		pubVars = new HashMap<String, Var>();
		privFuncs = new HashMap<String, Function>();
		pubFuncs = new HashMap<String, Function>();
		procm = new ProcessM("Object::",true);
		procm.setObjs(proc.getObjs());
	}
	public String getVarVal(String name)
	{
		return procm.getLocalVars().get(name).getValue();
	}
	public Var getVar(String name)
	{
		return procm.getLocalVars().get(name);
	}
	public Map<String, Var> getAllVars()
	{
		return procm.getLocalVars();
	}
	public void init(String args,Map<String,Object> objs)
	{
		if(this.procm.getFuncs().get("_init_")!=null)
		{
			this.procm.getFuncs().get("_init_").getCopy().executeFunc(args,procm);
		}
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
		procm.setName("Object::"+name);
	}
	public void addVar(boolean priv,Var var,Map<String,Object> objs)// throws Exception
	{
		if(priv && pubVars.get(var.getName())==null && privVars.get(var.getName())==null)
		{
			privVars.put(var.getName(), var);
			procm.getLocalVars().put(var.getName(), var);
		}
		else if(pubVars.get(var.getName())==null && privVars.get(var.getName())==null)
		{
			pubVars.put(var.getName(), var);
			procm.getLocalVars().put(var.getName(), var);
		}
		if(procm.getLocalVars().get(var.getName())!=null)
		{
			Var.initializeVals(var,objs);
		}
		//else
		//	throw new Exception("Variable with same name already exists");
	}
	public void addFunc(boolean priv,Function func)/// throws Exception
	{
		if(priv && privFuncs.get(func.getName())==null && pubFuncs.get(func.getName())==null)
		{
			privFuncs.put(func.getName(), func);
			procm.getFuncs().put(func.getName(), func);
		}
		else if(pubFuncs.get(func.getName())==null && privFuncs.get(func.getName())==null)
		{
			pubFuncs.put(func.getName(), func);
			procm.getFuncs().put(func.getName(), func);
		}			
		//else
		//	throw new Exception("Function with same name already exists");
	}
	public void addVarVal(boolean priv,String name,String val,ProcessM procm)
	{
		if(pubVars.get(name)!=null)
		{
			pubVars.get(name).setValue(val,procm);				
		}
		else if(privVars.get(name)!=null)
		{
			privVars.get(name).setValue(val,procm);
		}
	}
	public static String executeFunc(Var var,String name,String args,ProcessM procm,Map<String,Var> localVars)
	{	
		String retVal = "";
		if(var.getType().equals("string") || var.getType().equals("runtime"))
		{
			int argn = 0;
			if(args.indexOf(",")!=-1)
			{
				argn = args.split(",").length;
			}
			else if(!args.equals(""))
			{
				argn = 1;
			}
			if(name.equals("split") && argn==1)
			{
				
			}
			return retVal;
		}
		else if(var.getType().equals("file"))
		{
			int argn = 0;
			if(args.indexOf(",")!=-1)
			{
				argn = args.split(",").length;
			}
			else if(!args.equals(""))
			{
				argn = 1;
			}
			if(name.equals("open") && argn==1)
			{
				if(new File(args.split(",")[0]).exists())
				{
					var.setArray(new HashMap());
					var.getArray().put("FILE",args.split(",")[0]);
				}
				else
					System.out.println("Error opening file");
			}
			else if(name.equals("write") && argn==1)
			{
				if(var.getArray().get("FILE")!=null) 
				{
					try
					{
						new BufferedWriter(new FileWriter((String)var.getArray().get("FILE"))).write(args.split(",")[0]);
					}
					catch (IOException e)
					{
						System.out.println("Error writing to File");
					}
				}
				else
					System.out.println("File not open");
			}
			else if(name.equals("read") && argn==0)
			{
				if(var.getArray().get("FILE")!=null) 
				{
					try
					{
						BufferedReader buffr = new BufferedReader(new FileReader((String)var.getArray().get("FILE")));
						String data = null,temp="";
						while((data=buffr.readLine())!=null)
						{
							temp += data+"\n";
						}
						buffr.close();
						return temp;
					}
					catch (IOException e)
					{
						System.out.println("Error writing to File");
					}
				}
				else
					System.out.println("File not open");
			}
			return retVal;
		}
		else if(var.getType().equals("socket"))
		{
			int argn = 0;
			if(args.indexOf(",")!=-1)
			{
				argn = args.split(",").length;
			}
			else if(!args.equals(""))
			{
				argn = 1;
			}
			if(name.equals("listen") && argn==1)
			{
				try
				{
					ServerSocket sock = new ServerSocket(Integer.valueOf(args));
					var.setArray(new HashMap());
					var.getArray().put("SERVER-SOCKET",sock);
				}
				catch(Exception e)
				{
					System.out.println("Error opening socket");
					return "";
				}
				retVal = "SERVER-SOCKET";
			}
			else if(name.equals("connect") && argn==1)
			{
				try
				{
					Socket sock = new Socket(args.split(",")[0],Integer.valueOf(args.split(",")[1]));
					var.setArray(new HashMap());
					var.getArray().put("CONNEC-SOCKET",sock);
				}
				catch(Exception e)
				{
					System.out.println("Error connecting to remote host/port"+args.split(",")[0]+","+args.split(",")[1]);
					return "";
				}
				retVal = "";
			}
			else if(name.equals("accept") && argn==1 && var.getArray().get("SERVER-SOCKET")!=null)
			{
				try
				{
					ServerSocket sock = (ServerSocket)var.getArray().get("SERVER-SOCKET");
					Socket soc = sock.accept();
					var.getArray().put("ACCEP-SOCKET"+(var.getArray().size()+1)+":"+var.getName(),soc);
				}
				catch(Exception e)
				{
					System.out.println("Error accepting socket");
					procm.setShouldProcRun(false);
					return "";
				}
				retVal = "ACCEP-SOCKET"+var.getArray().size()+":"+var.getName();
			}
			else if(name.equals("write") && argn==1)
			{
				Var var1 = localVars.get(var.getLevel());
				if(var.getName().equals(var1.getName()))
				{
					if(localVars.get("func-ref::"+var.getLevel())!=null)
						var1 = localVars.get("func-ref::"+var.getLevel());
				}
				if(var1.getArray().get(var.getValue())!=null) 
				{
					try
					{
						Socket soc = (Socket)var1.getArray().get(var.getValue());
						new DataOutputStream(soc.getOutputStream()).writeUTF(args);
					}
					catch (Exception e)
					{
						System.out.println("Error writing to socket");
						procm.setShouldProcRun(false);
					}
				}
				else
				{
					System.out.println("Could not write to Socket");
					System.exit(0);
				}
				retVal = "";
			}
			else if(name.equals("read") && argn==0)
			{
				Var var1 = localVars.get(var.getLevel());
				if(var.getName().equals(var1.getName()))
				{
					if(localVars.get("func-ref::"+var.getLevel())!=null)
						var1 = localVars.get("func-ref::"+var.getLevel());
				}
				if(var1.getArray().get(var.getValue())!=null) 
				{
					try
					{
						Socket soc = (Socket)var1.getArray().get(var.getValue());
						BufferedReader in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
						String str = null;
						retVal="";int cnt = 0;						 
						if((str = in.readLine()) != null) 
						{
							retVal = str;cnt++;
						}	
						if(cnt==0 && str==null)
							throw new Exception("");
					}
					catch (Exception e)
					{
						retVal="";
						System.out.println("Error reading from socket");
						procm.setShouldProcRun(false);
					}
				}
				else
				{
					System.out.println("Could not write to Socket");
					System.exit(0);
				}
			}
			return retVal;
		}
		else
			return procm.getObjs().get(var.getType()).procm.getFuncs().get(name).getCopy().executeFunc(args,procm);		
	}
	
	public String string()
	{
		String str = "<obj name=\""+name+"\"><props>";
		for (Iterator iter = pubVars.entrySet().iterator(); iter.hasNext();)
		{
			Map.Entry<String,Var> entr = (Map.Entry<String,Var>)iter.next();
			String val = "";
			if(entr.getValue().getValue()!=null)
			{
				val = entr.getValue().getValue().replaceAll("\\<", "&lt;");
				val = val.replaceAll("\\>", "&gt;");
			}
			str += "<prop name=\"" + entr.getKey() + "\" type=\""+entr.getValue().getType()+"\"><value>" + val + "</value></prop>";
		}
		str += "</props></obj>";;
		return str;
	}
	
	public static Map evaluateArray(String xml)
	{
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("arr", arr.class);
		xstream.alias("entry", entry.class);
		xstream.alias("entries", entry[].class);
		xstream.processAnnotations(arr.class);
		xstream.processAnnotations(entry.class);
		arr arr = (arr)xstream.fromXML(xml);
		Map array = new HashMap();
		if(arr!=null && arr.entries!=null && arr.entries.length>0)
		{
			for (int i = 0; i < arr.entries.length; i++)
			{
				array.put(arr.entries[i].key, arr.entries[i].value);
			}
		}
		return array;
	}
	public static Object evaluateObject(String xml,ProcessM proc)
	{
		XStream xstream = new XStream(new DomDriver());
		xstream.alias("obj", obj.class);
		xstream.alias("prop", prop.class);
		xstream.alias("props", prop[].class);
		xstream.processAnnotations(obj.class);
		xstream.processAnnotations(prop.class);
		obj obj = (obj)xstream.fromXML(xml);
		Object object = null;
		if(obj!=null)
		{
			object = new Object(proc);
			object.setName(obj.name);
			for (int i = 0; i < obj.props.length; i++)
			{
				Var var = new Var();
				var.setName(obj.props[i].name);
				var.setType(obj.props[i].type);
				if(var.getType().indexOf("bounded-string-array")!=-1 
						|| var.getType().indexOf("bounded-boolean-array")!=-1 
						|| var.getType().indexOf("bounded-number-array")!=-1)
				{
					var.setArray(evaluateArray(obj.props[i].value));
				}
				else if(var.getType().equals("string") || var.getType().equals("number") 
						|| var.getType().equals("boolean"))
				{
					var.setValue(obj.props[i].value,proc);
				}
				else
				{
					var.setValObj(evaluateObject(obj.props[i].value,proc));					
				}
				object.addVar(false, var , proc.getObjs());
			}
		}
		System.out.println(obj);
		return object;
	}
}
