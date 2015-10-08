import java.util.Vector;

public class StructType extends CompositeType {

	public Scope scope;

	public StructType(String strName, int size) {
		super(strName, size);
		// TODO Auto-generated constructor stub
	}

	public StructType(String strName) {
		super(strName, 0);

	}

	public StructType(String strName, Vector<STO> fieldVarVector,
			Vector<STO> ctorDtorVector, Vector<STO> fieldFuncVector) {
		super(strName, 0);
	}

	public boolean isAssignableTo(Type t) {

		if (t.isStruct()) {
			return true;
		} else
			return false;

	}

	public boolean equals(Type t) {
		if (t instanceof StructType) {
			StructType other = (StructType) t;
			if (other.getName().equals(this.getName())) {
				if (other.scope == this.scope) {
					return true;
				} else
					return false;
			}
		}
		return false;
	}

	public boolean isModifiable() {
		return true;
	}

}
