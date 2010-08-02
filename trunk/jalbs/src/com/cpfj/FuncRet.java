package com.cpfj;

public class FuncRet
{
	public FuncRet()
	{
		retVal = "";
	}
	private boolean done;
	private String retVal;
	public boolean isDone()
	{		
		return done;
	}
	public void setDone(boolean done)
	{
		this.done = done;
	}
	public String getRetVal()
	{
		return retVal;
	}
	public void setRetVal(String retVal)
	{
		this.retVal = retVal;
	}
}
