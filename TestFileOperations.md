
```
//Process Entry point
function (start)
{	
        //Declare a new file object
	fff = new file()

        //Open a new file
	fff.open("C:\Documents and Settings\sumeetc\workspace\cpfj\src\com\cpfj\test2.jbsl");

        //Read all contents from the file
	gf = fff.read()
        out(gf)

        //Spawn a new Process with the given file contents as a script
	id = spawnProcess(gf)	
}
```