import java.math.BigDecimal;


public class ModOperator extends ArithmeticOperator {

	public ModOperator(){
		
	}

	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    if (!(aType instanceof IntType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,a.getType().getName(),"%","int"));
	    if (!(bType instanceof IntType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,b.getType().getName(),"%","int"));
	    if (a instanceof ConstSTO && b instanceof ConstSTO) {
	    	ConstSTO bCurr = (ConstSTO)b;
	    	if (bCurr.m_value.equals(new BigDecimal(0)))
    			return new ErrorSTO(ErrorMsg.error8_Arithmetic);
    		BigDecimal tmp;
    		try {
    			tmp = (((ConstSTO)a).getValue().remainder(((ConstSTO)b).getValue()));
    		} catch (ArithmeticException e) {
    			return new ErrorSTO(ErrorMsg.error8_Arithmetic);
    		} catch (Exception e) {
    			tmp = new BigDecimal(0);
    		}
    		
    		if (aType instanceof IntType && bType instanceof IntType) {
		    	return new ConstSTO(tmp.toString(),new IntType(),tmp.intValue());
	    	}
	    }
	    if (aType instanceof IntType && bType instanceof IntType)
	        return new ExprSTO(a.getName() + "%" + b.getName(),new IntType());
	    else {
	        return new ExprSTO(a.getName() + "%" + b.getName(),new FloatType());
	    }
	}
}
