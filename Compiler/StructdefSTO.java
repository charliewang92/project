import java.util.Vector;

//---------------------------------------------------------------------
// For structdefs
//---------------------------------------------------------------------

class StructdefSTO extends STO
{
	public Vector<STO> funcVector = new Vector<STO>(); 
	public Vector<STO> varVector = new Vector<STO>();
	public Vector<STO> ctorDtorVector = new Vector<STO>(); 
	
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public StructdefSTO(String strName)
	{
		super(strName);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		super.setType(new StructType(strName));
	}

	public StructdefSTO(String strName, Scope s)
	{
		super(strName);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		super.setType(new StructType(strName));

	}
	public StructdefSTO(String strName, Type typ)
	{
		super(strName, typ);
		super.setIsModifiable(true);
		super.setIsAddressable(true);
		super.setType(new StructType(strName));

	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isStructdef()
	{
		return true;
	}
	
	public STO structVariableExists(String id) {
		for (int i = 0 ; i < funcVector.size() ; i++) {
			if (funcVector.get(i).getName().equals(id))
				return funcVector.get(i);
		}
		return null;
	}
}
