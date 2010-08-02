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
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.cpfj.JBSLProcessor.Message;

public class ProcessM
{
	private long id;
	private Queue<Message> q;
	private static long pidcounter = 0L;
	private Map<String,Var> localVars;
	private Map<String,Function> funcs;
	private Function mainFunc = null;
	private Map<String,Object> objs;
	private List<ProcThr> threads;
	StringBuffer buf = null;
	private String name = null;
	private boolean shouldProcRun;
	public ProcessM(String name,boolean nofile)
	{
		this.q = new ConcurrentLinkedQueue<Message>();
		this.localVars = new HashMap<String,Var>();
		this.funcs = new HashMap<String, Function>();
		this.objs = new HashMap<String, Object>();	
		this.name = name;
		this.threads = new ArrayList<ProcThr>();
		this.shouldProcRun = true;
	}
	
	public ProcessM(String str)
	{
		this.id = generateNewID();
		this.q = new ConcurrentLinkedQueue<Message>();
		this.localVars = new HashMap<String,Var>();
		this.funcs = new HashMap<String, Function>();
		this.objs = new HashMap<String, Object>();	
		this.threads = new ArrayList<ProcThr>();
		this.shouldProcRun = true;
		this.createproc(str);
	}
	
	public ProcessM(String str,int y)
	{
		this.id = generateNewID();
		this.q = new ConcurrentLinkedQueue<Message>();
		this.localVars = new HashMap<String,Var>();
		this.funcs = new HashMap<String, Function>();
		this.objs = new HashMap<String, Object>();	
		this.threads = new ArrayList<ProcThr>();
		this.shouldProcRun = true;
		try
		{
			BufferedReader buffr = new BufferedReader(new FileReader(str));
			buf = new StringBuffer();
			String temp = null;
			while((temp=buffr.readLine())!=null)
			{
				buf.append(temp+"\n");
			}
			String cont = buf.toString();
			this.createproc(cont);
		}
		catch (Exception e) 
		{
			System.out.println("Script not found at "+str);
			this.shouldProcRun = false;
		}
	}
	
