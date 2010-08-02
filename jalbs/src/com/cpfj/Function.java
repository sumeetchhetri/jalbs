package com.cpfj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.cpfj.JBSLProcessor.Message;
@SuppressWarnings("unchecked")
public class Function implements Cloneable
{
	private Map<String,Var> localVars;
	private Map<String,String> argVars;
	private Map<Integer,String> argscnt;
	private int currentCounter = 0;
	private String name = null;
	private int ident = 0;
	private List<String> instrucs = null;
	private String[] strs = null;
	public Function()
	{
		
	}
	public Function getCopy()
	{
		Function funsc = new Function();
		funsc.setIdent(this.ident);
		funsc.setName(this.name);
		funsc.currentCounter = 0;
		funsc.strs = this.strs;
		funsc.argVars = new HashMap<String,String>();
		funsc.localVars = new HashMap<String,Var>();
		funsc.argscnt = new HashMap<Integer,String>();
		if(this.localVars!=null)
		{
			for (Iterator iter = this.localVars.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,Var> entry = (Map.Entry<String,Var>)iter.next();
				funsc.localVars.put(entry.getKey(), entry.getValue());
			}	
		}
		if(this.argVars!=null)
		{
			for (Iterator iter = this.argVars.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,String> entry = (Map.Entry<String,String>)iter.next();
				funsc.argVars.put(entry.getKey(), entry.getValue());
			}	
		}
		if(this.argscnt!=null)
		{
			for (Iterator iter = this.argscnt.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<Integer,String> entry = (Map.Entry<Integer,String>)iter.next();
				funsc.argscnt.put(entry.getKey(), entry.getValue());
			}	
		}
		return funsc;
	}
	public Function(String allda)
	{
		String[] dat = allda.split(",");
		instrucs = new ArrayList<String>();
		argVars = new HashMap<String,String>();
		localVars = new HashMap<String,Var>();
		argscnt = new HashMap<Integer,String>();
		int yy = 0;
		for (int i=0;i<dat.length;i++)
		{
			if(i==0)name = dat[i].trim();
			else if(!"".equals(dat[i].trim()))
			{
				argVars.put(dat[i].trim(), "");
				argscnt.put(yy++, dat[i].trim());
				ident += 1;
			}
		}
	}
	public void addInstructions(String instruction)
	{
		instrucs.add(instruction);
	}
	public void done()
	{
		strs = instrucs.toArray(new String[instrucs.size()]);
		instrucs = null;
	}
	
	public String executeFunc(String args,ProcessM procm,ProcThr procthr)
	{
		String retVal = "";
		localVars = new HashMap<String,Var>();
		if(procthr.getLocalVars()!=null)
		{
			for (Iterator iter = procthr.getLocalVars().entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,Var> entry = (Map.Entry<String,Var>)iter.next();
				localVars.put(entry.getKey(), entry.getValue());
			}	
		}
		argVars = new HashMap<String,String>();
		if(!args.trim().equals(""))
		{
			String[] argus = args.split(",");
			for (int o=0;o<argus.length;o++)
			{
				if((argus[o].trim().charAt(0)=='"' ||argus[o].trim().charAt(0)=='\'') && 
						(argus[o].trim().charAt(argus[o].trim().length()-1)=='"' 
							|| argus[o].trim().charAt(argus[o].trim().length()-1)=='\''))
					argus[o] = argus[o].trim().substring(1,argus[o].trim().length()-1);
				if(procthr.getLocalVars().get(argus[o])!=null)
				{
					if(localVars.get(argscnt.get(o))!=null)
						localVars.put("func-ref::"+argscnt.get(o), localVars.get(argscnt.get(o)));
					{
						Var var1 = new Var();
						var1.setName(procthr.getLocalVars().get(argus[o]).getName());
						var1.setType(procthr.getLocalVars().get(argus[o]).getType());
						var1.setValue(procthr.getLocalVars().get(argus[o]).getValue(),procm);
						var1.setLevel(procthr.getLocalVars().get(argus[o]).getLevel());						
						localVars.put(argscnt.get(o),var1);
					}
					
				}
				else
					argVars.put(argscnt.get(o),argus[o]);
			}
		}
		procm.setLocalVars(procthr.getLocalVars());
		while(currentCounter<strs.length && procthr.isShouldRun() && procm.isShouldProcRun())
		{
			 retVal = executeNextLine(procm);
		}
		currentCounter = 0;
		return retVal;
	}
	
