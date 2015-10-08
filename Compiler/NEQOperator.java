
public class NEQOperator extends ComparisonOperator {
	
	public NEQOperator(){
		
	}
	
	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    
	    if(aType instanceof NumericType && bType instanceof NumericType){
	    	if (a instanceof ConstSTO && b instanceof ConstSTO)
	    		return new ConstSTO(a.getName() + "!=" + b.getName(),new BoolType());
	    	else
	    		return new ExprSTO(a.getName() + "!=" + b.getName(),new BoolType());
	    }
	    else if(aType instanceof BoolType && bType instanceof BoolType){
	    	if (a instanceof ConstSTO && b instanceof ConstSTO)
	    		return new ConstSTO(a.getName() + "!=" + b.getName(),new BoolType());
	    	else
	    		return new ExprSTO(a.getName() + "!=" + b.getName(),new BoolType());


	    }
	    else 
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr,a.getType().getName(),"!=",b.getType().getName()));
	}

}