	public void createproc(String cont)
	{
		try
		{
			//String[] units = cont.split("function ");
			boolean initz = false;
			//for(int i=0;i<units.length;i++)
			//{
				String[] lineunits = cont.split("\n");
				boolean funcstart = false;					
				Function func = null;	
				Object obj = null;
				String objname = null;
				int countbrcks = 0;
				for(String linstring : lineunits)
				{	
					if((linstring.length()>2 && linstring.charAt(0)=='/' && linstring.charAt(1)=='/'))continue;
					if(!funcstart && !initz && linstring.indexOf("=")!=-1 && linstring.indexOf(" new ")!=-1 
							&& (linstring.indexOf("\"")>linstring.indexOf("=") || linstring.indexOf("\"")==-1) 
							&& (linstring.indexOf("'")>linstring.indexOf("=") || linstring.indexOf("'")==-1)
							) 
					{
						String[] strs = linstring.split("=");							
						Var var = new Var();
						var.setName(strs[0].trim());
						String strss = strs[1].replaceFirst(" new ","");	
						var.setType(strss.split("\\(")[0].trim());
						strss = strss.replaceFirst(var.getType()+"\\(", "");
						strss = strss.replaceFirst("\\)", "");
						if(!initz && !funcstart)
							var.setVisiblity("global");
						else if(funcstart)
							var.setVisiblity("function");
						var.setLevel("default");
						if(!var.getType().equals("file") && !var.getType().equals("socket"))
						{
							var.setValObj(objs.get(var.getType()));
							Var.initializeVals(var, objs);
							var.getValObj().init(strss, objs);
						}
						localVars.put(var.getName(), var);
					}
					else if(!funcstart && !initz && linstring.indexOf("=")!=-1 
							&& (linstring.indexOf("\"")>linstring.indexOf("=") || linstring.indexOf("\"")==-1) 
							&& (linstring.indexOf("'")>linstring.indexOf("=") || linstring.indexOf("'")==-1))
					{
						String[] strs = linstring.split("=");
						if((strs[1].trim().charAt(0)=='"' || strs[1].trim().charAt(0)=='\'') && 
							(strs[1].trim().charAt(strs[1].trim().length()-1)=='"' 
								|| strs[1].trim().charAt(strs[1].trim().length()-1)=='\''))
							strs[1] = strs[1].trim().substring(1,strs[1].trim().length()-1);
						{
							Var var = new Var();
							var.setName(strs[0].trim());
							var.setType("runtime");
							var.setVisiblity("global");
							var.setLevel("default");
							var.setValue(strs[1].trim(),this);
							localVars.put(var.getName(), var);
						}
					}
					else if(!funcstart && !initz && linstring.indexOf(" is ")!=-1 
							&& (linstring.indexOf("\"")>linstring.indexOf(" is ") || linstring.indexOf("\"")==-1) 
							&& (linstring.indexOf("'")>linstring.indexOf(" is ") || linstring.indexOf("'")==-1))
					{
						String[] strs = linstring.split(" is ");
						if((strs[1].trim().charAt(0)=='"' || strs[1].trim().charAt(0)=='\'') && 
							(strs[1].trim().charAt(strs[1].trim().length()-1)=='"' 
								|| strs[1].trim().charAt(strs[1].trim().length()-1)=='\''))
							strs[1] = strs[1].trim().substring(1,strs[1].trim().length()-1);
						{
							Var var = new Var();
							var.setName(strs[0].trim());
							var.setType(strs[1].trim());
							var.setVisiblity("global");
							var.setName("default");
							localVars.put(var.getName(), var);
						}
					}
					else if(linstring.indexOf("function")!=-1 || linstring.indexOf("_obj_prop_def")!=-1
							|| linstring.indexOf("_obj_mem_def")!=-1 || linstring.indexOf("thread_def_run")!=-1)
					{							
						String ter = linstring.trim().replaceAll("\\(","");
						ter = ter.replaceAll("\\)","");
						if(linstring.indexOf("function")!=-1 || linstring.indexOf("thread_def_run")!=-1)
						{
							if(linstring.indexOf("thread_def_run")!=-1)
							{
								ter = ter.replaceAll("thread_def_run","");
								ter = "thread::"+ter;
							}
							else
								ter = ter.replaceAll("function ","");
							func = new Function(ter);
							if(ter.indexOf("start")!=-1 || ter.indexOf("start,")!=-1)
								initz = true;
						}
						else if(linstring.indexOf("_obj_prop_def")!=-1)
						{
							ter = ter.replaceAll("_obj_prop_def","");
							obj = new Object(this);
							obj.setName(ter);
						}
						else if(linstring.indexOf("_obj_mem_def")!=-1)
						{
							ter = ter.replaceAll("_obj_mem_def","");
							String[] temo = ter.split(",");
							objname = temo[0];
							ter = ter.replaceFirst(temo[0]+",","");
							func = new Function(ter);
							initz = true;
						}
					}
					else if("{".equals(linstring.trim()))
					{
						if(funcstart)
							func.addInstructions(linstring);
						funcstart = true;
						countbrcks++;							
					}
					else if("}".equals(linstring.trim()))
					{
						countbrcks--;							
						if(countbrcks==0)
						{
							funcstart = false;
							if(func!=null && objname!=null && objs.get(objname)!=null && !func.getName().equals("start"))
							{
								func.done();
								objs.get(objname).addFunc(false, func);
								func = null;
								objname = null;
							}
							else if(func!=null)
							{
								func.done();
								if(func.getName().equals("start"))
									this.mainFunc = func;
								else
									funcs.put(func.getName()+func.getIdent(), func);
								func = null;
							}
							else if(obj!=null)
							{
								objs.put(obj.getName(), obj);
								obj = null;
							}
						}
						else
						{
							if(func!=null)
								func.addInstructions(linstring);
						}
					}
					else if(funcstart)
					{
						if(func!=null)
						{
							func.addInstructions(linstring);
						}
						else if(obj!=null)
						{
							String[] strs = linstring.split(" is ");
							if((strs[1].trim().charAt(0)=='"' || strs[1].trim().charAt(0)=='\'') && 
								(strs[1].trim().charAt(strs[1].trim().length()-1)=='"' 
									|| strs[1].trim().charAt(strs[1].trim().length()-1)=='\''))
								strs[1] = strs[1].trim().substring(1,strs[1].trim().length()-1);
							{
								Var var = new Var();
								var.setName(strs[0].trim());
								strs = strs[1].split(" ");
								var.setType(strs[0].trim());
								if(var.getType().equals("bounded-string-array"))
								{
									var.setArrEle(Integer.valueOf(strs[1]));
									if(strs.length>2 && !strs[2].equals(""))
										var.setVisiblity(strs[2]);
									if(strs.length>3 && !strs[3].equals(""))
										var.setLevel(strs[3]);
								}
								else if(strs.length>1)
									var.setVisiblity(strs[1]);
								if(strs.length>2 && !strs[2].equals(""))
									var.setLevel(strs[2]);
								if(var.getLevel()==null || !var.getLevel().toLowerCase().equals("private"))
									obj.addVar(false,var,objs);
								else
									obj.addVar(true,var,objs);
							}
						}
					}
				}
			//}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private long generateNewID()
	{
		pidcounter++;
		return pidcounter;
	}
	public long getId()
	{
		return id;
	}
	public void addMesasge(Message message)
	{
		this.q.add(message);
	}
	public Message getMesasge()
	{
		return this.q.poll();
	}
	public void executeMain() throws IOException
	{	
		this.mainFunc.executeFunc("",this);			
	}
	public void executeNextLine() throws IOException
	{	
		this.mainFunc.executeNextLine(this);			
	}
	public boolean checkFuncDone()
	{
		return this.mainFunc.checkFuncDone();
	}
	public Map<String, Function> getFuncs()
	{
		return funcs;
	}
	public Map<String, Var> getLocalVars()
	{
		return localVars;
	}
	public Function getMainFunc()
	{
		return mainFunc;
	}
	public Map<String, Object> getObjs()
	{
		return objs;
	}
	public void setFuncs(Map<String, Function> funcs)
	{
		this.funcs = funcs;
	}
	public void setLocalVars(Map<String, Var> localVars)
	{
		this.localVars = localVars;
	}
	public void setObjs(Map<String, Object> objs)
	{
		this.objs = objs;
	}
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public List<ProcThr> getThreads()
	{
		return threads;
	}
	public void setThreads(List<ProcThr> threads)
	{
		this.threads = threads;
	}
	public boolean isShouldProcRun()
	{
		return shouldProcRun;
	}
	public void setShouldProcRun(boolean shouldProcRun)
	{
		this.shouldProcRun = shouldProcRun;
		for (ProcThr procthr : threads)
		{
			procthr.setShouldRun(shouldProcRun);
		}
	}
	public boolean complete()
	{
		boolean flag = true;
		for (ProcThr procthr : threads)
		{
			flag &= procthr.isCompleted();
		}
		return flag;
	}
}