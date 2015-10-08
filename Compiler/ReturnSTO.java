
public class ReturnSTO extends ExprSTO {

	public ReturnSTO(String strName, STO sto) {
		super(strName, sto == null ? new VoidType(strName, 0) : sto.getType());
		/*if (sto.getType() instanceof ArrayType) {
			NOT SURE WHAT TO DO HERE
		}*/ 
		
		if (sto == null) {
			return;
		}
		if (sto instanceof FuncSTO) {
			if (((FuncSTO)sto).returnByRef) {
				super.setIsModifiable(true);
				super.setIsAddressable(true);
			} else {
				super.setRval(true);
			}
			
		}
		else if (sto instanceof VarSTO) {
			super.setIsModifiable(true);
			super.setIsAddressable(true);
		}
		else if (sto instanceof ConstSTO) {
			super.setIsAddressable(true);
			super.setIsModifiable(false);
		}
		// TODO Auto-generated constructor stub
	}
	

}
