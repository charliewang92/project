
public class IntType extends NumericType {

	
	public IntType(){
		super("int",4); 
		super.setName("int");
	}
	
	public IntType(String strName, int size) {
		super(strName, size);
		
	}
	
	public IntType(String strName){
		super(strName, 4); 
	}
	
	public boolean isAssignableTo(Type t) { 
		
		if(t instanceof IntType){
			return true; 
		}
		else if(t instanceof FloatType){
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
