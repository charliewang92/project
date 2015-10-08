import java.util.Vector;

//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

class FuncSTO extends STO
{
	//private Type m_returnType;
	public Vector<STO> paramList = new Vector<STO>();
	public Vector<FuncSTO> funcSTOList = new Vector<FuncSTO>();
	public boolean returnMissing; 
	public boolean returnByRef = false;

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public FuncSTO(String strName)
	{
		super (strName);
		super.setIsModifiable(false);
		super.setIsAddressable(true);
		setReturnType(new VoidType("void",0));
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}
	
	public FuncSTO(String strName, Type type)
	{
		super (strName);
		if (type == null)
			setReturnType(new VoidType("void",0));
		else
			setReturnType(type);
		
		if (type instanceof PointerType) { 
			super.setIsAddressable(true);
			super.setIsModifiable(true);
		}
		else {
			super.setIsAddressable(true);
			super.setIsModifiable(false);		
			}
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isFunc() 
	{ 
		return true;
		// You may want to change the isModifiable and isAddressable                      
		// fields as necessary
	}

	//----------------------------------------------------------------
	// This is the return type of the function. This is different from 
	// the function's type (for function pointers - which we are not 
	// testing in this project).
	//----------------------------------------------------------------
	public void setReturnType(Type typ)
	{
		super.setType(typ);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type getReturnType ()
	{
		return super.getType();
	}

	public boolean isOverloaded() {
		if (funcSTOList.size() > 1)
			return true;
		else
			return false;
	}
	
	public boolean paramExists(Vector<STO> params, ErrorPrinter m_errors, int m_nNumErrors) {
		Vector<STO> curr;
		boolean found_difference = false;
		for (int i = 0 ; i < funcSTOList.size() ; i++) {
			curr = funcSTOList.get(i).paramList;
			if (params.size() == curr.size()) {
					found_difference = false; 
				for (int j = 0; j < params.size() ; j++)
				{
					found_difference = false;
					//added check for const and pointer check 
					if ((!params.get(j).getType().equals(curr.get(j).getType())) || 
							((VarSTO)curr.get(j)).isReference == true && (!params.get(j).getType().equals(curr.get(j).getType()))){
						found_difference = true;
						break;
					}
					
					if (((VarSTO)curr.get(j)).isReference) {
						if (!params.get(j).getIsModifiable()) {
							found_difference = true;
							break;
						}
					}
					/*if (params.get(j) instanceof ConstSTO) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error18_AddressOf,params.get(j).getType().getName()));
						err.print(m_errors);
						m_nNumErrors++;
						found_difference = false;
						break;
					}*/
					
				}
				if (!found_difference)
					return true;
			}
			found_difference = true;
		}
		return false;
	}
	public boolean checkFuncSTO(FuncSTO sto) {
		boolean different = true;
		for(int i = 0; i< funcSTOList.size(); i++) { // for each function
			FuncSTO tmp = funcSTOList.get(i); 
			
			if(tmp.paramList.size() == sto.paramList.size()){
				different = false;
				for(int j = 0; j<tmp.paramList.size(); j++) { // each parameter
					STO origSTO = tmp.paramList.get(j);
					STO newSTO = sto.paramList.get(j);
					if (!origSTO.getType().equals(newSTO.getType())) {
						different = true;
						break;
					}
				}
				if (!different)
					return false;
			}
		}
		return true;
		
	}
	
	public void setReturnByRef(boolean t) {
		returnByRef = t;
	}
	public boolean getReturnByRef() {
		return returnByRef;
	}

	//Finds the correct param and uses it. 
	public STO paramExistsReturn(Vector<STO> params, ErrorPrinter m_errors,
			int m_nNumErrors) {
		Vector<STO> curr;
		FuncSTO correctParam = null; 
		boolean found_difference = false;
		for (int i = 0 ; i < funcSTOList.size() ; i++) {
			curr = funcSTOList.get(i).paramList;
			if (params.size() == curr.size()) {
					found_difference = false; 
					correctParam = funcSTOList.get(i);
				for (int j = 0; j < params.size() ; j++)
				{
					found_difference = false;
					correctParam = funcSTOList.get(i);
					//added check for const and pointer hceck 
					if ((!params.get(j).getType().equals(curr.get(j).getType())) || 
							((VarSTO)curr.get(j)).isReference == true && !(params.get(j).getType() instanceof PointerType)){
						found_difference = true;
						correctParam = null;
						break;
					}
					
					if (((VarSTO)curr.get(j)).isReference) {
						if (!params.get(j).getIsModifiable()) {
							found_difference = true;
							correctParam = null;
							break;
						}
					}
					/*if (params.get(j) instanceof ConstSTO) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error18_AddressOf,params.get(j).getType().getName()));
						err.print(m_errors);
						m_nNumErrors++;
						found_difference = false;
						correctParam = null;
						break;
					}*/
					
				}
				if (!found_difference)
					return correctParam;
			}
			found_difference = true;
		}
		 return correctParam;
	}
}
