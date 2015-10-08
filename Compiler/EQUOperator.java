
public class EQUOperator extends ComparisonOperator {

	public EQUOperator(){
	}

	public STO checkOperands(STO a, STO b) {
	    Type aType = a.getType();
	    Type bType = b.getType();
	    
	    if(aType instanceof NumericType && bType instanceof NumericType){
	        return new ExprSTO(a.getName() + "==" + b.getName(),new BoolType());
	    }
	    else if(aType instanceof BoolType && bType instanceof BoolType){
	        return new ExprSTO(a.getName() + "==" + b.getName(),new BoolType());

	    }
	    else 
	        return new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr,a.getType().getName(),"==",b.getType().getName()));
	}

}
