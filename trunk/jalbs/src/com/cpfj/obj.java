package com.cpfj;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class obj
{
	@XStreamAsAttribute
	String name;
	@XStreamAlias("props")
	prop[] props;	
}
