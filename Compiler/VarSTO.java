	//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class VarSTO extends STO
{
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	
	//To Keep track if & was used with it
	public boolean isReference; 
	
	public VarSTO(String strName)
	{
		super(strName);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		isReference = false;
		// You may want to change the isModifiable and isAddressable 
		// fields as necessary
	}

	public VarSTO(String strName, Type typ)
	{
		super(strName, typ);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		isReference = false; 
		// You may want to change the isModifiable and isAddressable 
		// fields as necessary
	}
	public void negate() {
		return;
	}
	public VarSTO(String strName, Type typ, String reference)
	{
		super(strName, typ);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		
		if(reference != null){	
			isReference = true; 
		}
		else{
			isReference = false;
		}
		// You may want to change the isModifiable and isAddressable 
		// fields as necessary
	}
	
	public VarSTO(STO sto, Type typ){
		super(sto.getName(), typ);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
	}
	
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isVar() 
	{
		return true;
	}
}
