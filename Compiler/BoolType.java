public class BoolType extends BasicType {

	public BoolType() {
		super("bool", 4);
		//System.err.println("Making Bool");
	}

	public BoolType(String strName, int size) {
		super(strName, size);
		//System.err.println("Making Bool");

	}

	public BoolType(String strName) {
		super(strName, 4);
		//System.err.println("Making Bool");

	}

	public boolean isAssignableTo(Type t) {

		if (t instanceof BoolType) {
			return true;
		} else
			return false;

	}
	public boolean equals(Type t) {
		if (t instanceof BoolType)
			return true;
		else
			return false;
	}
	public boolean isAssignableToCast(Type t){
		
		if(t instanceof BasicType){
			return true; 
		} else 
			return false; 
	}

}
