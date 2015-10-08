
public class PointerType extends CompositeType {
	public Type type;
	
	public PointerType(){
		super("Pointer", 4); 
	}
	public PointerType(String strName,int size) {
		super(strName, size);
		// TODO Auto-generated constructor stub
	}
	
	
public boolean isAssignableTo(Type t) { 
		return this.equals(t);
	
		/*if(t.isPointer()){
			return true; 
		}
		else return false; */
		
	}

public void setType(Type t) {
	type = t;
	
}

public PointerType getLastPointerType() {
	PointerType curr = this;
	
	while (curr.type instanceof PointerType) {
		curr = (PointerType)curr.type;
	}
	
	return curr;
}

public Type getPointerType() {
	return getLastPointerType().type;
}
public Type getType() {
	return type;
}
	public String getName() {
		Type curr = this;
		String name = "";
			while (curr instanceof PointerType) {
				name += "*";
				curr = ((PointerType) curr).type;
			}
		
			
		if(curr == null){
			return "nullptr" + name.substring(1) ;
		}
		
		return curr.getName() + name;
	}
	
	public boolean equals(Type t) {
		
		if (t instanceof PointerType) {
			PointerType curr = this;
			PointerType other = (PointerType)t;
			
			while (curr.getType() instanceof PointerType && other.getType() instanceof PointerType) {
				curr = (PointerType)curr.getType();
				other = (PointerType)other.getType();
			}
			Type c1 = curr.getType();
			Type c2 = other.getType();
			
			if (c1 == null && c2 != null)
				return false;
			else if (c1 != null && c2 == null)
				return false;
			
			if (c1.equals(c2))
				return true;
			else
				return false;
		}
		return false;
	}

}
