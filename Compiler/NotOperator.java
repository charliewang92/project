
public class NotOperator extends UnaryOperator {
	
	public NotOperator(){
		
	}
	
	public STO checkOperands(STO a, STO b) {
	    Type bType = b.getType();
	    
	    if(bType instanceof BoolType){
	    	if (b instanceof ConstSTO)
	    		return new ConstSTO(b.getName() + "!" + b.getName(),new BoolType());
	    	else {
	    	ExprSTO exp = new ExprSTO(b.getName() + "!" + b.getName(),new BoolType());
	    	exp.isConst = true;
	        return exp; 
	    	}
	    }
	    else {
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1u_Expr,b.getType().getName(),"!","bool"));
	    }
	}
}
