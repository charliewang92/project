//---------------------------------------------------------------------
// This is the top of the Type hierarchy. You most likely will need to
// create sub-classes (since this one is abstract) that handle specific
// types, such as IntType, FloatType, ArrayType, etc.
//---------------------------------------------------------------------

abstract class Type
{
	// Name of the Type (e.g., int, bool, some structdef, etc.)
	private String m_typeName;
	private int m_size;

	public Type() {
		
	}
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public Type(String strName, int size)
	{
		//System.err.println("strName for type: " + strName);
		setName(strName);
		setSize(size);
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public String getName()
	{
		return m_typeName;
	}
	
	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setName(String str)
	{
		m_typeName = str;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int getSize()
	{
		return m_size;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public void setSize(int size)
	{
		m_size = size;
	}

	public void setType(Type type2) { }
	//----------------------------------------------------------------
	//	It will be helpful to ask a Type what specific Type it is.
	//	The Java operator instanceof will do this, but you may
	//	also want to implement methods like isNumeric(), isInt(),
	//	etc. Below is an example of isInt(). Feel free to
	//	change this around.
	//----------------------------------------------------------------
	
	public boolean  isError()   	{ return this instanceof ErrorType; }
	public boolean  isInt()	   		{ return this instanceof IntType;}
	public boolean 	isNumeric()		{return this instanceof	NumericType;}
	public boolean  isFloat()	    { return this instanceof FloatType; }
	public boolean  isBool()	    { return this instanceof BoolType; }
	public boolean  isArray()	    { return this instanceof ArrayType; }
	public boolean  isStruct()	    { return this instanceof StructType; }
	public boolean  isNullPointerType()	    { return this instanceof NullPointerType; }
	public boolean  isPointer()	    { return this instanceof PointerType; }
	public boolean  isVoid()	    { return this instanceof VoidType; }

	//Can check what types a specific type can be assigned to 
	
	public boolean isAssignableTo(Type t) {
		if (this.equals(t))
			return true;
		else
			return false;
	}
	
	public boolean isAssignableToCast(Type t){
		
		return false;
	}
	
	public boolean isModifiable() { return false; }
	
	public boolean equals(Type t) {
		if (this.getClass().equals(t.getClass())) {
			return true;
		}
		return false;
		
		/*if(t != null){
			if(this instanceof Type){
				
				if(this.getName().equals(t.getName())){
					return true;
				}else{
					return false;
					}
				} else return false;
			
			} else return false;*/
	}
	}


