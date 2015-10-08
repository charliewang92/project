
public class NullPointerType extends PointerType {

	public NullPointerType(String strName, int size) {
		super(strName,size);
		// TODO Auto-generated constructor stub
	}
	
public boolean isAssignableTo(Type t) { 
		
		if(t instanceof PointerType){
			return true; 
		}
		else return false; 
		
	}

}
