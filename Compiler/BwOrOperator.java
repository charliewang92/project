
public class BwOrOperator extends BitwiseOperator {

	public BwOrOperator(){
		
	}
	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    if ((aType instanceof IntType && bType instanceof IntType))
	    	if (a instanceof ConstSTO && b instanceof ConstSTO) {
	    		ConstSTO ca = (ConstSTO)a;
	    		ConstSTO cb = (ConstSTO)b;
	    		return new ConstSTO(a.getName() + "|" + b.getName(),new IntType(),ca.m_value.intValue() | cb.m_value.intValue());
	    	}
	    	else
	    		return new ExprSTO(a.getName() + "|" + b.getName(),new IntType());
	    else if (aType instanceof IntType)
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,b.getType().getName(),"|","int"));
	    else
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,a.getType().getName(),"|","int"));

	}
}
