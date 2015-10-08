import java.math.BigDecimal;

public class ArrayType extends CompositeType {

	private Type type;
	public int arraySize;

	private static BigDecimal getConstVal(STO a) {
		ConstSTO con = (ConstSTO) a;

		return con.m_value;
	}

	public ArrayType(STO a) {
		super("array", 0);
		type = checkConstExprSTO(a);
	}

	private Type checkConstExprSTO(STO a) {
		ErrorType err = new ErrorType(a.getName());
		if (a.getType() instanceof ErrorType)
			return a.getType();

		if (a.isConst()) {
			if (a.getType() instanceof IntType) {
				int val = ((ConstSTO) a).getValue().intValue();
				if (val > 0) {
					arraySize = val;
					return null;
				} else {
					err.msg = Formatter.toString(ErrorMsg.error10z_Array, val);
				}
			} else {
				err.msg = Formatter.toString(ErrorMsg.error10i_Array, a
						.getType().getName());
			}
		} else {
			err.msg = (ErrorMsg.error10c_Array);
		}

		return err;
	}

	public ArrayType(String strName, int size) {
		super(strName, size * 4);
		// TODO Auto-generated constructor stub
	}

	public ArrayType(String strName, Type arrayType, int size) {
		super(strName, size * 4);
		type = arrayType;

		// TODO Auto-generated constructor stub
	}

	public Type getArrayType() {
		ArrayType curr;
		if (type instanceof ErrorType)
			return type;
		if (type != null) {
			if (type instanceof ArrayType) {

				curr = (ArrayType) type;

				while (curr.getType() instanceof ArrayType) {
					curr = (ArrayType) curr.getType();
				}

				if (curr.getType() instanceof ErrorType)
					return curr.getType();

				return curr;
			} else {
				return this;
			}
		} else {
			return this;
		}
	}

	public Type getType() {
		return type;
	}

	public String getName(){
		Type curr = null; 
		curr = type; 
		String tmp = ((ArrayType)getArrayType()).getType().getName();
		StringBuilder sb = new StringBuilder("[" + arraySize + "]"); 
		StringBuilder actual = new StringBuilder(); 
		
		while(curr != null){
			
			if(curr instanceof ArrayType){
				ArrayType at = (ArrayType) curr; 
				sb.append("[" + at.arraySize + "]"); 
				curr = ((ArrayType) curr).type; 
			}
			else{
				actual.append(tmp + sb.toString());
				curr = null; 
			}
		}
		//actual.append(tmp + sb.toString());

		return actual.toString(); 
	}
	
	public boolean isAssignableTo(Type t) {
			return this.equals(t);
	}

	public void setType(STO sto) {
		if (!(type instanceof ErrorType)) {
			type = sto.getType();
		}
	}

	public void setType(Type typ) {
		if (!(type instanceof ErrorType)) {
			type = typ;
		}
	}

	public void calcArraySize() {
		super.setSize(this.arraySize * 4);
	}
	
	public boolean equals(Type t) {
		if (t instanceof ArrayType) {
			ArrayType curr = this;
			ArrayType other = (ArrayType)t;
			if (curr.arraySize != other.arraySize)
				return false;
			while (curr.getType() instanceof ArrayType && other.getType() instanceof ArrayType) {
				curr = (ArrayType) curr.getType();
				other = (ArrayType) other.getType();
				
				if (curr.arraySize != other.arraySize)
					return false;
			}
			Type c1 = curr.getType();
			Type c2 = other.getType();
			if (c1.equals(c2))
				return true;
			else 
				return false;

		}
		return false;
	}
}
