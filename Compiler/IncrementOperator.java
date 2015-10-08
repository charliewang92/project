
public class IncrementOperator extends UnaryOperator {

	public IncrementOperator() {
		// TODO Auto-generated constructor stub
	}
	
	public STO checkOperands(STO a) {
	    Type aType = a.getType();
	    STO sto;
	    if (a instanceof ErrorSTO) {
	    	//System.out.println("hmm");
	    	return a;
	    }
	    if (a.getType() instanceof ErrorType) {
	    	//System.out.println("HELLA BAD");
	    }
	    if (!(aType instanceof NumericType) && !(aType instanceof PointerType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type,a.getType().getName(),"++"));
	    if (!(a.getIsModifiable()))
	    	return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Lval,"++"));
	    else if (aType instanceof IntType) 
	    	sto = new ExprSTO("++" + a.getName(),new IntType());
	    else if (aType instanceof FloatType)
	    	sto = new ExprSTO("++" + a.getName(),new FloatType());
	    else if (aType instanceof PointerType)
	    	sto = new ExprSTO("++" + a.getName(),new PointerType());
	    else
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type,a.getType().getName(),"++"));
	    
	    sto.setIsAddressable(false);
	    sto.setIsModifiable(false);
	    return sto;
	}
}
