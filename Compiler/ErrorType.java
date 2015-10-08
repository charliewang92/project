//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class ErrorType extends Type
{
	public String msg;
	public boolean printed = false;
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public ErrorType(String msg)
	{
		super(msg, 0);
		this.msg = msg;
		printed = false;
	}

	public ErrorType(String string, String strID) {
		super(strID,0);
		this.msg = string;
		printed = false;
	}

	//----------------------------------------------------------------
	//	There are times where it is an error if the Type is not a 
	//	assignable, equivalent,  or something else. However, if
	//	the Type is already an error, nothing should be said. To
	//	supress that error, we would have to check if the Type is
	//	not an ErrorType as well as what we want it to be.  Rather
	//	than 2 checks we'll have the ErrorType always return true
	//	for every check.
	//----------------------------------------------------------------
    public boolean isError()            { return true; }
    public boolean print(ErrorPrinter err) {
    	if (!printed) {
    		err.print(msg);
    		printed = true;
    		return true;
    	} else {
    		return false;
    	}
    	
    }
}
