
```
//Free or Loose variable declaration
i=1
j='sddd'
k="asadasdas"

//Strict variable declaration
z is number

//Object Property List definition , yourobject - name of object
_obj_prop_def(yourobject)
{
	ii is number
	jj is string
}

//Object Property List definition , myobject - name of object
_obj_prop_def(myobject)
{
	i is number private
	j is string
	k is boolean

        //Bounded array of length 5
	l is bounded-string-array 5

        //UnBounded array
	m is unbounded-string-array

        //Object as a property
	n is yourobject	
}

//Object member definition, myobject - object name, someaction - member name
_obj_mem_def(myobject,someaction)
{
	out("asdas")
	return "dddd"
}

//Object member definition, myobject - object name, _init_ - constructor
_obj_mem_def(myobject,_init_)
{
        //new construct - create new object instance
	n = new yourobject()
	n.ii = 10
	n.jj = "123123"
}

//Program start location
function (start)
{	
        //eval function
	eval("i=90\nout(i)")	
	b = new myobject()
	ddd = b.someaction() + b.someaction()
 
        //Prints to standard output 
	out(b.i)
	out(b.j)
	out(b.k)
 
        //Array assignment
	b.l[0] = "asdas"
	out(b.l)
	out(b.l[0])
	out(b.m)
	out(b.n.ii)
	out(ddd)

        //BODMAS rule check
	b.j = 1 + k + b.i + 6 + 3 + b.n.ii
	out(b)

        //Object assignment
	ggg = b
	out(ggg)

        u = test(2) + test(2) - test(2) / test(2) * test(2)
	w = test1(2) + test1("asdasd")
	out(u)
	out(w)
	out(test(2))

        //Inter-Process communication, 
        //2 - id of process to be notified
        //NOTIF - type of communication
	sendMsg(2,NOTIF,hi how are you)	
}

//Process Function test, argument t 
function (test,t)
{
	k=5
	out(t)
	return k
}

//Process Function test
function (test)
{
	for(i=0;i<10;i++)
	{
		out(j)
	}
	ff = 1
	while(ff<5)
	{
		out(ff)
		ff = ff + 1
	}
	if(ff==6)
	{
		out("if")
	}
	elseif(ff==8)
	{
		out("elseif")
	}
	else
	{
		out("else")
	}
}

//Process Function test1, argument e
function (test1,e)
{
	return e
}
```