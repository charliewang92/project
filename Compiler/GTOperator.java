
public class GTOperator extends ComparisonOperator {

	public GTOperator(){
		
	}
	

	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    if (!(aType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,a.getType().getName(),">"));
	    if (!(bType instanceof NumericType))
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr,b.getType().getName(),">"));
	    else
	    	if (a instanceof ConstSTO && b instanceof ConstSTO)
	    		return new ConstSTO(a.getName() + ">" + b.getName(),new BoolType());
	    	else
	    		return new ExprSTO(a.getName() + ">" + b.getName(),new BoolType());

	    
	}
}
