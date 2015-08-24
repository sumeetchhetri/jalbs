
```
//Process Entry point
function (start)
{
	i=1

        //Declare a new socket variable
	sock = new socket()

        //Make the socket a listener on port 9006
	sock.listen(9006)

	while(i==1)
	{
                //accept new incoming connections 
		sock1 = sock.accept(9006)

                //Let a new Thread handle the new connection
		thread::thread1(sock1)
	}
}

//Thread Definition, sock - argument
thread_def_run(thread1,sock)
{
	ip = 1
	while(ip==1)
	{
                //Read data from the incoming connection
		data = sock.read()
		out(data)

                //Write data to the incoming connection
		sock.write(data)
	}
}
```