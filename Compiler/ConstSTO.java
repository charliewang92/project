//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

import java.math.BigDecimal;

class ConstSTO extends STO
{
    //----------------------------------------------------------------
    //	Constants have a value, so you should store them here.
    //	Note: We suggest using Java's BigDecimal class, which can hold
    //	floats and ints. You can then do .floatValue() or 
    //	.intValue() to get the corresponding value based on the
    //	type. Booleans/Ptrs can easily be handled by ints.
    //	Feel free to change this if you don't like it!
    //----------------------------------------------------------------
    public BigDecimal		m_value = new BigDecimal(0);
    public boolean isLiteral = false; 

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public ConstSTO(String strName)
	{
		super(strName);
		super.setIsModifiable(false);
		super.setIsAddressable(true);

		m_value = null; // fix this
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
	}

	public ConstSTO(String strName, Type typ)
	{
		super(strName, typ);
		super.setIsModifiable(false);
		super.setIsAddressable(true);
		m_value = null; // fix this
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
	}

	public void negate() {
		if (m_value != null)
			m_value = m_value.negate();
	}
	
	public BigDecimal getBD(){
		BigDecimal bg = new BigDecimal(m_value.doubleValue());
		return bg; 
	}
	public void negateInt() {
		if(this.getType() instanceof IntType){
			double val = m_value.intValue();
			val = val * -1; 
			m_value = new BigDecimal(val); 
		}
	}
	
	public void negateFloat(){
		if(this.getType() instanceof FloatType){
			double val = m_value.floatValue();
			val = val * -1; 
			m_value = new BigDecimal(val); 
		}
	}
	
	public ConstSTO(String strName, Type typ, int val)
	{
		super(strName, typ);
		super.setIsModifiable(false);
		super.setIsAddressable(true);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
	}

	public ConstSTO(String strName, Type typ, double val)
	{
		super(strName, typ);
		super.setIsModifiable(false);
		super.setIsAddressable(true);
		m_value = new BigDecimal(val);
		// You may want to change the isModifiable and isAddressable
		// fields as necessary
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean isConst() 
	{
		return true;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public BigDecimal getValue() 
	{
		return m_value;
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public int getIntValue() 
	{
		return m_value.intValue();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public float getFloatValue() 
	{
		return m_value.floatValue();
	}

	//----------------------------------------------------------------
	//
	//----------------------------------------------------------------
	public boolean getBoolValue() 
	{
		return !BigDecimal.ZERO.equals(m_value);
	}
}
