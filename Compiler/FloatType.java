
public class FloatType extends NumericType {

	public FloatType(String strName, int size) {
		super(strName, size);
		// TODO Auto-generated constructor stub
	}
	
	public FloatType(String strName) {
		super(strName, 4);
		// TODO Auto-generated constructor stub
	}
	
	public FloatType() {
	super();
	super.setName("float");
	super.setSize(4);
	
	}
	
public boolean isFloat() {
	return true;
}
public boolean isAssignableTo(Type t) { 
		
		if(t instanceof FloatType){
			return true; 
		}
		else return false; 
		
	}

public boolean isAssignableToCast(Type t){
	if(t instanceof BasicType){
		return true; 
	} else 
		return false; 
}
}
 