package com.cpfj;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.media.sound.HsbParser;

public class ProcThr implements Runnable
{
	private String args;
	private ProcessM procm;
	private String string;
	private boolean shouldRun;
	private boolean completed = false;
	private Map<String,Var> localVars;
	public boolean isCompleted()
	{
		return completed;
	}
	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}
	public ProcThr(String args,ProcessM procm,String string,Map<String,Var> localVars)
	{
		this.args = args;
		this.procm = procm;
		for (Iterator iter = this.procm.getFuncs().values().iterator(); iter.hasNext();)
		{
			Function func = (Function)iter.next();
			func.init();
		}
		if(procm.getLocalVars()!=null)
		{
			this.procm.setLocalVars(new HashMap<String, Var>());
			for (Iterator iter = procm.getLocalVars().entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,Var> entry = (Map.Entry<String,Var>)iter.next();
				this.procm.getLocalVars().put(entry.getKey(), Var.getCopy(entry.getValue()));
			}	
		}
		this.string = string;
		this.shouldRun = true;
		this.localVars = new HashMap<String, Var>();
		if(localVars!=null)
		{
			for (Iterator iter = localVars.entrySet().iterator(); iter.hasNext();)
			{
				Map.Entry<String,Var> entry = (Map.Entry<String,Var>)iter.next();
				this.localVars.put(entry.getKey(), Var.getCopy(entry.getValue()));
			}			
		}
	}
	public void run()
	{	
		procm.getFuncs().get(string).getCopy().executeFunc(this.args,this.procm,this);
		completed = true;
	}
	public boolean isShouldRun()
	{
		return shouldRun;
	}
	public void setShouldRun(boolean shouldRun)
	{
		this.shouldRun = shouldRun;
	}
	public Map<String, Var> getLocalVars()
	{
		return localVars;
	}
	public void setLocalVars(Map<String, Var> localVars)
	{
		this.localVars = localVars;
	}
}
