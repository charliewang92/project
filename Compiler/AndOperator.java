
public class AndOperator extends BooleanOperator {

	public AndOperator(){
		
	}
	
	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    
	    if(aType instanceof BoolType && bType instanceof BoolType){
	    	if (a instanceof ConstSTO && b instanceof ConstSTO)
	    		return new ConstSTO(a.getName() + "&&" + b.getName(),new BoolType());
	    	else
	    		return new ExprSTO(a.getName() + "&&" + b.getName(),new BoolType());
	    }
	    else if (aType instanceof BoolType) 
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,b.getType().getName(),"&&","bool"));
	    else
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1w_Expr,a.getType().getName(),"&&","bool"));

	}
}
