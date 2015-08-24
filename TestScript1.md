
```
//Process entry point
function (start)
{
        //Create a new Thread with 2 arguments
	thread::thread1(1,2)

        //Receive message from other Processes
	i = recvMsg()
	while(i=="")
	{
		i = recvMsg()
	}
	out("received message")
	out(i)

        //Create another new Thread with 2 arguments
	thread::thread1(1,2)
	ip = 1
	while(ip==1)
	{
		out("running process 2")
 
                //Sleep for 5 seconds
		sleep(5)
	}
}

//Thread definition, thread1 - name of thread, a,b - arguments to thread
thread_def_run(thread1,a,b)
{
	ip = 1
	while(ip==1)
	{
		out("running thread")
		out(a)
		out(b)
		sleep(5)
	}
}
```