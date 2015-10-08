import java.math.BigDecimal;


public class DivideOperator extends ArithmeticOperator {

	public DivideOperator(){
		
	}
	
	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    	
	    
	    if (!(aType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,a.getType().getName(),"/"));
	    if (!(bType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,b.getType().getName(),"/"));
	    
	    if (b instanceof ConstSTO) {
	    	ConstSTO c = (ConstSTO)b;
	    	if (c.getValue().equals(new BigDecimal(0))) {
	    		return new ErrorSTO(ErrorMsg.error8_Arithmetic);
	    	}
	    }
	    
	    if (a instanceof ConstSTO && b instanceof ConstSTO) {
    		float tmp;
    		try {
    			tmp = (((ConstSTO)a).getValue().floatValue() / ((ConstSTO)b).getValue().floatValue());
    		} catch (ArithmeticException e) {
    			return new ErrorSTO(ErrorMsg.error8_Arithmetic);
    		} catch (Exception e) {
    			tmp = 0;
    		}
    		if (tmp == Double.POSITIVE_INFINITY)
    			return new ErrorSTO(ErrorMsg.error8_Arithmetic);
    		
	    	if (aType instanceof IntType && bType instanceof IntType) {
		    	return new ConstSTO(Integer.toString((int)tmp),new IntType(),(int)tmp);
	    	}
	    	else
	    		return new ConstSTO(Float.toString(tmp),new FloatType(),tmp);
	    }
	    if (aType instanceof IntType && bType instanceof IntType)
	        return new ExprSTO(a.getName() + "/" + b.getName(),new IntType());
	    else {
	        return new ExprSTO(a.getName() + "/" + b.getName(),new FloatType());
	    }
	}
}
