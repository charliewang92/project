import java.math.BigDecimal;


public class MultiplyOperator extends ArithmeticOperator {

	public MultiplyOperator(){
		
	}
	
	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    if (!(aType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,a.getType().getName(),"*"));
	    if (!(bType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,b.getType().getName(),"*"));
	    if (a instanceof ConstSTO && b instanceof ConstSTO) {
	    	BigDecimal tmp;
	    	try {
	    		 tmp = (((ConstSTO)a).getValue().multiply(((ConstSTO)b).getValue()));
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
	        return new ExprSTO(a.getName() + "*" + b.getName(),new IntType());
	    else {
	        return new ExprSTO(a.getName() + "*" + b.getName(),new FloatType());
	    }
	
		}
}
