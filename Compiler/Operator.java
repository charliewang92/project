
abstract class Operator {

	public String name = ""; 
	
STO checkOperands(STO a, STO b) {
	return new ErrorSTO("Missing Operator");
}

public void setName(String s){
	this.name = s; 
}
	

}
	
