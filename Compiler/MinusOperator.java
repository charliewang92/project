import java.math.BigDecimal;


public class MinusOperator extends ArithmeticOperator {

	public MinusOperator(){
		
	}
	
	
	public STO checkOperands(STO a, STO b) {
		if (a instanceof ErrorSTO) 
			return a;
		
		if (b instanceof ErrorSTO)
			return b;
	
		if (a.getType() instanceof ErrorType) {
			ErrorType err = (ErrorType)a.getType();
			return new ErrorSTO(err.msg);
		}
		
		if (b.getType() instanceof ErrorType) {
			ErrorType err = (ErrorType)b.getType();
			return new ErrorSTO(err.msg);
		}
		
	    Type aType = a.getType();
	    Type bType = b.getType();
		
	    if (!(aType instanceof NumericType)) {
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,a.getType().getName(),"-"));
	    }
	    if (!(bType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,b.getType().getName(),"-"));
	    if (a instanceof ConstSTO && b instanceof ConstSTO) {
	    	BigDecimal tmp;
	    	try {
    		tmp = (((ConstSTO)a).getValue().subtract(((ConstSTO)b).getValue()));
	    	} catch (Exception e) {
	    		tmp = new BigDecimal(0);
	    	}
	    	if (aType instanceof IntType && bType instanceof IntType) {
		    	return new ConstSTO(tmp.toString(),new IntType(),tmp.intValue());
	    	}
	    	else
	    		return new ConstSTO(tmp.toString(),new FloatType(),tmp.intValue());
	    }
	    if (aType instanceof IntType && bType instanceof IntType)
	        return new ExprSTO(a.getName() + "-" + b.getName(),new IntType());
	    else {
	        return new ExprSTO(a.getName() + "-" + b.getName(),new FloatType());
	    }
	}
}
