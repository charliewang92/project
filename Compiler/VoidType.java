
public class VoidType extends Type {

	public VoidType(String strName, int size) {
		super(strName, size);
		// TODO Auto-generated constructor stub
	}

public boolean isAssignableTo(Type t) { 
	if (t instanceof VoidType)
		return true;
			return false; 
	}
}
