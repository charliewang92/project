//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class ExprSTO extends STO
{
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public ExprSTO(String strName)
	{
		super(strName);
        // You may want to change the isModifiable and isAddressable
        // fields as necessary
	}

	public ExprSTO(String strName, Type typ)
	{
		super(strName, typ);
		//System.err.println("Type is: " + typ.getName());

        // You may want to change the isModifiable and isAddressable
        // fields as necessary
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isExpr()
	{
		return true;
	}
}
