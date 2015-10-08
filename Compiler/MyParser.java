//---------------------------------------------------------------------
//
//---------------------------------------------------------------------

import java_cup.runtime.*;

import java.math.BigDecimal;
import java.util.Vector;

class MyParser extends parser {
	private Lexer m_lexer;
	private ErrorPrinter m_errors;
	private int m_nNumErrors;
	private String m_strLastLexeme;
	private boolean m_bSyntaxError = true;
	private int m_nSavedLineNum;
	FuncSTO currFuncSTO;
	public StructdefSTO structKeeper;
	private int codeBlockCounter = 0;

	private SymbolTable m_symtab;

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public MyParser(Lexer lexer, ErrorPrinter errors) {
		m_lexer = lexer;
		m_symtab = new SymbolTable();
		m_errors = errors;
		m_nNumErrors = 0;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public boolean Ok() {
		return m_nNumErrors == 0;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public Symbol scan() {
		Token t = m_lexer.GetToken();

		// We'll save the last token read for error messages.
		// Sometimes, the token is lost reading for the next
		// token which can be null.
		m_strLastLexeme = t.GetLexeme();

		switch (t.GetCode()) {
		case sym.T_ID:
		case sym.T_ID_U:
		case sym.T_STR_LITERAL:
		case sym.T_FLOAT_LITERAL:
		case sym.T_INT_LITERAL:
			return new Symbol(t.GetCode(), t.GetLexeme());
		default:
			return new Symbol(t.GetCode());
		}
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void syntax_error(Symbol s) {
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void report_fatal_error(Symbol s) {
		m_nNumErrors++;
		if (m_bSyntaxError) {
			m_nNumErrors++;

			// It is possible that the error was detected
			// at the end of a line - in which case, s will
			// be null. Instead, we saved the last token
			// read in to give a more meaningful error
			// message.
			m_errors.print(Formatter.toString(ErrorMsg.syntax_error,
					m_strLastLexeme));
		}
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void unrecovered_syntax_error(Symbol s) {
		report_fatal_error(s);
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void DisableSyntaxError() {
		m_bSyntaxError = false;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void EnableSyntaxError() {
		m_bSyntaxError = true;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public String GetFile() {
		return m_lexer.getEPFilename();
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public int GetLineNum() {
		return m_lexer.getLineNumber();
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public void SaveLineNum() {
		m_nSavedLineNum = m_lexer.getLineNumber();
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	public int GetSavedLineNum() {
		return m_nSavedLineNum;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoProgramStart() {
		// Opens the global scope.
		m_symtab.openScope();
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoProgramEnd() {
		m_symtab.closeScope();
	}

	void DoForEachCheck(String id, STO expr) {
		if (m_symtab.access(id) == null) {
			//System.out.println("Huge error");
			return;
		}
		VarSTO iterVar = (VarSTO) m_symtab.access(id);
		VarSTO arrayVar = (VarSTO) m_symtab.access(expr.getName());
		ArrayType arrType;
		Type finalArrayType;
		if (arrayVar.getType() instanceof ArrayType) {
			arrType = (ArrayType) arrayVar.getType();
			arrType = (ArrayType) arrType.getArrayType();
			finalArrayType = arrType.getType();
		} else {
			m_nNumErrors++;
			m_errors.print(ErrorMsg.error12a_Foreach);
			return;
		}

		if (iterVar.isReference) {
			if (finalArrayType.equals(iterVar.getType())) {
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error12r_Foreach,
						finalArrayType.getName(), id, iterVar.getType()
								.getName()));
			}
		} else {
			if (!finalArrayType.isAssignableTo(iterVar.getType())) {

				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error12v_Foreach,
						finalArrayType.getName(), id, iterVar.getType()
								.getName()));
			}
		}

	}

	void DoVarDecl_Struct(String id, Type type, STO arrSTO, Vector<STO> exprList) {
		if (!(type instanceof StructType)) {
			// throw error
		}
		StructdefSTO structSTO = null;
		FuncSTO ctorSTO = null;
		if (m_symtab.access(id) != null) {
			// redeclared var
			return;
		}

		if (m_symtab.access(type.getName()) != null
				&& m_symtab.access(type.getName()) instanceof StructdefSTO)
			structSTO = (StructdefSTO) m_symtab.access(type.getName());
		else {
			// throw error
		}
		Scope structScope = null;
		
		if (structSTO != null)
			structScope = ((StructType) structSTO.getType()).scope;
		else
			return;
		
		if (structScope.accessLocal(structSTO.getName()) == null
				|| !(structScope.accessLocal(structSTO.getName()) instanceof FuncSTO)) {
			// throw error
		} else {
			ctorSTO = (FuncSTO) structScope.accessLocal(structSTO.getName());
		}

		STO res = DoFuncCall_Struct(ctorSTO, exprList, structScope);

		if (res instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) res;
			if (err.print(m_errors))
				m_nNumErrors++;
			m_symtab.insert(new VarSTO(id, structSTO.getType()));

			return;
		} else {
			m_symtab.insert(new VarSTO(id, structSTO.getType()));
			return;
		}
		/*
		 * for (int i = 0 ; i < ctorSTO.funcSTOList.size() ; i++) { curr =
		 * (FuncSTO)ctorSTO.funcSTOList.get(i); if (curr.paramExists(exprList))
		 * { m_symtab.insert(new VarSTO(id,structSTO.getType())); return; } }
		 */

	}

	// ----------------------------------------------------------------
	// So far done without a null or regular pointer type
	// ----------------------------------------------------------------
	void DoVarDecl(String id, Type type, STO arrSTO, STO initSTO) {
		boolean isArray = false;
		int size = 0;
		VarSTO arraySTO = null;

		/**
		 * if (arrSTO != null) { ArrayType typ = (ArrayType) arrSTO.getType();
		 * ArrayType tmp = (ArrayType) typ.getArrayType(); tmp.setType(type);
		 * type = arrSTO.getType(); }
		 */

		// If we already initialized this value
		if (m_symtab.accessLocal(id) != null) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		if (arrSTO instanceof VarSTO) {
			arraySTO = (VarSTO) arrSTO;
		} else if (arrSTO instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) arrSTO;
			if (err.print(m_errors))
				m_nNumErrors++;
			return;
		}

		// Checking for assignment of ErrorType
		if (initSTO instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) initSTO;
			if (err.print(m_errors))
				m_nNumErrors++;
			err.setName(id);
			m_symtab.insert(err);
			return;
		}

		// Checking for initialization type such as: int x = 3.55
		if (initSTO != null && !(initSTO.getType().isAssignableTo(type))) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error8_Assign, initSTO.getType().getName(),
					type.getName()),id);
			if (err.print(m_errors))
				m_nNumErrors++;
			m_symtab.insert(err);
			return;
		}

		// Check to see if we are handling an array
		if (arraySTO != null) {
			if (arraySTO.getType() instanceof ArrayType) // Is STO contain array
			{
				ArrayType myArray = (ArrayType) arraySTO.getType(); // Get first
																	// array
																	// type
				// Type lastType = (ArrayType)myArray.getArrayType(); // get
				// most bottom array type

				if (myArray.getArrayType() instanceof ErrorType) { // if it
																	// returns
																	// an error,
																	// print it
					ErrorType err = (ErrorType)myArray.getArrayType();
					if (err.print(m_errors));
						m_nNumErrors++;
					return;
				} else { // else set the bottom-most array-type to variable
							// type.
					myArray.getArrayType().setType(type);
					arraySTO.setName(id);
					arraySTO.setIsModifiable(false);
					arraySTO.setIsAddressable(true);
					m_symtab.insert(arraySTO); // add to sym tab
					return;
				}
			}
		}
		// Handling the IntType
		else if (type.isInt()) {

			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new IntType(),
						size));
				m_symtab.insert(sto);
				return;
			}

			VarSTO sto = new VarSTO(id, new IntType());
			m_symtab.insert(sto);
			return;
			// Handling the FloatType
		} else if (type.isFloat()) {
			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new FloatType(),
						size));
				m_symtab.insert(sto);
				return;
			}
			VarSTO sto = new VarSTO(id, new FloatType());
			m_symtab.insert(sto);
			return;

			// Handling the BoolType
		} else if (type.isBool()) {

			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new BoolType(),
						size));
				m_symtab.insert(sto);
				return;
			}

			VarSTO sto = new VarSTO(id, new BoolType());
			m_symtab.insert(sto);
			return;
		}

		// Handling the StructType
		else if (type.isStruct()) {
			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id,
						new StructType(id), size));
				m_symtab.insert(sto);
				return;
			}
			VarSTO sto = new VarSTO(id, type);
			m_symtab.insert(sto);
			return;
		} else if (type.isPointer()) {
			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, type, size));
				m_symtab.insert(sto);
				return;
			}
			VarSTO sto = new VarSTO(id, type);
			m_symtab.insert(sto);
			return;
		}

		// /Adding more type handlers later on.....//////

		// If not assignable at all because there are no types of this
		else {
			ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.not_type,
					type.getName()), id);
			err.print(m_errors);
			m_nNumErrors++;
			m_symtab.insert(err);
		}

	}

	// ----------------------------------------------------------------
	// DoVarDecl_Struct designed to declare a field variable of Struct
	// ----------------------------------------------------------------
	STO DoVarDeclStruct(String id, Type type, VarSTO arraySTO) {
		boolean isArray = false;
		int size = 0;

		// Check to see if we are handling an array
		if (arraySTO != null) {
			if (arraySTO.getType() instanceof ArrayType) // Is STO contain array
			{
				ArrayType myArray = (ArrayType) arraySTO.getType(); // Get first
																	// array
																	// type
				// Type lastType = (ArrayType)myArray.getArrayType(); // get
				// most bottom array type

				if (myArray.getArrayType() instanceof ErrorType) { // if it
																	// returns
																	// an error,
																	// print it
					m_nNumErrors++;
					return arraySTO;
				} else { // else set the bottom-most array-type to variable
							// type.
					myArray.getArrayType().setType(type);
					arraySTO.setName(id);
					structKeeper.funcVector.addElement(arraySTO);
					return arraySTO; // add to sym tab
				}
			}
		}
		// Handling the IntType
		else if (type.isInt()) {

			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new IntType(),
						size));
				structKeeper.funcVector.addElement(sto);
				return sto;
			}

			VarSTO sto = new VarSTO(id, new IntType());
			structKeeper.funcVector.addElement(sto);
			return sto;

			// Handling the FloatType
		} else if (type.isFloat()) {
			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new FloatType(),
						size));
				structKeeper.funcVector.addElement(sto);
				return sto;
			}
			VarSTO sto = new VarSTO(id, new FloatType());
			structKeeper.funcVector.addElement(sto);
			return sto;

			// Handling the BoolType
		} else if (type.isBool()) {

			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, new BoolType(),
						size));
				structKeeper.funcVector.addElement(sto);
				return sto;
			}

			VarSTO sto = new VarSTO(id, new BoolType());
			structKeeper.funcVector.addElement(sto);
			return sto;
		}

		// Handling the StructType
		else if (type.isStruct()) {

			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id,
						new StructType(id), size));
				structKeeper.funcVector.addElement(sto);
				return sto;
			}

			VarSTO sto = new VarSTO(id, new StructType(id));
			structKeeper.funcVector.addElement(sto);
			return sto;
		} else if (type.isPointer()) {
			if (isArray) {
				VarSTO sto = new VarSTO(id, new ArrayType(id, type, size));
				structKeeper.funcVector.addElement(sto);
				return sto;
			}
			VarSTO sto = new VarSTO(id, type);
			structKeeper.funcVector.addElement(sto);
			return sto;
		}
		// /Adding more type handlers later on.....//////

		// If not assignable at all because there are no types of this
		m_nNumErrors++;
		return new ErrorSTO("", id);
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoExternDecl(String id) {
		if (m_symtab.accessLocal(id) != null) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
		}

		VarSTO sto = new VarSTO(id);
		m_symtab.insert(sto);
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoConstDecl(String id, Type typ, STO expr) {
		ConstSTO sto;
		if (expr instanceof ErrorSTO) {
			m_nNumErrors++;
			return;
		}
		if (!(expr instanceof ConstSTO)) {
			// changed this to handle the case where a non-const assigned to
			// const
			if (!(expr instanceof ErrorSTO)) {
				ErrorSTO sto1 = new ErrorSTO(Formatter.toString(ErrorMsg.error8_CompileTime,id), id);
				sto1.print(m_errors);
				m_nNumErrors++;
				m_symtab.insert(sto1);
				return;
			}
			// return;
		}
		ConstSTO curr = (ConstSTO)expr;
		if (!curr.getType().isAssignableTo(typ)) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error8_Assign, curr
					.getType().getName(), typ.getName()));
			return;
		}

		if (m_symtab.accessLocal(id) != null) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
			return;
		}

		else {
			if (curr.getType() instanceof IntType) {
				try {
					sto = new ConstSTO(id, typ,
							(curr.m_value).intValue());
				} catch (NumberFormatException e) {
					sto = new ConstSTO(id, typ);
				}
			} else if (curr.getType() instanceof FloatType) {
				try {
					sto = new ConstSTO(id, typ,
							(curr.m_value).floatValue());
				} catch (NumberFormatException e) {
					sto = new ConstSTO(id, typ);
				}
			} else {
				sto = new ConstSTO(id, typ);
			}

			m_symtab.insert(sto);
		}
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoStructdefDecl(String id, Scope s, Vector<STO> StructVarList,
			Vector<STO> CtorDtorList, Vector<STO> FieldFuncList, STO struct) {
		int sizeCounter = 0; 
		// StructType typ = new StructType(id, fieldVarVector, CtorDtorVector,
		// FieldFuncVector);
		// Check if the struct has already been declared

		((StructType) struct.getType()).scope = s;
		for(int i = 0; i<s.m_lstLocals.size(); i++){
			STO tmpSTO = s.m_lstLocals.get(i); 
			if(tmpSTO instanceof VarSTO){
				sizeCounter++; 
			}
		}
		int size = sizeCounter * 4; 
		struct.getType().setSize(size);
				
		/**
		 * if (m_symtab.accessLocal(id) != null) { m_nNumErrors++;
		 * m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id)); }
		 */
		// Inserting field variables
		// Checking to make sure that all the STOs declared are unique
		/**
		 * if(StructVarList != null){ for(int i = 0; i< StructVarList.size();
		 * i++){ STO tmp = StructVarList.get(i); if(s.access(tmp.getName()) ==
		 * null){ s.InsertLocal(tmp); } else{ m_nNumErrors++;
		 * m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct,
		 * tmp.getName())); } } }
		 */

		/**
		 * //Inserting Constructor/Destructor if(CtorDtorList != null){ for(int
		 * i = 0; i<CtorDtorList.size(); i++){ STO tmp = CtorDtorList.get(i);
		 * if(tmp != null){ if(tmp.getName().charAt(0) == '~'){ String dtorName
		 * = tmp.getName().substring(0); if(dtorName.equals(id)){
		 * s.InsertLocal(tmp); } //checks for Destructor else{ m_nNumErrors++;
		 * m_errors.print(Formatter.toString(ErrorMsg.error13b_Dtor, dtorName));
		 * } } else if(tmp.getName().equals(id)){ s.InsertLocal(tmp); } //checks
		 * for constructor else{ m_nNumErrors++;
		 * m_errors.print(Formatter.toString(ErrorMsg.error13b_Ctor,
		 * tmp.getName())); } } } }
		 **/

		/**
		 * //Inserting functions into struct if(FieldFuncList != null){ for(int
		 * i = 0; i<FieldFuncList.size(); i++){ STO tmp = FieldFuncList.get(i);
		 * if(tmp != null){ if(s.access(tmp.getName()) != null){ m_nNumErrors++;
		 * m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct,
		 * tmp.getName())); } if(tmp.getName() != id){ s.InsertLocal(tmp); }
		 * else{ m_nNumErrors++;
		 * m_errors.print("Function should not have same name as struct"); } } }
		 * }
		 **/
	}

	// ----------------------------------------------------------------
	// inserting struct
	// after the scope as closed
	//
	// ----------------------------------------------------------------
	void insertStruct(STO struct) {

		if (struct != null) {
			if (m_symtab.access(struct.getName()) == null)
				m_symtab.insert(struct);
			else 
				m_errors.print(Formatter.toString(ErrorMsg.redeclared_id,struct.getName()));
		}
	}
	void DoFuncDecl_1_DTOR(String id) {
		FuncSTO sto = new FuncSTO(id,null);
		m_symtab.openScope();
		m_symtab.setFunc(sto);
	}
	// ----------------------------------------------------------------
	// check foo
	// check not a function
	//
	// ----------------------------------------------------------------
	void DoFuncDecl_1(String id, Type type, String reference) {

		FuncSTO sto = new FuncSTO(id, type);
		if (reference != null) {
			sto.setIsModifiable(true);

			sto.setReturnByRef(true);
		} else {
			sto.setRval(true);
			sto.setReturnByRef(false);
		}

		if (m_symtab.accessLocal(id) == null) {
			m_symtab.insert(sto);
		/*} else if (id.charAt(0) == '~') {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, id));*/
		} else if (m_symtab.accessLocal(id) instanceof VarSTO) {
			if(structKeeper != null){
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error13a_Struct, id));
				return;
			}
			else{
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, id));
			return;
			}
		}

		m_symtab.openScope();
		m_symtab.setFunc(sto);
	}

	// ----------------------------------------------------------------
	// Regular Function Declarations for settingup functions
	// ----------------------------------------------------------------
	void DoFuncDecl_2(Vector<STO> vector, String funcName) {

		STO functionSTO = m_symtab.getFunc();

		// m_symtab.access(funcName);

		STO curr;
		boolean return_found = false;

		if (functionSTO instanceof VarSTO) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.redeclared_id, funcName));
			return;
		}

		if (functionSTO == null) {
			return;
		}

		for (int i = 0; i < vector.size(); i++) {
			curr = vector.get(i);

			if (curr instanceof ReturnSTO) {
				return_found = true;
				DoReturnStmtChecks((ReturnSTO) curr, (FuncSTO) functionSTO);
			} //else if (curr instanceof ExitSTO)
				//DoExitStmtChecks((ExitSTO) curr);
			// throw invalid?
		}

		if (!return_found
				&& !(((FuncSTO) functionSTO).getReturnType() instanceof VoidType)) {

			if (!((FuncSTO) functionSTO).returnMissing == true) {
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error6c_Return_missing);
			}
		}

		m_symtab.closeScope();
		m_symtab.setFunc(null);
	}

	// ----------------------------------------------------------------
	// Will create the constructor or destructor depending on checks
	// ----------------------------------------------------------------
	STO DoCtorDtorDecl(Vector<STO> vector, String funcName) {

		FuncSTO functionSTO = m_symtab.getFunc();

		// m_symtab.access(funcName);
		if (functionSTO instanceof FuncSTO) {
			STO curr;

			for (int i = 0; i < vector.size(); i++) {
				curr = vector.get(i);

				if (curr instanceof ExitSTO)
					DoExitStmtChecks((ExitSTO) curr);
				else if (curr instanceof BreakSTO) {
					ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Break,
							funcName);
					err.print(m_errors);
					m_nNumErrors++;
					return err;
				} else if (curr instanceof ContinueSTO) {
					ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Continue,
							funcName);
					return err;
				}
				// throw invalid?
			}
			m_symtab.closeScope();
			return functionSTO;
		} else {
			m_symtab.closeScope();
			m_symtab.setFunc(null);
			return null;
		}
	}

	void DoExitStmtChecks(ExitSTO stmt) {
		if (stmt.getType() instanceof ErrorType) {
			return;
		}
		if (!stmt.getType().isAssignableTo(new IntType())) {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error7_Exit,
					stmt.expr_type.getName()));
		}
	}

	void DoReturnStmtChecks(ReturnSTO stmt, FuncSTO funct) {
		Type funcReturnType = funct.getReturnType();

		if (stmt.getType() instanceof ErrorType)
			return;
		/*
		 * if (funct.getType() instanceof ErrorType) return;
		 */
		if (stmt instanceof ReturnSTO) {

			if (stmt.getType() instanceof VoidType
					&& !(funcReturnType instanceof VoidType)) {
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error6a_Return_expr);
				return;
			}

			if (!funct.returnByRef) {
				if (!stmt.getType().isAssignableTo(funcReturnType)) {
					m_nNumErrors++;
					m_errors.print(Formatter.toString(
							ErrorMsg.error6a_Return_type, stmt.getType()
									.getName(), funcReturnType.getName()));
					return;
				}
			} else {
				if (!stmt.getType().equals(funcReturnType)) {
					m_nNumErrors++;
					m_errors.print(Formatter.toString(
							ErrorMsg.error6b_Return_equiv, stmt.getType()
									.getName(), funcReturnType.getName()));
					return;
				}
			}
			/**
			 * else if (!stmt.getType().equals(funcReturnType)) {
			 * m_nNumErrors++; m_nNumErrors++;
			 * m_errors.print(Formatter.toString(ErrorMsg.error6a_Return_type,
			 * stmt.getType().getName(), funcReturnType.getName())); return;
			 */
			if (funct.returnByRef) {
				if (!stmt.isModLValue()) {
					m_nNumErrors++;
					m_errors.print(ErrorMsg.error6b_Return_modlval);
					return;
				}
			}

			if (funct.isModLValue()) { // must be return by reference

				// then!
				if (!stmt.getType().getName().equals(funcReturnType.getName())) {
					m_nNumErrors++;
					m_errors.print(Formatter.toString(
							ErrorMsg.error6b_Return_equiv, stmt.getType()
									.getName(), funcReturnType.getName()));
					return;
				} /*
				 * else if (!stmt.getIsModifiable()) { m_nNumErrors++;
				 * m_errors.print(ErrorMsg.error6b_Return_modlval); }
				 */
			}
		}
	}

	// }

	// ----------------------------------------------------------------
	// Regular Function Declarations for settingup functions
	// ----------------------------------------------------------------
	STO FieldFuncDef(Vector<STO> vector, String funcName) {

		STO functionSTO = m_symtab.getFunc();

		// m_symtab.access(funcName);

		STO curr;
		boolean return_found = false;

		if (functionSTO instanceof VarSTO) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error13a_Struct,functionSTO.getName()));  
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}

		if (functionSTO == null) {
			//System.err.println("HUGE ERROR!");
			return new ErrorSTO("HUGE ERROR!");

		}

		for (int i = 0; i < vector.size(); i++) {
			curr = vector.get(i);

			if (curr instanceof ReturnSTO) {
				return_found = true;
				DoReturnStmtChecks((ReturnSTO) curr, (FuncSTO) functionSTO);
			//} //else if (curr instanceof ExitSTO) {
				//	DoExitStmtChecks((ExitSTO) curr);
			}
			else if (curr instanceof BreakSTO) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Break);
				m_nNumErrors++;
				err.print(m_errors);
				return err;

			} else if (curr instanceof ContinueSTO) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Continue);
				m_nNumErrors++;
				err.print(m_errors);
				return err;

			} /*else if (curr instanceof VarSTO) {
				VarSTO vSTO = (VarSTO)curr;
				
				if ((m_symtab.accessLocal(vSTO.getName()) == null) && (m_symtab.accessGlobal(vSTO.getName()) == null)) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.undeclared_id,vSTO.getName()));
					err.print(m_errors);
					m_nNumErrors++;
					return err;
				}
				
			}*/
			// throw invalid?
		}

		if (!return_found
				&& !(((FuncSTO) functionSTO).getReturnType() instanceof VoidType)) {

			if (!((FuncSTO) functionSTO).returnMissing == true) {
				m_nNumErrors++;
				m_errors.print(ErrorMsg.error6c_Return_missing);
			}
			/**
			ErrorSTO err = new ErrorSTO(ErrorMsg.error6c_Return_missing);
			m_nNumErrors++;
			err.print(m_errors);
			return err;
			*/
		}

		m_symtab.closeScope();
		m_symtab.setFunc(null);
		return functionSTO;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoFormalParams(String id, Vector<STO> params) {
		if (m_symtab.getFunc() == null) {
			return;
		}
		// STO funct = (FuncSTO) m_symtab.getFunc();
		
		STO funct = m_symtab.access(id);
		
		if (funct instanceof FuncSTO) {
			FuncSTO tmp = (FuncSTO) funct;
			FuncSTO overRideFunc = m_symtab.getFunc();
			overRideFunc.paramList = params;
			if (!tmp.paramExists(overRideFunc.paramList, m_errors, m_nNumErrors))
				tmp.funcSTOList.add(overRideFunc);
			else {
				overRideFunc.returnMissing = true;
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, id));
			}
		}else {
			ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error13a_Struct,id));
			err.print(m_errors);
			m_nNumErrors++;
			return;
		}
	}

	void DoFormalParamsCtor(String id, Vector<STO> params) {
		
		if (m_symtab.getFunc() == null) {
			return;
		}
		// STO funct = (FuncSTO) m_symtab.getFunc();
		
		STO funct = m_symtab.access(id);
		
		if (funct instanceof FuncSTO) {
			FuncSTO tmp = (FuncSTO) funct;
			FuncSTO overRideFunc = m_symtab.getFunc();
			overRideFunc.paramList = params;
			//if (!tmp.paramExists(overRideFunc.paramList, m_errors, m_nNumErrors))
			//tmp.funcSTOList.add(overRideFunc);
			/*else {
				overRideFunc.returnMissing = true;
				m_nNumErrors++;
				m_errors.print(Formatter.toString(ErrorMsg.error9_Decl, id));
			}
		}else {
			ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error13a_Struct,id));
			err.print(m_errors);
			m_nNumErrors++;
			return;
		}*/
	}
	}
	
	
	
	
	
	
	
	
	
	
	void insertVarSTO(Vector<STO> params) {
		if (params != null) {
			for (int i = 0; i < params.size(); i++) {
				STO s = params.get(i);

				if (s != null) {
					if (m_symtab.accessLocal(s.getName()) != null) {
						STO f = m_symtab.accessLocal(s.getName());
						m_nNumErrors++;
						m_errors.print(Formatter.toString(
								ErrorMsg.error13a_Struct, s.getName()));
					} else {
						m_symtab.insert(s);
					}
				}
			}
		}
	}

	void insertCtorDtorSTO(Vector<STO> params, String structName) {
		if (params != null) {
			for (int i = 0 ; i < params.size() ; i++) {
			FuncSTO currFunc = (FuncSTO)params.get(i);
			
			if (currFunc.getName().charAt(0) == '~') { // Destructors
				if (!currFunc.getName().substring(1).equals(structName)) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error13b_Dtor,currFunc.getName(),structName));
					err.print(m_errors);
					m_nNumErrors++;
					continue;
				} else if (m_symtab.access(currFunc.getName()) != null) { // check for duplicate decl
					ErrorSTO err = new ErrorSTO (Formatter.toString(ErrorMsg.error9_Decl,currFunc.getName()));
					err.print(m_errors);
					m_nNumErrors++;
					continue;
				} else {
					m_symtab.insert(currFunc);
				}
				
			} else // CTORS
			{
				if (!currFunc.getName().equals(structName)) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error13b_Ctor,currFunc.getName(),structName));
					err.print(m_errors);
					m_nNumErrors++;
					continue;
				} else if (m_symtab.accessLocal(currFunc.getName()) != null) { // check for duplicate decl
					STO tmp = m_symtab.access(currFunc.getName());
					FuncSTO mainFunc = null;
					if (tmp instanceof FuncSTO) {
						mainFunc = (FuncSTO)tmp;
					}

					if (mainFunc.paramExists(currFunc.paramList, m_errors, m_nNumErrors)) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error9_Decl,currFunc.getName()));
						err.print(m_errors);
						m_nNumErrors++;
						continue;
					}
					else {
						mainFunc.funcSTOList.add(currFunc);
					}
				} else {
					m_symtab.insert(currFunc);
				}
				
			}
			
			}
			
		}
		
		
		
		/*if (params != null) {
			for (int i = 0; i < params.size(); i++) {
				STO s = params.get(i);
				if (s != null) {
					if (s.getName().charAt(0) == '~') {
						String dtorName = s.getName().substring(1);
						if (dtorName.equals(structName)) {
							m_symtab.insert(s);
						} // checks for Destructor
						else {
							m_nNumErrors++;
							m_errors.print(Formatter.toString(
									ErrorMsg.error13b_Dtor, dtorName,
									structName));
						}
					} else if (s.getName().equals(structName)) {
						m_symtab.insert(s);
					} // checks for constructor
					else {
						m_nNumErrors++;
						m_errors.print(Formatter.toString(
								ErrorMsg.error13b_Ctor, s.getName(), structName));
					}
				}
			}
		}*/

	}

	void insertFuncSTO(Vector<STO> params, String structName) {
		if (params != null) {
			for (int i = 0; i < params.size(); i++) {
				STO s = params.get(i);
				if (s != null) {
					if (s.getName().equals(structName)) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(
								ErrorMsg.error13a_Struct, s.getName()));
						err.print(m_errors);
						m_nNumErrors++;
						m_symtab.insert(err);
					} else {
						m_symtab.insert(s);
					}
				}
			}
		}
	}

	void structKept(STO stru) {
		if (stru != null) {
			structKeeper = (StructdefSTO) stru;
		} else {
			m_errors.print("Struct Variable is null");
		}
	}

	STO getStructKept() {
		return this.structKeeper;
	}

	STO getStruct(String s) {
		return m_symtab.access(s);
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	void DoBlockOpen() {
		// Open a scope.
		m_symtab.openScope();
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	Scope DoBlockClose() {
		return m_symtab.closeScope();
	}
	STO DoNegate(STO sto) {
		if (sto instanceof ErrorSTO) {
			return sto;
		}
		if (sto instanceof ConstSTO) {
			return new ConstSTO(sto.getName(), sto.getType(), (-1) * (((ConstSTO) sto).getIntValue()));
		}
		
		return sto;
	}
	
	String GetStructName() {
		if (structKeeper != null)
			return structKeeper.getName();
		else
			return null;
	}
	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoAssignExpr(STO stoDes, STO stoVal) {
		if (stoVal instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) stoVal;
			if (err.print(m_errors))
				m_nNumErrors++;
			return err;

		}
		if (stoDes instanceof ErrorSTO) {
			if (stoVal instanceof ErrorSTO) {
				ErrorSTO err = (ErrorSTO) stoDes;
				if (err.print(m_errors))
					m_nNumErrors++;
				return err;
			}
		}

		if (stoDes.getType() instanceof StructType
				&& stoVal.getType() instanceof StructType) {
			if (!(stoDes.getType().getName().equals(stoVal.getType().getName()))) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error3b_Assign, stoVal.getType().getName(),
						stoDes.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
		}

		if (stoDes.getType() == null || !stoDes.isModLValue()
				|| stoDes.getType() instanceof ArrayType) {
			if (!(stoDes instanceof ErrorSTO)) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error3a_Assign,
						stoDes.getName());
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
		} else if (stoDes.getType() instanceof ArrayType) {
			ArrayType arr = (ArrayType) stoDes.getType();
			Type typ = arr.getType();
			if (!(stoVal.getType().isAssignableTo(typ))) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error3b_Assign, stoVal.getType().getName(),
						typ.getName()), stoDes.getName());
				m_nNumErrors++;
				err.print(m_errors);
				return err;
			}
		} else if (!stoVal.getType().isAssignableTo(stoDes.getType())) {
			if (!(stoVal instanceof ErrorSTO || stoDes instanceof ErrorSTO)) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error3b_Assign, stoVal.getType().getName(),
						stoDes.getType().getName()), stoDes.getName());
				m_nNumErrors++;
				err.print(m_errors);
				return err;
			}
		}

		if (stoDes.getType() instanceof PointerType) {
			PointerType pType = (PointerType) stoDes.getType();
			if (stoVal.getType().isAssignableTo(pType)) {
				return stoDes;
			} else {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error3b_Assign, stoVal.getType().getName(),
						stoDes.getType().getName()), stoDes.getName());
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
		}

		return stoDes;
	}

	STO DoFuncCall_Struct(STO sto, Vector<STO> params, Scope m_symtab) {
		if (params == null) {
			params = new Vector<STO>();
		}

		if (!sto.isFunc()) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.not_function, sto.getName()));
			err.print(m_errors);
			m_nNumErrors++;
		}

		if (m_symtab.access(sto.getName()) != null) {
			FuncSTO funct = (FuncSTO) sto;

			if (funct.isOverloaded()) {
				if (funct.paramExists(params, m_errors, m_nNumErrors)){
					sto = funct.paramExistsReturn(params, m_errors, m_nNumErrors);
					return sto;
				}
				else {
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error9_Illegal, sto.getName()));
					err.print(m_errors);
					m_nNumErrors++;
				}
			} else {
				if (funct.paramList.size() != params.size()) {
					sto = new ErrorSTO(Formatter.toString(
							ErrorMsg.error5n_Call, params.size(),
							funct.paramList.size()));
					m_nNumErrors++;
					((ErrorSTO) sto).print(m_errors);
				} else {

					for (int i = 0; i < params.size(); i++) {
						if (params.get(i).getType() instanceof ErrorType) {
							return sto;
						}
						// checking right type
						if (!(params.get(i).getType()
								.isAssignableTo(funct.paramList.get(i)
										.getType()))) {
							sto = new ErrorSTO(Formatter.toString(
									ErrorMsg.error5a_Call, params.get(i)
											.getType().getName(),
									funct.paramList.get(i).getName(),
									funct.paramList.get(i).getType().getName()));
							m_nNumErrors++;
							((ErrorSTO) sto).print(m_errors);
						}
					}
				}
			}
		} else if (m_symtab.access(sto.getName()) == null) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.not_function, sto.getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}
		return sto;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoFuncCall(STO sto, Vector<STO> params) {
		if (params == null) {
			params = new Vector<STO>();
		}

		if (sto instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) sto;

			if (err.print(m_errors))
				m_nNumErrors++;
			return err;
		}
		if (!sto.isFunc()) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.not_function, sto.getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		} /*
		 * else { FuncSTO fSTO = (FuncSTO)sto; if (fSTO.paramExists(params))
		 * return sto; else { ErrorSTO err = new
		 * ErrorSTO(Formatter.toString(ErrorMsg.error5n_Call, params.size(),
		 * fSTO.paramList.size())); err.print(m_errors); m_nNumErrors++; return
		 * err; } }
		 */

		// if (m_symtab.access(sto.getName()) != null) {
		FuncSTO funct = (FuncSTO) sto;
		
		if (funct.isOverloaded()) {
			if (funct.paramExists(params, m_errors, m_nNumErrors)){
				sto = funct.paramExistsReturn(params, m_errors, m_nNumErrors);
				return sto;
			}
			else {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error9_Illegal, sto.getName()));
				err.print(m_errors);
				m_nNumErrors++;
			}

		} else {
			if (funct.paramList.size() != params.size()) {
				sto = new ErrorSTO(Formatter.toString(ErrorMsg.error5n_Call,
						params.size(), funct.paramList.size()));
				m_nNumErrors++;
				((ErrorSTO) sto).print(m_errors);
			} else {

				for (int z = 0; z < params.size(); z++) {
					if (params.get(z).getType() instanceof ErrorType) {
						continue;
					}

					Type argType = params.get(z).getType();
					Type paramType = funct.paramList.get(z).getType();

					VarSTO vSTO = (VarSTO) funct.paramList.get(z);

					if (vSTO.isReference) {
						if (!(argType.equals(paramType))) {
							sto = new ErrorSTO(Formatter.toString(
									ErrorMsg.error5r_Call, params.get(z)
											.getType().getName(),
									funct.paramList.get(z).getName(),
									funct.paramList.get(z).getType().getName()));
							m_nNumErrors++;
							((ErrorSTO) sto).print(m_errors);

						} else if (!(params.get(z).isModLValue())) {
							ErrorSTO err = new ErrorSTO(Formatter.toString(
									ErrorMsg.error5c_Call,
									funct.paramList.get(z).getName(),
									funct.paramList.get(z).getType().getName()));
							if (err.print(m_errors))
								m_nNumErrors++;
							sto = err;
						}

					} else {
						// checking right type
						if (!(params.get(z).getType()
								.isAssignableTo(funct.paramList.get(z)
										.getType()))) {
							sto = new ErrorSTO(Formatter.toString(
									ErrorMsg.error5a_Call, params.get(z)
											.getType().getName(),
									funct.paramList.get(z).getName(),
									funct.paramList.get(z).getType().getName()));
							m_nNumErrors++;
							((ErrorSTO) sto).print(m_errors);
						}
					}
				}
			}
		}

		// sto = new ErrorSTO(Formatter.toString(ErrorMsg.not_function,
		// sto.getName()));
		// ((ErrorSTO)sto).print(m_errors);
		// m_nNumErrors++;
		return sto;

	}

	STO setPassByReference(STO sto) {
		ExprSTO exprSTO = null;

		if (sto.getIsAddressable()) {
			if (sto instanceof ConstSTO) {
				if (((ConstSTO) sto).isLiteral == true) {
					ErrorSTO err = new ErrorSTO(
							Formatter.toString(ErrorMsg.error18_AddressOf, sto
									.getType().getName()));
					err.print(m_errors);
					m_nNumErrors++;
					return err;
				} else {
					PointerType pType = new PointerType(sto.getName(), 4);
					pType.setType(sto.getType());
					exprSTO = new ExprSTO(sto.getName(), pType);
					exprSTO.setIsAddressable(false);
					exprSTO.setIsModifiable(false);
					return exprSTO;
				}
			} else if (sto instanceof VarSTO) {
				PointerType pType = new PointerType(sto.getName(), 4);
				pType.setType(sto.getType());
				exprSTO = new ExprSTO(sto.getName(), pType);
				exprSTO.setIsAddressable(false);
				exprSTO.setIsModifiable(false);
				return exprSTO;
			} else if (sto instanceof ExprSTO) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error18_AddressOf, sto.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			} else {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error18_AddressOf, sto.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
		}
		return exprSTO;

	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoDesignator2_Dot(STO sto, String strID) {
		if (sto instanceof ErrorSTO)
			return sto;
		// Good place to do the struct checks
		if (sto.getType() instanceof StructType) {
				StructType structtype = (StructType) sto.getType();

				if (structtype.scope.access(strID) != null) {
					STO a = structtype.scope.access(strID);
					return structtype.scope.access(strID);
				} else {
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error14f_StructExp, strID, sto.getType()
									.getName()));
					err.print(m_errors);
					m_nNumErrors++;
					return err;
			}
		} else if (sto.getType() instanceof ThisType) {
			if (structKeeper == null) {
				ErrorSTO err = new ErrorSTO(
						"Cannot use this outside of a struct");
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			} else {
				STO tmp;
				if ((tmp = structKeeper.structVariableExists(strID)) != null) {
					return tmp;
				} else {
					if (m_symtab.access(strID) == null) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(
								ErrorMsg.error14c_StructExpThis, strID));
						err.print(m_errors);
						m_nNumErrors++;
						return err;
					} else
						return m_symtab.access(strID);
				}
			}
		} else {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error14t_StructExp, sto.getType().getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoDesignator2_Array(STO arraySto, STO expr) {
		// Good place to do the array checks
		STO sto = arraySto;
		STO newSto;

		if (arraySto instanceof ErrorSTO) {
			return arraySto;
		}
		
		if (expr instanceof ErrorSTO) {
			return expr;
		}
		// checking if the expression is the a const instead of something
		if (expr instanceof ConstSTO) {
			
			if (expr.getType() instanceof IntType) { // check to make sure that
														// expression is int
														// type
				ConstSTO con = (ConstSTO) expr;
				int val = con.getValue().intValue(); // get the value of the
														// expression
				if (arraySto.getType() instanceof ArrayType) {
					ArrayType arr = (ArrayType) arraySto.getType();
					if (val < 0 || val >= arr.arraySize) { // check the
															// boundaries
						m_nNumErrors++;
						ErrorSTO err = new ErrorSTO(Formatter.toString(
								ErrorMsg.error11b_ArrExp, val, arr.arraySize));
						err.print(m_errors);
						// the
						// error
						return err;
					}
				} else if (arraySto.getType() instanceof PointerType) {
					if (expr.getType() instanceof NullPointerType) {
						ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Nullptr);
						err.print(m_errors);
						m_nNumErrors++;
						return err;
					} else if (!(expr.getType() instanceof IntType)) {
						ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Receiver);
						err.print(m_errors);
						m_nNumErrors++;
						return err;
					}
					PointerType pType = (PointerType) arraySto.getType();
					return new VarSTO(arraySto.getName(), pType.type);

				} else {
					m_nNumErrors++;
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error11t_ArrExp, arraySto.getType()
									.getName()), sto.getName()); // return the
					return err; // Error
				}
			} else {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error11i_ArrExp, expr.getType().getName()));
				m_nNumErrors++;
				// error
				return err;
			}

		} else if (expr instanceof VarSTO) {
		
			
			// At this point, [x] is okay.
			VarSTO varSTO = (VarSTO) expr;

			if (!(varSTO.getType() instanceof IntType)) {
				
				if(!(arraySto.getType() instanceof ArrayType)){
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error11t_ArrExp, arraySto.getType()
									.getName()), sto.getName()); // return the
					if(err.print(m_errors))
						m_nNumErrors++;
				}
				
				else{
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error11i_ArrExp, expr.getType().getName()));
				if(err.print(m_errors))
					m_nNumErrors++;
				}
			}
		}
		else if(expr instanceof FuncSTO){
			if(!(expr.getType() instanceof IntType)){
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error11i_ArrExp, expr.getType().getName()));
				if(err.print(m_errors))
					m_nNumErrors++;
			}
		}
		else if (expr instanceof ExprSTO){
			if(!(expr.getType() instanceof IntType)){
				
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						 ErrorMsg.error11i_ArrExp, expr.getType().getName()));
				if(err.print(m_errors))
					m_nNumErrors++;
				return err;
			}
		}
		if (sto.getType() instanceof PointerType) {
			PointerType pType = (PointerType) arraySto.getType();
			return new VarSTO(arraySto.getName(), pType.type);
		} else if (sto.getType() instanceof ArrayType) {

			// Assumes multi-dimensionl array, this does a peel for us
			ArrayType arr2 = (ArrayType) sto.getType();
			Type tempTyp = arr2.getType();

			newSto = new VarSTO("array", tempTyp);
			return newSto;
		}

		return new VarSTO(arraySto.getName(), arraySto.getType());

	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoDesignator3_ID(String strID) {
		STO sto;
		if (structKeeper == null) {
			if ((sto = m_symtab.access(strID)) == null) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.undeclared_id, strID), strID);
				m_nNumErrors++;
				err.print(m_errors);
				return err;
			}
			return sto;
		} else {
			if (m_symtab.accessLocal(strID) == null) {
				if (m_symtab.accessGlobal(strID) == null) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.undeclared_id, strID), strID);
					m_nNumErrors++;
					err.print(m_errors);
					return err;
				}
				else
					return m_symtab.accessGlobal(strID);
			}
			else {
				return m_symtab.accessLocal(strID);
			}
			
			
		}
	}

	STO DoDesignator3Global_ID(String strID) {
		STO sto;
		if ((sto = m_symtab.accessGlobal(strID)) == null) {
			// m_nNumErrors++;
			// m_errors.print(Formatter.toString(ErrorMsg.error0g_Scope,
			// strID));
			sto = new ErrorSTO(
					Formatter.toString(ErrorMsg.error0g_Scope, strID));
		}
		return sto;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	Type DoStructType_ID(String strID) {
		STO sto;

		if ((sto = m_symtab.access(strID)) == null) {
			ErrorType err = new ErrorType(Formatter.toString(
					ErrorMsg.undeclared_id, strID), strID);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		} /*else if (!sto.isStructdef()) {
			ErrorType err = new ErrorType(Formatter.toString(ErrorMsg.not_type,
					sto.getName()), strID);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}*/

		return sto.getType();
	}

	// ----------------------------------------------------------------
	// DoBinaryExpression check will check the operations between the
	// the two disparate STOs. Will return the STO if compatible, if not
	// not compatible will return an error
	// ----------------------------------------------------------------

	STO DoBinaryExpr(STO a, Operator o, STO b) {
		STO x = m_symtab.access(b.getName());
		if (a != null && a.getType() instanceof ErrorType
				|| a instanceof ErrorSTO)
			return a;
		else if (b != null && b.getType() instanceof ErrorType
				|| b instanceof ErrorSTO)
			return b;

		if (o instanceof EQUOperator || o instanceof NEQOperator) {
			if (a.getType() instanceof PointerType
					|| b.getType() instanceof PointerType) {
				if (a.getType() instanceof PointerType
						&& b.getType() instanceof PointerType) {
					if ((a.getType() instanceof NullPointerType)
							|| (b.getType() instanceof NullPointerType)) {
						return new ExprSTO("true", new BoolType("true"));
					}
					PointerType ptrTypeA = (PointerType) a.getType();
					PointerType ptrTypeB = (PointerType) b.getType();
					if (!(ptrTypeA.equals(ptrTypeB))) {
						if (!(a.getType() instanceof NullPointerType)
								&& !(b.getType() instanceof NullPointerType)) {
							ErrorSTO err = new ErrorSTO(Formatter.toString(
									ErrorMsg.error17_Expr, o.name, a.getType()
											.getName(), b.getType().getName()));
							err.print(m_errors);
							m_nNumErrors++;
							return err;
						}

					}
					return new ExprSTO("true", new BoolType("true"));
				}

				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error17_Expr, o.name, a.getType().getName(), b
								.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}

		}
		STO result = o.checkOperands(a, b);

		if (result instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) result;
			if (err.print(m_errors))
				m_nNumErrors++;
			return err;
		}

		return result;
	}

	void ReadCodeBlock(Vector<STO> stmts) {
		STO curr;
		for (int i = 0; i < stmts.size(); i++) {
			curr = stmts.get(i);
			if (curr instanceof ReturnSTO) {
				DoReturnStmtChecks((ReturnSTO) curr,
						(FuncSTO) m_symtab.getFunc());
			}
			else if (curr instanceof BreakSTO) {
				
			}

		}
	}

	void DoIfStmt(STO expr) {
		if (expr.getType() instanceof BoolType)
			return;
		if (expr instanceof ErrorSTO || expr.getType() instanceof ErrorType) {
			ErrorSTO err = (ErrorSTO) expr;
			if (err.print(m_errors))
				m_nNumErrors++;
		} else {
			m_nNumErrors++;
			m_errors.print(Formatter.toString(ErrorMsg.error4_Test, expr
					.getType().getName()));
		}
	}

	void DoWhileStmt(STO expr) {
		if (expr instanceof ErrorSTO || expr.getType() instanceof ErrorType)
			return;
		if (expr.getType() instanceof BoolType)
			return;

		m_nNumErrors++;
		m_errors.print(Formatter.toString(ErrorMsg.error4_Test, expr.getType()
				.getName()));
	}

	public STO checkNullPtr(STO sto) {
		if (sto.getType() instanceof NullPointerType) {
			ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Nullptr);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		} else if (!(sto.getType() instanceof PointerType)) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error15_Receiver, sto.getType().getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}

		return sto;
	}

	public STO DoStarCheck(STO sto) {
		if (sto instanceof ErrorSTO) {
			return sto;
		}
		
		if (sto.getType() instanceof NullPointerType) {
			ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Nullptr);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		} else if (sto.getType() instanceof PointerType) {
			PointerType currType = (PointerType) sto.getType();
			Type nextType = currType.type;
			// sto.setType(nextType);
			//sto.setIsAddressable(true);
			//sto.setIsModifiable(true);
			VarSTO vSTO = new VarSTO(sto.getName(), nextType);
			vSTO.setIsAddressable(true);
			vSTO.setIsModifiable(true);
			return vSTO;
		} else {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error15_Receiver, sto.getType().getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}

	}

	public STO DoArrowCheck(STO sto, String strID) {
		Type curr = sto.getType();
		if (curr instanceof StructType) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}
		if (sto.getType() instanceof PointerType) {
			PointerType pType = (PointerType) sto.getType();
			if (pType.getPointerType() instanceof StructType)
				curr = pType.getPointerType();
			else {
				ErrorSTO err = new ErrorSTO(
						Formatter.toString(ErrorMsg.error15_ReceiverArrow, sto
								.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
		} else if (sto.getType() instanceof NullPointerType) {
			ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Nullptr);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		} else if (!(sto.getType() instanceof StructType)) {
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error15_ReceiverArrow, sto.getType().getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}
		// Everything good so far
		StructType structType = (StructType) curr;
		
		if (structType.scope == null) {
			if (m_symtab.access(strID) != null) {
				return m_symtab.access(strID);
			}
		} else if (structType.scope.access(strID) != null) {
			return structType.scope.access(strID);
		} else { // field does not exist in struct scope
			ErrorSTO err = new ErrorSTO(Formatter.toString(
					ErrorMsg.error14f_StructExp, strID, structType.getName()));
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}
		return sto;

	}

	public STO DoBracketCheck(STO sto) {
		if (sto.getType() instanceof NullPointerType) {
			ErrorSTO err = new ErrorSTO(ErrorMsg.error15_Nullptr);
			err.print(m_errors);
			m_nNumErrors++;
			return err;
		}

		return sto;
	}

	// ----------------------------------------------------------------
	//
	// ----------------------------------------------------------------
	STO DoPrePostIncDecExpr(STO a, UnaryOperator o) {

		if (a instanceof ErrorSTO || a.getType() instanceof ErrorType) {
			return a;
		}
		STO result = o.checkOperands(a);

		if (result instanceof ErrorSTO) {
			ErrorSTO err = (ErrorSTO) result;
			if (err.print(m_errors))
				m_nNumErrors++;
			return err;
		}

		return result;
	}

	public void checkNewStmt(STO stmt, Vector<STO> OptCtorCall) {

		if (stmt != null) {

			if (!(stmt.isModLValue())) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error16_New_var);
				err.print(m_errors);
				m_nNumErrors++;
				return;
			}

			else if (!(stmt.getType() instanceof PointerType)) {
				if (stmt.getType() instanceof ErrorType) {
					ErrorType err = (ErrorType) stmt.getType();
					if (err.print(m_errors))
						m_nNumErrors++;
					return;
				}
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error16_New, stmt.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return;
			}
			PointerType ptrType = (PointerType) stmt.getType();

			if ((ptrType.getPointerType() instanceof StructType)) {
				StructdefSTO stru = (StructdefSTO) getStruct(ptrType
						.getPointerType().getName());
				StructType struType = (StructType) stru.getType();
				Scope s = struType.scope;
				STO fun = struType.scope.access(ptrType.getPointerType()
						.getName());
				DoFuncCall_Struct(fun, OptCtorCall, s);
			} else {
				if (OptCtorCall != null) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(ErrorMsg.error16b_NonStructCtorCall, stmt.getType().getName()));
					err.print(m_errors);
					m_nNumErrors++;
				}
			}
		}
	}

	public void checkDeleteStmt(STO stmt) {

		if (stmt != null) {
			if (!(stmt.isModLValue())) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error16_Delete_var);
				err.print(m_errors);
				m_nNumErrors++;
				return;
			}
			if (!(stmt.getType() instanceof PointerType)) {
				if (stmt.getType() instanceof ErrorType) {
					ErrorType err = (ErrorType) stmt.getType();
					if (err.print(m_errors))
						m_nNumErrors++;
					return;
				}
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error16_Delete, stmt.getType().getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return;
			}
		}
	}

	public STO calcSizeOf(STO target) {
		ConstSTO val = new ConstSTO("Const", new IntType(), target.getType().getSize());
		
		if (target != null) {
			if (!(target.getType() instanceof Type)) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error19_Sizeof);
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
			if (!(target.getIsAddressable())) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error19_Sizeof);
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}

			if(target.getType() instanceof ArrayType){
				return calcSizeOfArray(target.getType(), target);
			}
			 val = new ConstSTO("Const", new IntType(), target
					.getType().getSize());
		}
		return val;
	}

	public STO calcSizeOfArray(Type target, STO array) {
		ConstSTO value = new ConstSTO("Const", new IntType(), 0);
		
		if(array == null){
			return new ConstSTO(target.getName(), new IntType(), target.getSize());
		}
		
		if (target != null) {
			if (!(array.getType() instanceof Type)) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error19_Sizeof);
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}
			if (!(array.getIsAddressable())) {
				ErrorSTO err = new ErrorSTO(ErrorMsg.error19_Sizeof);
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}

			// need to calculate it a little better
			ArrayType at = (ArrayType) array.getType();
			at.calcArraySize();

			ConstSTO val = new ConstSTO("Const", new IntType(), at.getSize());
			return val;
		}
		return value;
	}
	

	public STO DoTypeCast(Type typeToCast, STO origSTO) {
		Type typeNew = typeToCast;
		Type type2Old = origSTO.getType();

		if (typeToCast != null) {
			if (typeToCast instanceof NullPointerType
					|| origSTO.getType() instanceof NullPointerType) {
				ErrorSTO err = new ErrorSTO(Formatter.toString(
						ErrorMsg.error20_Cast, origSTO.getType().getName(),
						typeToCast.getName()));
				err.print(m_errors);
				m_nNumErrors++;
				return err;
			}

			if (origSTO != null) {
				if(origSTO.getType() instanceof PointerType && !(typeToCast instanceof NullPointerType)){
					VarSTO typeNewToReturn = new VarSTO(origSTO.getName(), typeNew);
					typeNewToReturn.setIsAddressable(false);
					typeNewToReturn.setIsModifiable(false);
					return typeNewToReturn;
				}
				if (!(typeNew instanceof BasicType)
						&& !(typeNew instanceof PointerType)) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error20_Cast, origSTO.getType().getName(),
							typeNew.getName()));
					err.print(m_errors);
					m_nNumErrors++;
					return err;
				}
				// Pointer check
				if (typeNew instanceof PointerType) {
					Type ActualpointerType = ((PointerType) typeNew)
							.getPointerType();
					if (!(type2Old.isAssignableToCast(ActualpointerType))) {
						ErrorSTO err = new ErrorSTO(Formatter.toString(
								ErrorMsg.error20_Cast, origSTO.getType()
										.getName(), typeToCast.getName()));
						err.print(m_errors);
						m_nNumErrors++;
						return err;
					}
					VarSTO typeNewToReturn = new VarSTO(origSTO.getName(),
							typeNew);
					typeNewToReturn.setIsAddressable(false);
					typeNewToReturn.setIsModifiable(false);
					return typeNewToReturn;
				} else if (type2Old instanceof PointerType) {
					VarSTO typeNewToReturn = new VarSTO(origSTO.getName(),
							typeNew);
					typeNewToReturn.setIsAddressable(false);
					typeNewToReturn.setIsModifiable(false);
					return typeNewToReturn;
				}

				// Non-PointerCheck
				if (!(type2Old.isAssignableToCast(typeNew))) {
					ErrorSTO err = new ErrorSTO(Formatter.toString(
							ErrorMsg.error20_Cast, origSTO.getType().getName(),
							typeToCast.getName()));
					err.print(m_errors);
					m_nNumErrors++;
					return err;
				}

				VarSTO typeNewToReturn = new VarSTO(origSTO.getName(), typeNew);
				typeNewToReturn.setIsAddressable(false);
				typeNewToReturn.setIsModifiable(false);
				return typeNewToReturn;
			}
		}
		return origSTO;

	}

	public void AddParams(Vector<STO> params) {
		for (int i = 0; i < params.size(); i++)
			m_symtab.insert(params.get(i));
	}

	// Keeps track if we are inside of a codeblock statement or not
	// if we are, that means codeBlockCounter > 0, else == 0
	public void incrementCodeBlockCounter() {
		codeBlockCounter++;
	}

	public void decrementCodeBlockCounter() {
		codeBlockCounter--;
	}

	// Checks the codeblock to see if we are in the statement for break/cont.
	public void checkCodeBlock(STO breakOrContinue) {
		if (!(breakOrContinue == null)) {
			if (!(codeBlockCounter > 0)) {
				if (breakOrContinue instanceof ContinueSTO) {
					ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Continue);
					m_nNumErrors++;
					err.print(m_errors);
				} else if (breakOrContinue instanceof BreakSTO) {
					ErrorSTO err = new ErrorSTO(ErrorMsg.error12_Break);
					m_nNumErrors++;
					err.print(m_errors);
				}
			}
		}
	}

	public VarSTO createNewVarSTO(String id, Type typ, String ref, STO array) {
		if (array == null) {
			return new VarSTO(id, typ, ref);
		} else {
			((ArrayType) array.getType()).getArrayType().setType(typ);
			if (ref != null) {
				((VarSTO) array).isReference = true;
			} else
				((VarSTO) array).isReference = false;
			array.setName(id);
			return (VarSTO) array;
		}
	}

}