	public String executeFunc(String args,ProcessM procm)
	{
		String retVal = "";
		localVars = new HashMap<String,Var>();
		argVars = new HashMap<String,String>();
		if(!args.trim().equals(""))
		{
			String[] argus = evaluate(args,procm).split(",");
			for (int o=0;o<argus.length;o++)
			{
				if((argus[o].trim().charAt(0)=='"' ||argus[o].trim().charAt(0)=='\'') && 
						(argus[o].trim().charAt(argus[o].trim().length()-1)=='"' 
							|| argus[o].trim().charAt(argus[o].trim().length()-1)=='\''))
					argus[o] = argus[o].trim().substring(1,argus[o].trim().length()-1);
				argVars.put(argscnt.get(o),argus[o]);
			}
		}
		while(currentCounter<strs.length && procm.isShouldProcRun())
			retVal = executeNextLine(procm);
		currentCounter = 0;
		return retVal;
	}
	public boolean checkFuncDone()
	{
		return currentCounter>=strs.length;
	}
	public String executeNextLine(ProcessM procm)
	{
		 return execute(strs[currentCounter++],procm);
	}
	private String execute(String string,ProcessM procm)
	{
		if(procm.isShouldProcRun())
			return evaluate(string,procm);
		else return "";
	}
	private String evaluate(String string,ProcessM procm)
	{
		String retVal = string.trim();	
		boolean forwhif = false;
		while(((string.indexOf("=")==-1 || (string.indexOf("=")!=-1 
				&& ((string.indexOf("\"")<string.indexOf("=") && string.indexOf("\"")!=-1) 
				|| (string.indexOf("'")<string.indexOf("=") && string.indexOf("'")!=-1)))) || string.indexOf("==")!=-1) && string.indexOf("(")!=-1 && string.indexOf(")")!=-1 
				|| (string.indexOf("=")!=-1 && string.indexOf("for(")!=-1 && string.indexOf(")")!=-1
						&& string.indexOf(";")!=-1))
		{				
			String funcn = string.substring(0,string.indexOf("(")).trim();
			String inter = "";
			if(string.lastIndexOf(")") != string.indexOf("(")+1)
				inter = string.substring(string.indexOf("(")+1,string.lastIndexOf(")")).trim();
			if(JBSLProcessor.isDebug())
			{
				System.out.println("--"+string);
				System.out.println("----"+funcn+" "+inter);
			}
			if(funcn.equalsIgnoreCase("out"))
			{
				if(inter.indexOf(".")!=-1 && inter.indexOf("\"")==-1 && inter.indexOf("'")==-1)
				{	
					Var var = null;
					String temp = inter;
					String varnam = "";
					while(temp.indexOf(".")!=-1 && temp.indexOf("\"")==-1 && temp.indexOf("'")==-1)
					{
						if(var!=null && var.getValObj()!=null && var.getValObj().getVar(temp.split("\\.")[0])!=null)
							var = var.getValObj().getVar(temp.split("\\.")[0]);
						else if(localVars.get(temp.split("\\.")[0])!=null)
							var = localVars.get(temp.split("\\.")[0]);
						else if(procm.getLocalVars().get(temp.split("\\.")[0])!=null)
							var = procm.getLocalVars().get(temp.split("\\.")[0]);						
						if(temp.split("\\.").length>2)
							temp = temp.substring(temp.indexOf(".")+1);
						else
						{
							varnam = temp.split("\\.")[1];
							temp = "";
						}
					}
					String key = varnam;
					if(varnam.indexOf("[")!=-1 && varnam.indexOf("]")!=-1)
					{
						varnam = key.substring(0,varnam.indexOf("["));
						key = key.substring(key.indexOf("[")+1);
						key = key.replaceAll("\\]", "");
						retVal = var.getValObj().getVar(varnam).getValFromArr(key);
					}
					else
						retVal = var.getValObj().getVarVal(varnam);
				}
				else
				{
					retVal = evaluate(inter,procm);
					if(localVars.get(retVal)!=null)
						retVal = localVars.get(retVal).getValue();
					else if(argVars.get(retVal)!=null)
						retVal = argVars.get(retVal);
					else if(procm.getLocalVars().get(retVal)!=null)
						retVal = procm.getLocalVars().get(retVal).getValue();
					if(retVal.indexOf("\"")!=-1)
						retVal = retVal.replaceAll("\"","");
					else if(retVal.indexOf("'")!=-1)
						retVal = retVal.replaceAll("'","");
				}
				System.out.println(retVal);
				retVal = "";
			}
			else if(funcn.equalsIgnoreCase("sleep"))
			{
				try
				{
					String argu = evaluate(inter,procm);
					Thread.sleep(Long.parseLong(argu));
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				retVal = "";
			}
			else if(funcn.equalsIgnoreCase("sendMsg"))
			{	
				String[] args = inter.split(",");
				String argu = evaluate(args[0],procm);
				if(args.length==3 && JBSLProcessor.processes.get(new Long(argu))!=null)
				{
					if(args[1].equalsIgnoreCase("KILL"))
					{
						if(!JBSLProcessor.processes.get(new Long(argu)).isShouldProcRun())
						{
							System.out.println("Process with pid "+argu+" already killed,Kill now requested by process with pid "+procm.getId());
						}
						else
						{
							JBSLProcessor.processes.get(new Long(argu)).setShouldProcRun(false);
							System.out.println("Process with pid "+argu+" killed, Killed by process with pid "+procm.getId());
						}
						System.out.println(args[2]);
					}
					else
					{
						Message message = new Message();
						message.setFrmid(procm.getId());
						message.setType(evaluate(args[1],procm));
						message.setMessage(evaluate(args[2],procm));
						JBSLProcessor.processes.get(new Long(argu)).addMesasge(message);
					}
				}
				retVal = "";
			}
			else if(funcn.equalsIgnoreCase("recvMsg"))
			{					
				Message message = JBSLProcessor.processes.get(procm.getId()).getMesasge();
				if(message!=null)
				{
					if(message.getType().equalsIgnoreCase("KILL"))
					{
						procm.setShouldProcRun(false);
						System.out.println("Process with pid "+procm.getId()+" killed, Killed by process with pid "+message.getFrmid());
						System.out.println(message.getMessage().toString());
					}
					else
						retVal = message.getMessage().toString();
				}
				else
					retVal = "";
			}
			else if(funcn.equalsIgnoreCase("for"))
			{
				evaluateLoops(inter,"FOR",procm);
				retVal = "";
				forwhif = true;
			}
			else if(funcn.equalsIgnoreCase("while"))
			{
				evaluateLoops(inter,"WHILE",procm);
				retVal = "";
				forwhif = true;
			}
			else if(funcn.equalsIgnoreCase("if") || funcn.equalsIgnoreCase("elseif"))
			{
				evaluateLoops(inter,"IF",procm);
				retVal = "";
				forwhif = true;
			}
			else if(funcn.equalsIgnoreCase("else"))
			{
				evaluateLoops(inter,"ELSE",procm);
				retVal = "";
				forwhif = true;
			}
			else if(funcn.equalsIgnoreCase("spawnProcess"))
			{
				String[] args = inter.split(",");
				if(args.length==1)
				{					
					if(args[0]!=null && !args[0].equals("") && (args[0].charAt(0)=='"' || args[0].charAt(0)=='\'')
							&& (args[0].charAt(args[0].length()-1)=='"' || args[0].charAt(args[0].length()-1)=='\''))
					{
						String data = args[0].substring(1,args[0].length()-1);
						retVal = String.valueOf(JBSLProcessor.get().newProcess(new ProcessM(data)));
					}
					else if(args[0]!=null && !args[0].equals("") && (args[0].charAt(0)!='"' && args[0].charAt(0)!='\'')
							&& (args[0].charAt(args[0].length()-1)!='"' && args[0].charAt(args[0].length()-1)!='\''))
					{
						String argu = evaluate(args[0],procm);
						retVal = String.valueOf(JBSLProcessor.get().newProcess(new ProcessM(argu)));
					}
					else
						retVal = "";
				}
				else if(args.length==2)
				{
					if(args[0]!=null && !args[0].equals("") && (args[0].charAt(0)=='"' || args[0].charAt(0)=='\'')
							&& (args[0].charAt(args[0].length()-1)=='"' || args[0].charAt(args[0].length()-1)=='\''))
					{
						String data = args[0].substring(1,args[0].length()-1);
						retVal = String.valueOf(JBSLProcessor.get().newProcess(new ProcessM(data,1)));
					}
					else if(args[0]!=null && !args[0].equals("") && (args[0].charAt(0)!='"' && args[0].charAt(0)!='\'')
							&& (args[0].charAt(args[0].length()-1)!='"' && args[0].charAt(args[0].length()-1)!='\''))
					{
						String argu = evaluate(args[0],procm);
						retVal = String.valueOf(JBSLProcessor.get().newProcess(new ProcessM(argu)));
					}
					else
						retVal = "";					
				}
				forwhif = true;
			}
			else if(funcn.equalsIgnoreCase("eval"))
			{
				String[] args = inter.split(",");
				if(args.length==1)
				{
					String argu = args[0];
					if((argu.charAt(0)=='"' || argu.charAt(0)=='\'') 
							&& (argu.charAt(argu.length()-1)=='"' || argu.charAt(argu.length()-1)=='\''))
					{}
					else
						argu = evaluate(args[0],procm);
					JBSLProcessor.evaluate(args[0]);
				}
				retVal = "";
				forwhif = true;
			}
			else
			{
				forwhif = true;
				int args = 0;
				if(inter.indexOf(",")!=-1)
				{
					args = inter.split(",").length;
				}
				else if(!inter.equals(""))
				{
					args = 1;
				}
				if(args>0)
				{
					string = evaluate(inter,procm);
				}
				else
					string = inter;
				if(funcn.indexOf(".")!=-1)
				{
					Var var = null;
					String temp = funcn;
					String varnam = "";
					while(temp.indexOf(".")!=-1 && temp.indexOf("\"")==-1 && temp.indexOf("'")==-1)
					{
						if(var!=null && var.getValObj()!=null && var.getValObj().getVar(temp.split("\\.")[0])!=null)
							var = var.getValObj().getVar(temp.split("\\.")[0]);
						else if(localVars.get(temp.split("\\.")[0])!=null)
							var = localVars.get(temp.split("\\.")[0]);
						else if(procm.getLocalVars().get(temp.split("\\.")[0])!=null)
							var = procm.getLocalVars().get(temp.split("\\.")[0]);
						if(temp.split("\\.").length>2)
							temp = temp.substring(temp.indexOf(".")+1);
						else
						{
							varnam = temp.split("\\.")[1];
							temp = "";
						}
					}
					if(var!=null)
					{
						//procm.getObjs().get(var.getType())						
						retVal = Object.executeFunc(var,varnam,string,procm,localVars);
						string = "";
					}
				}
				else
				{
					if(funcn.indexOf("thread::")!=-1)
					{
						string = inter;
						procm.getThreads().add(executeThread(funcn+args,string,procm,localVars));
						retVal = "";
					}
					else
					{
						retVal = procm.getFuncs().get(funcn+args).getCopy().executeFunc(string,procm);
					}
				}
			}
			string = string.replace(funcn+"("+inter+")", "");
		}
		if(string.indexOf("=")!=-1 || (string.indexOf(" is boolean")!=-1 
				|| string.indexOf(" is string")!=-1 || string.indexOf(" is number")!=-1
				|| string.indexOf(" is number")!=-1 || string.indexOf(" is bounded-string-array")!=-1
				|| string.indexOf(" is bounded-boolean-array")!=-1 || string.indexOf(" is bounded-number-array")!=-1
				|| string.indexOf(" is unbounded-string-array")!=-1
				|| string.indexOf(" is unbounded-boolean-array")!=-1 || string.indexOf(" is unbounded-number-array")!=-1))
		{
			//string = string.replaceAll(" ", "");
			if((string.indexOf("=")!=-1 && string.indexOf(" new ")!=-1 
					&& (string.indexOf("\"")>string.indexOf("=") || string.indexOf("\"")==-1) 
					&& (string.indexOf("'")>string.indexOf("=") || string.indexOf("'")==-1)) || string.indexOf(" is ")!=-1 )
			{
				String[] strs = null;
				String strss = null;
				Var var = new Var();
				if(string.indexOf(" new ")!=-1)
				{	
					strs = string.split("=");							
					strss = strs[1].replaceFirst(" new ","");
					var.setType(strss.split("\\(")[0].trim());
					strss = strss.replaceFirst(var.getType()+"\\(", "");
					strss = strss.replaceFirst("\\)", "");
				}
				else
				{	
					strs = string.split(" is ");
					strss = strs[1].trim();
					var.setType(strss);
				}			
				var.setName(strs[0].trim());			
				var.setVisiblity("function");
				var.setLevel("default");
				if(!var.getType().equals("file") && !var.getType().equals("socket") && !var.isDefinedObject())
				{
					var.setValObj(procm.getObjs().get(var.getType()));
					Var.initializeVals(var, procm.getObjs());
					var.getValObj().init(strss, procm.getObjs());
				}
				localVars.put(var.getName(), var);
				return "";
			}
			String[] strs = string.split("=");
			String val = strs[1].trim();
			if(strs[1].trim().indexOf("+")!=-1 || strs[1].trim().indexOf("-")!=-1 
					|| strs[1].trim().indexOf("/")!=-1 || strs[1].trim().indexOf("*")!=-1)
			{
				val = evalBODMAS(strs[1].trim(),procm);
			}
			else if(string.indexOf("(")!=-1 && string.indexOf(")")!=-1 )
			{
				val = evaluate(val, procm);				
			}
			else
				val = getVariable(val, procm);
			if(val!=null && val.length()>0 && (val.trim().charAt(0)=='"' || val.trim().charAt(0)=='\'') && 
					(val.trim().charAt(val.trim().length()-1)=='"' 
						|| val.trim().charAt(val.trim().length()-1)=='\''))
				val = val.trim().substring(1,strs[1].trim().length()-1);
			else if(val!=null && val.length()>0 && (val.trim().equalsIgnoreCase("true")||val.trim().equalsIgnoreCase("false")))
			{
				val = val.trim();
			}
			if(strs[0].trim().indexOf(".")!=-1)
			{
				Var var = null;
				String varnam = "";
				String temp = strs[0].trim();
				while(temp.indexOf(".")!=-1 && temp.indexOf("\"")==-1 && temp.indexOf("'")==-1)
				{
					if(var!=null && var.getValObj()!=null && var.getValObj().getVar(temp.split("\\.")[0])!=null)
						var = var.getValObj().getVar(temp.split("\\.")[0]);
					else if(localVars.get(temp.split("\\.")[0])!=null)
						var = localVars.get(temp.split("\\.")[0]);
					else if(procm.getLocalVars().get(temp.split("\\.")[0])!=null)
						var = procm.getLocalVars().get(temp.split("\\.")[0]);
					if(temp.split("\\.").length>2)
						temp = temp.substring(temp.indexOf(".")+1);
					else
					{
						varnam = temp.split("\\.")[1];
						temp = "";
					}
				}
				if(var!=null)
				{
					String key = varnam;
					if(varnam.indexOf("[")!=-1 && varnam.indexOf("]")!=-1)
					{
						varnam = key.substring(0,varnam.indexOf("["));
						key = key.substring(key.indexOf("[")+1);
						key = key.replaceAll("\\]", "");
						var.getValObj().getVar(varnam).addVarToArr(key, val, procm);
					}
					else
						var.getValObj().addVarVal(false, key, val, procm);
				}
			}
			else
			{
				Var var = new Var();
				var.setName(strs[0].trim());
				boolean obj = false;
				if(localVars.get(val)!=null)
				{
					val = localVars.get(val).getValue();
					obj = true; 
				}
				else if(argVars.get(val)!=null)
				{
					val = argVars.get(val);
					obj = true; 
				}
				else if(procm.getLocalVars().get(val)!=null)
				{
					val = procm.getLocalVars().get(val).getValue();
					obj = true; 
				}
				if(obj)
				{
					Object objec = Object.evaluateObject(val, procm);
					var.setValObj(objec);
				}
				
				var.setLevel("default");
				if(val.indexOf("ACCEP-SOCKET")!=-1 || val.equals("CONNEC-SOCKET")
						|| val.equals("SERVER-SOCKET"))
				{
					var.setType("socket");
					var.setLevel(val.split(":")[1]);
				}
				else
					var.setType("runtime");
				var.setVisiblity("function");
				var.setValue(val,procm);
				if(argVars.get(strs[0].trim())==null && procm.getLocalVars().get(strs[0].trim())==null 
						&& localVars.get(strs[0].trim())==null)
					localVars.put(var.getName(),var);
				else if(localVars.get(strs[0].trim())!=null)
					localVars.get(strs[0].trim()).setValue(val,procm);
				else if(argVars.get(strs[0].trim())!=null)
					argVars.put(strs[0].trim(),val);
				else if(procm.getLocalVars().get(strs[0].trim())!=null)
					procm.getLocalVars().get(strs[0].trim()).setValue(val,procm);
			}
		}
		else if(string.indexOf("return ")!=-1)
		{
			String varn = string.replaceFirst("return ", "").trim();
			if(varn.indexOf("\\.")!=-1)
			{
				Var var = null;
				String temp = varn;
				String varnam = "";
				while(temp.indexOf(".")!=-1 && temp.indexOf("\"")==-1 && temp.indexOf("'")==-1)
				{
					if(var!=null && var.getValObj()!=null && var.getValObj().getVar(temp.split("\\.")[0])!=null)
						var = var.getValObj().getVar(temp.split("\\.")[0]);
					else if(localVars.get(temp.split("\\.")[0])!=null)
						var = localVars.get(temp.split("\\.")[0]);
					else if(procm.getLocalVars().get(temp.split("\\.")[0])!=null)
						var = procm.getLocalVars().get(temp.split("\\.")[0]);
					if(temp.split("\\.").length>2)
						temp = temp.substring(temp.indexOf(".")+1);
					else
					{
						varnam = temp.split("\\.")[1];
						temp = "";
					}
				}
				String key = varnam;
				if(varnam.indexOf("[")!=-1 && varnam.indexOf("]")!=-1)
				{
					varnam = key.substring(0,varnam.indexOf("["));
					key = key.substring(key.indexOf("[")+1);
					key = key.replaceAll("\\]", "");
					retVal = var.getValObj().getVar(varnam).getValFromArr(key);
				}
				else
					retVal = var.getValObj().getVarVal(varnam);
			}
			else
			{
				if(localVars.get(varn)!=null)
					retVal = localVars.get(varn).getValue();
				else if(argVars.get(varn)!=null)
					retVal = argVars.get(varn);
				else if(procm.getLocalVars().get(varn)!=null)
					retVal = procm.getLocalVars().get(varn).getValue();
				else
					retVal = varn;
				if(retVal.indexOf("\"")!=-1)
					retVal = retVal.replaceAll("\"","");
				else if(retVal.indexOf("'")!=-1)
					retVal = retVal.replaceAll("'","");
			}
		}
		else if(string.indexOf(".")!=-1 && string.indexOf("\"")==-1 && string.indexOf("'")==-1)
		{	
			Var var = null;
			String temp = string.trim();
			String varnam = "";
			while(temp.indexOf(".")!=-1 && temp.indexOf("\"")==-1 && temp.indexOf("'")==-1)
			{
				if(var!=null && var.getValObj()!=null && var.getValObj().getVar(temp.split("\\.")[0])!=null)
					var = var.getValObj().getVar(temp.split("\\.")[0]);
				else if(localVars.get(temp.split("\\.")[0])!=null)
					var = localVars.get(temp.split("\\.")[0]);
				else if(procm.getLocalVars().get(temp.split("\\.")[0])!=null)
					var = procm.getLocalVars().get(temp.split("\\.")[0]);
				if(temp.split("\\.").length>2)
					temp = temp.substring(temp.indexOf(".")+1);
				else
				{
					varnam = temp.split("\\.")[1];
					temp = "";
				}
			}
			String key = varnam;
			if(varnam.indexOf("[")!=-1 && varnam.indexOf("]")!=-1)
			{
				varnam = key.substring(0,varnam.indexOf("["));
				key = key.substring(key.indexOf("[")+1);
				key = key.replaceAll("\\]", "");
				retVal = var.getValObj().getVar(varnam).getValFromArr(key);
			}
			else
				retVal = var.getValObj().getVarVal(varnam.trim());
		}
		else if(!forwhif)
		{
			String varn = string.trim();
			if(localVars.get(string.trim())!=null)
				varn = localVars.get(string.trim()).getValue();
			else if(argVars.get(string.trim())!=null)
				varn = argVars.get(string.trim());
			else if(procm.getLocalVars().get(string.trim())!=null)
				varn = procm.getLocalVars().get(string.trim()).getValue();
			if(varn!=null && varn.indexOf("\"")!=-1)
				varn = varn.replaceAll("\"","");
			else if(varn!=null && varn.indexOf("'")!=-1)
				varn = varn.replaceAll("'","");
			retVal = varn;
		}
		return retVal;
	}
	private String getVariable(String string,ProcessM procm)
	{
		String varn = string.trim();if("".equals(varn))return "";
		if(localVars.get(string.trim())!=null)
			varn = localVars.get(string.trim()).getValue();
		else if(argVars.get(string.trim())!=null)
			varn = argVars.get(string.trim());
		else if(procm.getLocalVars().get(string.trim())!=null)
			varn = procm.getLocalVars().get(string.trim()).getValue();
		if(varn!=null && varn.equals(string.trim()))
		{
			if((varn.charAt(0)=='"' && varn.charAt(varn.length()-1)=='"')
					|| (varn.charAt(0)=='\'' && varn.charAt(varn.length()-1)=='\''))
			{
			}
			else
			{
				try{Long.valueOf(varn);}
				catch(Exception e)
				{
					if(varn.equalsIgnoreCase("false") || varn.equalsIgnoreCase("true"))
					{}
					else
					{System.out.println("Invalid assignment for variable "+varn);procm.setShouldProcRun(false);}
				}
			}
		}
		else if(varn==null)
		{
			varn = string.trim();
			if((varn.charAt(0)=='"' && varn.charAt(varn.length()-1)=='"')
					|| (varn.charAt(0)=='\'' && varn.charAt(varn.length()-1)=='\''))
			{
			}
			else
			{
				try{Long.valueOf(varn);}
				catch(Exception e)
				{
					if(varn.equalsIgnoreCase("false") || varn.equalsIgnoreCase("true"))
					{}
					else
					{System.out.println("Invalid assignment for variable "+varn);procm.setShouldProcRun(false);}
				}
			}
			varn = string.trim();
		}
		if(varn!=null && varn.indexOf("\"")!=-1)
			varn = varn.replaceAll("\"","");
		else if(varn!=null && varn.indexOf("'")!=-1)
			varn = varn.replaceAll("'","");
		return varn;
	}
	private ProcThr executeThread(String string, String args, ProcessM procm,Map<String,Var> localVars)
	{
		ProcThr t = new ProcThr(args,procm,string,localVars);
		new Thread(t).start();
		return t;
	}
	private boolean evaluateConditions(String expr,ProcessM procm)
	{
		boolean val = false;
		while(expr.indexOf("(")!=-1)
		{
			int rbb = expr.lastIndexOf("(");
			int rbe = expr.indexOf(")");
			String ter3 = expr.substring(rbb+1,rbe);
			String[] toks = ter3.trim().split("&&|\\|\\|");
			String ter = ter3.trim().replaceAll("[^&\\|]", "");	
			ter = ter.replaceAll("&&", "&");
			ter = ter.replaceAll("\\|\\|", "|");
			RetValTyp typ = evaluateLogicalValues(ter, "&", toks, procm);
			typ = evaluateLogicalValues(typ.ter, "|", typ.toks, procm);
			String val1 = typ.toks[0];
			String tempexp = expr.substring(0,rbb) + val1;
			tempexp += expr.substring(rbe+1);
			expr = tempexp;
		}
		String[] toks = expr.split("&&|\\|\\|");
		String ter = expr.replaceAll("[^&\\|]", "");
		RetValTyp typ = evaluateLogicalValues(ter, "&", toks, procm);
		typ = evaluateLogicalValues(typ.ter, "|", typ.toks, procm);
		val = Boolean.valueOf(typ.toks[0]);
		return val;
	}
	
	private RetValTyp evaluateLogicalValues(String ter,String tok,String[] toks,ProcessM procm)
	{
		if(ter.trim().equals(""))
		{
			RetValTyp typ = new RetValTyp();
			typ.ter = ter;
			typ.toks = new String[]{String.valueOf(evaluateLogicalValue(toks[0],procm))};
			return typ;
		}
		while(ter.indexOf(tok)!=-1)
		{
			String[] toks1 = new String[toks.length-1];
			for (int i = 0; i < ter.indexOf(tok); i++)
			{
				toks1[i] = toks[i];
			}			
			boolean val1 = evaluateLogicalValue(toks[ter.indexOf(tok)],procm);
			boolean val2 = evaluateLogicalValue(toks[ter.indexOf(tok)+1],procm);
			boolean valr = false;
			if("&".equals(tok))
				valr = val1 && val2;
			else
				valr = val1 || val2;
			toks1[ter.indexOf(tok)] = String.valueOf(valr);
			for (int i = ter.indexOf(tok)+1; i < toks1.length; i++)
			{
				toks1[i] = toks[i+1];
			}
			ter = ter.substring(0,ter.indexOf(tok)) + ter.substring(ter.indexOf(tok)+1);
			toks = toks1;
		}
		RetValTyp typ = new RetValTyp();
		typ.ter = ter;
		typ.toks = toks;
		return typ;
	}
	private boolean evaluateLogicalValue(String inter,ProcessM procm)
	{
		String conds = inter.trim();	
		String chktyp="";
		if(conds.indexOf(">=")!=-1 || conds.indexOf("=>")!=-1)
		{
			chktyp = ">=";
		}
		else if(conds.indexOf("=<")!=-1 || conds.indexOf("<=")!=-1)
		{
			chktyp = "<=";
		}
		else if(conds.indexOf(">")!=-1)
		{
			chktyp = ">";
		}
		else if(conds.indexOf("<")!=-1)
		{
			chktyp = "<";
		}
		else if(conds.indexOf("==")!=-1)
		{
			chktyp = "==";
		}				
		else if(conds.indexOf("!=")!=-1)
		{
			chktyp = "!=";
		}
		if(chktyp.equals(""))
		{
			conds = evaluate(conds,procm);
			try
			{
				boolean valk = Boolean.valueOf(conds);
				return valk;
			}			
			catch(Exception e)
			{
				return (conds!=null && !conds.equals(""));
			}
		}
		String lhs = evaluate(conds.split(chktyp)[0], procm);
		String rhs = evaluate(conds.split(chktyp)[1], procm);
		return evaluateCondition(lhs, rhs, chktyp);
	}
	private boolean evaluateCondition(String lhs,String rhs,String type)
	{		
		if(type.equals(">") || type.equals("<") || type.equals(">=") || type.equals("=>")
				|| type.equals("<=") || type.equals("=<") || type.equals("==") || type.equals("!="))
		{
			if(type.indexOf("<=")!=-1 || type.indexOf("=<")!=-1)
			{
				return Integer.parseInt(lhs) <= Integer.parseInt(rhs);
			}
			else if(type.indexOf("=>")!=-1 || type.indexOf(">=")!=-1)
			{
				return Integer.parseInt(lhs) >= Integer.parseInt(rhs);
			}
			else if(type.indexOf("!=")!=-1)
			{
				boolean fl = false;
				try
				{
					fl = Integer.parseInt(lhs) != Integer.parseInt(rhs);
				}
				catch(NumberFormatException e)
				{
					fl = lhs.equals(rhs);
				}
				return fl;
			}	
			else if(type.indexOf(">")!=-1)
			{
				return Integer.parseInt(lhs) > Integer.parseInt(rhs);
			}
			else if(type.indexOf("<")!=-1)
			{
				return Integer.parseInt(lhs) < Integer.parseInt(rhs);
			}
			else if(type.indexOf("==")!=-1)
			{
				boolean fl = false;
				try
				{
					fl = Integer.parseInt(lhs) == Integer.parseInt(rhs);
				}
				catch(NumberFormatException e)
				{
					fl = lhs.equals(rhs);
				}
				return fl;
			}
			return false;
		}
		else
		{
			
		}
		return false;
	}
	private void evaluateLoops(String inter,String string, ProcessM procm)
	{
		String chkval = "",initval = "";
		String chktyp = "";
		int incby = 0;
		if(string.equals("FOR"))
		{
			String forparms = inter.trim();
			forparms = forparms.replaceAll(" ", "");
			String[] conds = forparms.split(";");
			if(localVars.get(conds[0].split("=")[0])!=null && (localVars.get(conds[0].split("=")[0]).getValue().equals(conds[0].split("=")[1])))
				localVars.get(conds[0].split("=")[0]).setValue(conds[0].split("=")[1],procm);
			else if(localVars.get(conds[0].split("=")[0])==null)
			{
				Var var = new Var();
				var.setName(conds[0].split("=")[0]);				
				var.setType("number");
				var.setValue(conds[0].split("=")[1],procm);
				var.setVisiblity("loop");
				var.setLevel("default");
				localVars.put(var.getName(),var);
			}
			initval = localVars.get(conds[0].split("=")[0]).getValue();
			if(conds[1].indexOf(">=")!=-1 || conds[1].indexOf("=>")!=-1)
			{
				chktyp = ">=";
				chkval = conds[1].split(chktyp)[1];
			}
			else if(conds[1].indexOf("=<")!=-1 || conds[1].indexOf("<=")!=-1)
			{
				chktyp = "<=";
				chkval = conds[1].split(chktyp)[1];
			}
			else if(conds[1].indexOf(">")!=-1)
			{
				chktyp = ">";
				chkval = conds[1].split(chktyp)[1];
			}
			else if(conds[1].indexOf("<")!=-1)
			{
				chktyp = "<";
				chkval = conds[1].split(chktyp)[1];
			}
			else if(conds[1].indexOf("==")!=-1)
			{
				chktyp = "==";
				chkval = conds[1].split(chktyp)[1];
			}				
			else if(conds[1].indexOf("!=")!=-1)
			{
				chktyp = "!=";
				chkval = conds[1].split(chktyp)[1];
			}	
			if(localVars.get(chkval)!=null)
				chkval = localVars.get(chkval).getValue();
			else if(argVars.get(chkval)!=null)
				chkval = argVars.get(chkval);
			else if(procm.getLocalVars().get(chkval)!=null)
				chkval = procm.getLocalVars().get(chkval).getValue();
			if(conds[2].indexOf("++")!=-1)
				incby = 1;
			else if(conds[2].indexOf("--")!=-1)
				incby = -1;
			else if(conds[2].indexOf("+")!=-1)
				incby = Integer.parseInt(conds[2].split("+")[1]);
			else if(conds[2].indexOf("-")!=-1)
				incby = Integer.parseInt(conds[2].split("-")[1]);
			if(!evaluateCondition(initval, chkval, chktyp))
			{
				if(strs[currentCounter].trim().equals("{"))
				{							
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;							
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;						
						}
						currentCounter++;
					}
				}
				else
				{
					currentCounter++;
				}
			}
			else if(strs[currentCounter].trim().equals("{"))
			{				
				//for (; evaluateCondition(initval, chkval, chktyp); initval=String.valueOf(Integer.parseInt(initval)+incby))
				if(evaluateCondition(initval, chkval, chktyp))
				{
					int stmtcnt = 0;
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;
							currentCounter++;
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;
							if(cntr!=0)
								currentCounter++;							
						}
						if(cntr>0)
						{
							executeNextLine(procm);
							stmtcnt++;
						}
					}	
					if(evaluateCondition(initval, String.valueOf(Integer.parseInt(chkval)-1), "!="))
						currentCounter = currentCounter - stmtcnt - 2;
					localVars.get(conds[0].split("=")[0]).setValue(String.valueOf(Integer.parseInt(initval)+incby),procm);					
				}
			}
			else
			{
				//for (; evaluateCondition(initval, chkval, chktyp); initval=String.valueOf(Integer.parseInt(initval)+incby))
				if(evaluateCondition(initval, chkval, chktyp))
				{
					executeNextLine(procm);
					currentCounter = currentCounter-1;
					initval=String.valueOf(Integer.parseInt(initval)+incby);
					localVars.get(conds[0].split("=")[0]).setValue(initval,procm);
				}
				//currentCounter = currentCounter+1;
			}
		}
		else if(string.equals("WHILE"))
		{
			if(!evaluateConditions(inter.trim(),procm))
			{
				if(strs[currentCounter].trim().equals("{"))
				{							
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;							
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;						
						}
						currentCounter++;
					}
				}
				else
				{
					currentCounter++;
				}
			}
			else if(strs[currentCounter].trim().equals("{"))
			{				
				if (evaluateConditions(inter.trim(),procm))
				{
					int stmtcnt = 0;
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;
							currentCounter++;
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;
							if(cntr!=0)
								currentCounter++;							
						}
						if(cntr>0)
						{
							executeNextLine(procm);
							stmtcnt++;
						}
					}
					if(evaluateConditions(inter.trim(),procm))
						currentCounter = currentCounter - stmtcnt - 2;
				}
			}
			else
			{
				if (evaluateConditions(inter.trim(),procm))
				{
					executeNextLine(procm);
					if(evaluateConditions(inter.trim(),procm))
						currentCounter = currentCounter - 2;
				}
			}
		}
		else if(string.equals("IF"))
		{			
			if(strs[currentCounter].trim().equals("{"))
			{				
				if (evaluateConditions(inter.trim(),procm))
				{
					int stmtcnt = 0;
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;
							currentCounter++;
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;
							currentCounter++;							
						}
						if(cntr>0)
						{
							executeNextLine(procm);
							stmtcnt++;
						}
					}
				}
				else
				{
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;							
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;						
						}
						currentCounter++;
					}
				}
				while (currentCounter<strs.length && evaluateConditions(inter.trim(),procm) && (strs[currentCounter].trim().indexOf("elseif")!=-1
						 || strs[currentCounter].trim().indexOf("else")!=-1))
				{
					currentCounter++;
					if(strs[currentCounter].trim().equals("{"))
					{							
						int cntr = 1;
						while(cntr>0)
						{
							if(strs[currentCounter].trim().equals("{"))
							{
								if(cntr!=1)
									cntr++;							
							}
							else if(strs[currentCounter].trim().equals("}"))
							{
								cntr--;						
							}
							currentCounter++;
						}
					}
					else
					{
						currentCounter++;
					}
				}
			}
			else
			{
				if (evaluateConditions(inter.trim(),procm))
				{
					executeNextLine(procm);
				}
				else
				{
					currentCounter++;
				}
				while (currentCounter<strs.length && evaluateConditions(inter.trim(),procm) && (strs[currentCounter].trim().indexOf("elseif")!=-1
						 || strs[currentCounter].trim().indexOf("else")!=-1))
				{
					currentCounter++;
					if(strs[currentCounter].trim().equals("{"))
					{							
						int cntr = 1;
						while(cntr>0)
						{
							if(strs[currentCounter].trim().equals("{"))
							{
								if(cntr!=1)
									cntr++;							
							}
							else if(strs[currentCounter].trim().equals("}"))
							{
								cntr--;						
							}
							currentCounter++;
						}
					}
					else
					{
						currentCounter++;
					}
				}
			}
		}	
		else if(string.equals("ELSE"))
		{
			if(strs[currentCounter].trim().equals("{"))
			{				
				if (true)
				{
					int stmtcnt = 0;
					int cntr = 1;
					while(cntr>0)
					{
						if(strs[currentCounter].trim().equals("{"))
						{
							if(cntr!=1)
								cntr++;
							currentCounter++;
						}
						else if(strs[currentCounter].trim().equals("}"))
						{
							cntr--;
							currentCounter++;							
						}
						if(cntr>0)
						{
							executeNextLine(procm);
							stmtcnt++;
						}
					}
				}
			}
			else
				executeNextLine(procm);
		}
	}
	class RetValTyp
	{
		String ter = null;
		String[] toks = null;
	}
	private String evalBODMAS(String expr,ProcessM procm)
	{
		String[] toks = expr.split("\\+|\\-|/|\\*");
		String ter11 = expr.replaceAll("[^\\+\\-/\\*]", "");
		String tempexpr =  "";
		for (int i = 0; i < toks.length; i++)
		{
			tempexpr += evaluate(toks[i], procm);
			if(ter11.length()-1>=i)
				tempexpr += ter11.charAt(i);
		}
		expr = tempexpr;
		while(expr.indexOf("(")!=-1)
		{
			int rbb = expr.lastIndexOf("(");
			int rbe = expr.indexOf(")");
			String ter3 = expr.substring(rbb+1,rbe);
			toks = ter3.split("\\+|\\-|/|\\*");
			String ter = ter3.replaceAll("[^\\+\\-/\\*]", "");
			RetValTyp typ = evaluateAritmetic(ter, "/", toks, procm);
			typ = evaluateAritmetic(typ.ter, "*", typ.toks, procm);
			typ = evaluateAritmetic(typ.ter, "+", typ.toks, procm);
			typ = evaluateAritmetic(typ.ter, "-", typ.toks, procm);
			String val = typ.toks[0];
			String tempexp = expr.substring(0,rbb) + val;
			tempexp += expr.substring(rbe+1);
			expr = tempexp;
		}
		toks = expr.split("\\+|\\-|/|\\*");
		String ter = expr.replaceAll("[^\\+\\-/\\*]", "");
		RetValTyp typ = evaluateAritmetic(ter, "/", toks, procm);
		typ = evaluateAritmetic(typ.ter, "*", typ.toks, procm);
		typ = evaluateAritmetic(typ.ter, "+", typ.toks, procm);
		typ = evaluateAritmetic(typ.ter, "-", typ.toks, procm);
		String val = typ.toks[0];
		return val;
	}
	
	private RetValTyp evaluateAritmetic(String ter,String tok,String[] toks,ProcessM procm)
	{
		while(ter.indexOf(tok)!=-1)
		{
			String[] toks1 = new String[toks.length-1];
			for (int i = 0; i < ter.indexOf(tok); i++)
			{
				toks1[i] = toks[i];
			}
			String val1 = evaluate(toks[ter.indexOf(tok)],procm);
			String val2 = evaluate(toks[ter.indexOf(tok)+1],procm);
			String vah = "";
			boolean flag = false;
			try
			{
				Long.valueOf(val1);
				Long.valueOf(val2);
			}
			catch (Exception e)
			{
				flag = true;
			}
			if(flag)
			{
				vah = val1 + val2;
			}
			else
			{
				long valr = 0L;
				if("/".equals(tok))
					valr = Long.valueOf(val1)/Long.valueOf(val2);
				else if("*".equals(tok))
					valr = Long.valueOf(val1)*Long.valueOf(val2);
				else if("+".equals(tok))
					valr = Long.valueOf(val1)+Long.valueOf(val2);
				else
						valr = Long.valueOf(val1)-Long.valueOf(val2);
				vah = String.valueOf(valr);
			}
			toks1[ter.indexOf(tok)] = vah;
			for (int i = ter.indexOf(tok)+1; i < toks1.length; i++)
			{
				toks1[i] = toks[i+1];
			}
			ter = ter.substring(0,ter.indexOf(tok)) + ter.substring(ter.indexOf(tok)+1);
			toks = toks1;
		}
		RetValTyp typ = new RetValTyp();
		typ.ter = ter;
		typ.toks = toks;
		return typ;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public int getIdent()
	{
		return ident;
	}
	public void setIdent(int ident)
	{
		this.ident = ident;
	}
	public void init()
	{
		currentCounter=0;
	}
}
