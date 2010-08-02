package com.cpfj;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ConcurrentHashMap;

public class JBSLProcessor
{
	public static ConcurrentHashMap<Long, ProcessM> processes = new ConcurrentHashMap<Long, ProcessM>();
	private static JBSLProcessor instance = null;
	
	int totthreads = 1;
	int procpjt = 4;
	private JBSLProcessor()
	{			
	}	
	public static JBSLProcessor get()
	{
		if(instance==null)
			instance = new JBSLProcessor();		
		return instance;
	}
	public JBSLProcessor(int totthreads,int procpjt)
	{
		this.procpjt = procpjt;
		this.totthreads = totthreads;
	}
	static class Message
	{
		long frmid;
		java.lang.Object message = null;
		String type = null;
		public long getFrmid()
		{
			return frmid;
		}
		public void setFrmid(long frmid)
		{
			this.frmid = frmid;
		}
		public java.lang.Object getMessage()
		{
			return message;
		}
		public void setMessage(java.lang.Object message)
		{
			this.message = message;
		}
		public String getType()
		{
			return type;
		}
		public void setType(String type)
		{
			this.type = type;
		}
	}		
	public long newProcess(ProcessM pro)
	{
		processes.put(pro.getId(), pro);
		final ProcessM prog = pro;
		new Thread(new Runnable()
		{
			public void run()
			{						
				while(true)
				{						
					try
					{
						if(prog!=null && prog.isShouldProcRun() && !prog.checkFuncDone())
						{
							prog.executeNextLine();
						}
						else if(prog.complete())
						{
							break;
						}
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}
			}
		}).start();	
		return pro.getId();
	}
	
	public long newSynchronousProcess(ProcessM prog)
	{
		processes.put(prog.getId(), prog);
		while(true)
		{						
			try
			{
				if(prog!=null && prog.isShouldProcRun() && !prog.checkFuncDone())
				{
					prog.executeNextLine();
				}
				else if(prog.complete())
				{
					break;
				}
			}
			catch (Exception e)
			{
				System.out.println("Execution stopped due to syntax error.");
			}
		}
		return prog.getId();
	}
	
	public void killProcess(long id)
	{
		if(processes.containsKey(id))
			processes.remove(id);
	}
	
	public void send(long tid,Message message)
	{
		ProcessM procm = processes.get(tid);
		procm.addMesasge(message);
	}
	
	public java.lang.Object receive(long id)
	{
		ProcessM procm = processes.get(id);
		return procm.getMesasge();
	}
	
	public void executeProcesses() throws IOException
	{		
		for (int i = 1; i < totthreads+1; i++)
		{	
				
		}
	}
	private boolean debugMode = false;
	public static boolean isDebug()
	{
		return get().debugMode;
	}
	public static void main(String[] args)
	{
		try
		{
			JBSLProcessor conc = get();
			String debugflag = System.getProperty("debugflag");
			if(debugflag!=null && "true".equalsIgnoreCase(debugflag))
				conc.debugMode = true;
			
			if(args!=null && args.length>0)
			{
				for (int i = 0; i < args.length; i++)
				{
					conc.newProcess(new ProcessM(args[i],1));	
				}				
			}
			else
			{
				while(true)
				{
					System.out.print("jalbs> ");
					BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
					String command = reader.readLine();
					if(command.startsWith("run "))
					{
						String file = command.replaceFirst("run " , "");
						if(!file.equals(""))
						{
							conc.newSynchronousProcess(new ProcessM(file,1));
						}
						else
						{
							System.out.println("Please provide file name");
						}
					}
					else if(command.equalsIgnoreCase("quit"))
					{
						break;
					}
					else if(command.startsWith("debug "))
					{
						if(command.split(" ")[1].equalsIgnoreCase("true"))
						{
							get().debugMode = true;
						}
						else if(command.split(" ")[1].equalsIgnoreCase("false"))
						{
							get().debugMode = false;
						}
						else
						{
							System.out.println("Invalid value for debug Flag");
						}
					}
					else if(!command.equals(""))
					{
						synchronousEvaluate(command);
					}
					else
					{
						System.out.println("Invalid command");
					}
				}
			}
						
		}
		catch (Exception e)
		{
			System.out.println("Execution stopped due to syntax error.");
		}
	}
	
	public static void evaluate(String data)
	{
		if(data!=null && !data.equals("") && (data.charAt(0)=='"' || data.charAt(0)=='\'')
				&& (data.charAt(data.length()-1)=='"' || data.charAt(data.length()-1)=='\''))
		{
			data = data.substring(1,data.length()-1);
			data = data.replaceAll("\\\\n", "\n");
			data = "function (start)\n{\n" + data + "\n}";
			JBSLProcessor.get().newProcess(new ProcessM(data));
		}
		else if(data!=null && !data.equals(""))
		{
			data = data.replaceAll("\\\\n", "\n");
			data = "function (start)\n{\n" + data + "\n}";
			JBSLProcessor.get().newProcess(new ProcessM(data));
		}
	}
	
	public static void synchronousEvaluate(String data)
	{
		if(data!=null && !data.equals("") && (data.charAt(0)=='"' || data.charAt(0)=='\'')
				&& (data.charAt(data.length()-1)=='"' || data.charAt(data.length()-1)=='\''))
		{
			data = data.substring(1,data.length()-1);
			data = data.replaceAll("\\\\n", "\n");
			data = "function (start)\n{\n" + data + "\n}";
			JBSLProcessor.get().newSynchronousProcess(new ProcessM(data));
		}
		else if(data!=null && !data.equals(""))
		{
			data = data.replaceAll("\\\\n", "\n");
			data = "function (start)\n{\n" + data + "\n}";
			JBSLProcessor.get().newSynchronousProcess(new ProcessM(data));
		}
	}
}


