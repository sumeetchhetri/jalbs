package com.cpfj;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

public class prop
{
	@XStreamAsAttribute
	String type;
	@XStreamAsAttribute
	String name;
	String value;
}
