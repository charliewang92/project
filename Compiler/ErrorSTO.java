//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class ErrorSTO extends STO
{
	public Type originalType;
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public ErrorSTO(String msg,String strName)
	{
		super(strName, new ErrorType(msg));
	}
	
	public ErrorSTO(String msg) {
		super("",new ErrorType(msg));
	}

	//----------------------------------------------------------------
	//	There are times where it is an error if the STO is not a 
	//	constant or adddressable or something else. However, if
	//	the STO is already an error, nothing should be said. To
	//	supress that error, we would have to check if the STO is
	//	not an ErrorSTO as well as what we want it to be.  Rather
	//	than 2 checks we'll have the ErrorSTO always return true
	//	for every check.  (This is an example of where the
	//	instanceof operator would not have been appropriate.)
	//----------------------------------------------------------------
    public boolean isVar()              { return true; }
    public boolean isConst()            { return true; }
    public boolean isExpr()             { return true; }
    public boolean isFunc()             { return true; }
    public boolean isStructdef()        { return true; }
    public boolean isError()            { return true; }
    public boolean print(ErrorPrinter err) {
    	return ((ErrorType)(super.getType())).print(err);
    }
    
    public String getMsg() {
    	return ((ErrorType)(super.getType())).msg;
    }
    
    public void setMsg(String msg) {
    	((ErrorType)(super.getType())).msg = msg;
    }
	public boolean getIsAddressable()   { return true; }
	public boolean getIsModifiable()    { return true; }
	public boolean isModLValue()        { return true; }
}