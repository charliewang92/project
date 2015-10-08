
public abstract class UnaryOperator extends Operator {

	public UnaryOperator() {
		// TODO Auto-generated constructor stub
	}
	
	public STO checkOperands(STO a) {
			return new ErrorSTO("TEST");
	        //return new ErrorSTO(Formatter.toString(ErrorMsg.error2_Type,a.getType().getName(),"ll"));
	}

}
