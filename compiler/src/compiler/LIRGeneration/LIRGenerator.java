package compiler.LIRGeneration;

import java.security.cert.PKIXRevocationChecker.Option;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import compiler.semanticAnalysis.SymbolTable;
import compiler.semanticAnalysis.TypeTable;
import compiler.tree.*;
import compiler.tree.expr.*;
import compiler.tree.expr.Expr.ParamType;
import compiler.tree.stmt.*;
import utils.Visitor;

public class LIRGenerator  implements Visitor 
{
	
	boolean debug = false;
	boolean verbose = true;

	private ASTNode root;
	SymbolTable st;
	TypeTable tt;
	StringMapping stringMapping;
	Method currentMethod;
	String currentClass="";
	Map<String, List<String>> dispatchTable;
	StringBuffer commands = new StringBuffer();
	StringBuffer dispatchCommands = new StringBuffer();
	StringBuffer stringLiterals = new StringBuffer();
	int regCount;
	int lableCount;
	List<String> whileLable = new ArrayList<>();
	String currentClassInstanceReg = "" ;//Holds the name of the register which is pointing to the current class instance
	boolean currentlyInsideAssignmentStmt = false;
	
	public LIRGenerator(ASTNode root)
	{
		init(root);
		tt= new TypeTable(root);
	}
	
	// use this builder in case Type Table has already been build elsewhere
	public LIRGenerator(ASTNode root, TypeTable tt)
	{
		init(root);
		this.tt= tt;
	}
	
	private void init(ASTNode root)
	{
		this.root = root;
		st = new SymbolTable();
		stringMapping = new StringMapping();
		dispatchTable = new HashMap<>();
		regCount = 0;  
		lableCount = 0;
	}
	
	private String getNewReg()
	{
		return "R" + regCount++;
	}
	
	private String getNewLable()
	{
		return "_" + (lableCount++) + "lable";
	}
	
	public String generate() 
	{
		addCheckFunctionStringErrors();
		addCheckFunctions();
		
		for(String className: tt.allClasses)
		{
			if(!className.equals("Library")){
				dispatchTable.put("_DV_" + className, new ArrayList<String>());
				for(Method method: tt.ClassNonStaticMethods(className))
				{
					dispatchTable.get("_DV_" + className).add("_" + method.className + "_" + method.name);
				}
			}
		}
		
		root.accept(this);
		
		for(Entry<String, String> entry: stringMapping.getStringMapping())
		{
			stringLiterals.append(entry.getValue() + ": \"" + entry.getKey() + "\" \n");
		}
		
		for(Entry<String, List<String>> entry: dispatchTable.entrySet())
		{
			dispatchCommands.append(entry.getKey() + ": [");
			
			for(String methodName: entry.getValue())
				dispatchCommands.append(methodName + ", ");
			
			if(entry.getValue().size() > 0)
				dispatchCommands.delete(dispatchCommands.length() - 2, dispatchCommands.length());
			
			dispatchCommands.append("]\n");
		}
		
		String output = "";
		output = stringLiterals.toString() + dispatchCommands.toString() + commands.toString();
		System.out.println(output);
		
		return output;
	}
	
	public void addCommand(String instruction, String op1, String op2)
	{
		commands.append(instruction);
		
		if(!op1.equals(""))
		{
			commands.append(" " + op1);
			if(!op2.equals(""))
				commands.append(", " + op2);
		}
		
		commands.append("\n");
	
	}
	
	@Override
	public void visit(CallStmt callStmt) {
		callStmt.call.accept(this);
	}

	@Override
	public void visit(ClassDecl classDecl) {
		st.pushScope();
		
		currentClass = classDecl.name;
		
		// Visit Static methods Before Class Fields were added to the Scope.
		if(classDecl.methods!=null)
			for (Method method: classDecl.methods)
			{
				if(method.isStatic)
					method.accept(this);
			}
		
		// Add Class Fields to the class Scope
		if(tt.FieldsInClass.containsKey(currentClass))
			for (Field field: tt.FieldsInClass.get(currentClass))
			{
				field.accept(this);
			}

		// Visit Non-Static methods after Class Fields were added to  the scope
		if(classDecl.methods!=null)
			for (Method method: classDecl.methods)
			{
				if(!method.isStatic)
					method.accept(this);
			}
		
		currentClass = "";
		
		st.popScope();
	}

	@Override
	public void visit(Program program) {
		if(program.classDecl_l!=null)
		for (ClassDecl cd : program.classDecl_l)
		{
			cd.accept(this);
		}
	}

	@Override
	public void visit(StmtList stmts) {
		if(stmts.statements!=null)
		{
			for (Stmt stmt : stmts.statements) 
			{
				stmt.accept(this);
			}
		}
	}

	@Override
	public void visit(Stmt stmt) {
		// TODO Auto-generated method stub
		
	}

	public String getLirSymbolName(String symbol)
	{
		return "v" + st.map.get(symbol).peek().scopeDepth + symbol;
	}
	
	@Override
	public void visit(AssignStmt stmt) 
	{
		if(verbose)addCommand("# Assignment:", "","");
		//Accept the expression on the right side of the assignment first
		if(verbose)addCommand("# Evaluate the expression:", "","");
		stmt.expr.accept(this);
		
		//Declaration with Assignment;		[// stmt ::= type ID ASSIGN expr SEMI]  For example:  "int a = b;"
		if(stmt.location == null)	
		{//Add Symbol to Scope
			//Try to Add variable ID to symbol table in the current scope + Initialize the variable because it was assigned
			st.tryAddSymbol(stmt.type, stmt.name, true, stmt.line);
						
			// if expression is memory, convert expression to registry
			if(stmt.expr.resultParamType == Expr.ParamType.Memory)
			{
				String newReg = getNewReg();
				addCommand("Move", stmt.expr.resultParam, newReg);

				addCommand("Move", newReg, getLirSymbolName(stmt.name));
			}
			else
			{
				addCommand("Move", stmt.expr.resultParam, getLirSymbolName(stmt.name));
			}
				

		}
		//Only Assignment(Without Declaration);		[// stmt ::= location ASSIGN expr SEMI] For example:  "a = b;"
		else
		{
			if(debug)System.out.println("***AssignStmt,Without Declaration");
			//We only initialize the Symbol
			if(stmt.location.getClass() == LocationID.class)  //For example: a = b;
			{
				st.tryInitSymbol(((LocationID)stmt.location).id);
			}
			
			currentlyInsideAssignmentStmt = true;
			stmt.location.accept(this);
			currentlyInsideAssignmentStmt = false;
			
			String src = stmt.expr.resultParam ,
				   dst = stmt.location.resultParam;
			
			
			if(debug)System.out.println(commands.toString());
			if(debug)System.out.println("src = " + src + " dst = " + dst);
			
			// if both location and expression are memory, convert expression to registry
			if(stmt.expr.resultParamType == Expr.ParamType.Memory &&
			   stmt.location.resultParamType == Expr.ParamType.Memory )
			{//Both are memory so Move through a new Register
				String newReg = getNewReg();
				addCommand("Move", stmt.expr.resultParam, newReg);
				src = newReg;
			}
			

			if(verbose)addCommand("# Assign the expression:", "","");
			
			if(stmt.location.resultParamField != "") //Move result to a Field
			{
				dst = stmt.location.resultParamField; // destination should be R1.1
				addCommand("MoveField", src, dst);
			}
			else if(stmt.location.resultParamArray != "") //Move result inside of an array
			{
				dst = stmt.location.resultParamArray; // destination should be R1[5]
				addCommand("MoveArray", src, dst);
			}
			else
			{
				addCommand("Move", src, dst);
			}

				
		}
		
		if(verbose)addCommand("# //Assignment", "","");

		
	}

	@Override
	public void visit(Expr expr) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void visit(BooleanExpr booleanExpr) 
	{
		if(booleanExpr.value == true)
		{
			booleanExpr.resultParam = "1";
		}
		else
		{
			booleanExpr.resultParam = "0";
		}
		booleanExpr.resultParamType = Expr.ParamType.Immediate;
	}

	@Override
	public void visit(VarExpr expr) {
		
		String newVar = stringMapping.addString(expr.name);
		//String newReg = getNewReg();
		
		expr.resultParam = newVar;
		expr.resultParamType = Expr.ParamType.Memory;
		
		//addCommand("Move", newVar, newReg);
	}

	@Override
	public void visit(NumberExpr expr) 
	{
		//String newReg = getNewReg();
		expr.resultParam = "" + expr.value;
		expr.resultParamType = Expr.ParamType.Immediate;
		
		//addCommand("Move", "" + expr.value, newReg);
	}

	@Override
	public void visit(UnaryOpExpr expr) 
	{
		expr.operand.accept(this);
		String targetParam;
		if(expr.operand.resultParamType== Expr.ParamType.Immediate || expr.operand.resultParamType== Expr.ParamType.Memory)
		{
			String newReg = getNewReg();
			addCommand("Move", expr.operand.resultParam, newReg);
			targetParam = newReg;
		}
		else
		{
			targetParam = expr.operand.resultParam;
		}
		
		
		String newLable;
		switch(expr.op)
		{
			case MINUS:
				addCommand("Neg", targetParam,"");
				break;
			case LNEG: // maybe we can use Inc Reg && Dec Reg  more easy??
				newLable = getNewLable();
				addCommand("Compare", "0", targetParam);
				addCommand("JumpFalse", "_false" + newLable, "");
				addCommand("Move", "1", targetParam);
				addCommand("Jump", "_end" + newLable, "");
				addCommand("_false" + newLable + ":", "", "");
				addCommand("Move", "0", targetParam);
				addCommand("_end" + newLable + ":", "", "");	
				break;	
			default:
				break;
		}
		
		expr.resultParam = targetParam;
		if(expr.operand.resultParamType == Expr.ParamType.Memory)
			expr.resultParamType = Expr.ParamType.Memory;
		else
			expr.resultParamType = Expr.ParamType.Reg;
		
	}

	@Override
	public void visit(BinaryOpExpr expr)
	{
		expr.lhs.accept(this); 
		expr.rhs.accept(this);
		
		String targetParam;
		
		if(expr.lhs.resultParamType == Expr.ParamType.Immediate || expr.lhs.resultParamType == Expr.ParamType.Memory)
		{
			String newReg = getNewReg();	
			addCommand("Move", expr.lhs.resultParam, newReg);		
			targetParam = newReg;
		}
		else
			targetParam = expr.lhs.resultParam;
		String newLable;
		switch(expr.op)
		{
			case PLUS: // NEED TO add a check for String type ( Concatenation )
				if(expr.lhs.staticType.basicType == BasicType.INT)
				{
					addCommand("Add", expr.rhs.resultParam, targetParam);
				}
				if(expr.lhs.staticType.basicType == BasicType.STRING)
				{
					addCommand("Library", "__stringCat("+ expr.lhs.resultParam +", "+expr.rhs.resultParam  + ")", targetParam);
				}
				break;
			case MINUS:
				addCommand("Sub", expr.rhs.resultParam, targetParam);
				break;
			case MULTIPLY:
				addCommand("Mul", expr.rhs.resultParam, targetParam);
				break;
			case DIVIDE:
			{
				addCommand("StaticCall __checkZero(b=" + expr.rhs.resultParam + "),Rdummy","","");
				addCommand("Div", expr.rhs.resultParam, targetParam);
				break;
			}
			case MOD:
				addCommand("Mod", expr.rhs.resultParam, targetParam);
				break;
			case GT:
				newLable = getNewLable();
				addCommand("Compare", expr.rhs.resultParam, targetParam);
				addCommand("JumpG", "_GT" + newLable, "");
				addCommand("Move", "0", targetParam);
				addCommand("Jump", "_end" + newLable, "");
				addCommand("_GT" + newLable + ":", "", "");
				addCommand("Move", "1", targetParam);
				addCommand("_end" + newLable + ":", "", "");			
				break;
			case GTE:
				newLable = getNewLable();
				addCommand("Compare", expr.rhs.resultParam, targetParam);
				addCommand("JumpGE", "_GTE" + newLable, "");
				addCommand("Move", "0", targetParam);
				addCommand("Jump", "_end" + newLable, "");
				addCommand("_GTE" + newLable + ":", "", "");
				addCommand("Move", "1", targetParam);
				addCommand("_end" + newLable + ":", "", "");			
				break;
			case LT:
				newLable = getNewLable();
				addCommand("Compare", expr.rhs.resultParam, targetParam);
				addCommand("JumpL", "_LT" + newLable, "");
				addCommand("Move", "0", targetParam);
				addCommand("Jump", "_end" + newLable, "");
				addCommand("_LT" + newLable + ":", "", "");
				addCommand("Move", "1", targetParam);
				addCommand("_end" + newLable + ":", "", "");			
				break;
			case LTE:
				newLable = getNewLable();
				addCommand("Compare", expr.rhs.resultParam, targetParam);
				addCommand("JumpLE", "_LTE" + newLable, "");
				addCommand("Move", "0", targetParam);
				addCommand("Jump", "_end" + newLable, "");
				addCommand("_LTE" + newLable + ":", "", "");
				addCommand("Move", "1", targetParam);
				addCommand("_end" + newLable + ":", "", "");			
				break;
			case LAND:	
				addCommand("And", expr.rhs.resultParam, targetParam);
				break;
			case LOR:	
				addCommand("Or", expr.rhs.resultParam, targetParam);
				break;
			case EQUAL: // NEED TO add references check
				if(expr.lhs.staticType.basicType == BasicType.INT || expr.lhs.staticType.basicType == BasicType.BOOLEAN)
				{
					newLable = getNewLable();
					addCommand("Compare", expr.rhs.resultParam, targetParam);
					addCommand("JumpFalse", "_then" + newLable, "");
					addCommand("Move", "1", targetParam);
					addCommand("Jump", "_end" + newLable, "");
					addCommand("_then" + newLable + ":", "", "");
					addCommand("Move", "0", targetParam);
					addCommand("_end" + newLable + ":", "", "");
				}
				else
				{
					String newReg = getNewReg();	
					addCommand("Move", expr.rhs.resultParam, newReg);
					newLable = getNewLable();
					addCommand("Compare", newReg, targetParam);
					addCommand("JumpFalse", "_then" + newLable, "");
					addCommand("Move", "1", targetParam);
					addCommand("Jump", "_end" + newLable, "");
					addCommand("_then" + newLable + ":", "", "");
					addCommand("Move", "0", targetParam);
					addCommand("_end" + newLable + ":", "", "");
				}
				break;
			case NEQUAL:  // NEED TO add references check
				if(expr.lhs.staticType.basicType == BasicType.INT || expr.lhs.staticType.basicType == BasicType.BOOLEAN)
				{
					newLable = getNewLable();
					addCommand("Compare", expr.rhs.resultParam, targetParam);
					addCommand("JumpFalse", "_then" + newLable, "");
					addCommand("Move", "0", targetParam);
					addCommand("Jump", "_end" + newLable, "");
					addCommand("_then" + newLable + ":", "", "");
					addCommand("Move", "1", targetParam);
					addCommand("_end" + newLable + ":", "", "");
				}
				else
				{
					String newReg = getNewReg();	
					addCommand("Move", expr.rhs.resultParam, newReg);
					newLable = getNewLable();
					addCommand("Compare", newReg, targetParam);
					addCommand("JumpFalse", "_then" + newLable, "");
					addCommand("Move", "0", targetParam);
					addCommand("Jump", "_end" + newLable, "");
					addCommand("_then" + newLable + ":", "", "");
					addCommand("Move", "1", targetParam);
					addCommand("_end" + newLable + ":", "", "");
				}
				break;	
			default:
				break;
		}
		
		
		expr.resultParam = targetParam;
		if(expr.rhs.resultParamType == Expr.ParamType.Memory)
			expr.resultParamType = Expr.ParamType.Memory;
		else
			expr.resultParamType = Expr.ParamType.Reg;

	}

	@Override
	public void visit(Formal field) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Method method) 
	{
		currentClassInstanceReg = "";
		// new scope
		st.pushScope();
		
		currentMethod = method;

		if(!method.name.equals("main"))
			commands.append("\n_" + method.className + "_" + method.name + ":\n");
		else
			commands.append("\n_ic_main:\n");
		
		if(method.formals != null)
		{
			for(Formal formal :  method.formals) //Add all the method parameters to the current scope 
			{
				st.tryAddSymbol(formal.type ,formal.name,true,formal.line);
			}
		}
		method.statements.accept(this);
		
		if(method.name.equals("main"))
			addCommand("Library", "__exit(0)", "Rdummy");
		else if(method.type.basicType == BasicType.VOID)
		{
			addCommand("Return", "9999", "");
		}
		// exit scope
		currentMethod = null;
		st.popScope();
		currentClassInstanceReg = "";
	}

	@Override
	public void visit(Type type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(FieldOrMethodList fieldOrMethodList) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(Field field) {
		for(String id: field.idList) //Add all Class fields to current Scope 
		{ 
			st.tryAddSymbol(field.type ,id,false,field.line); 
			st.tryInitSymbol(id);		
		}
	}

	@Override
	public void visit(StaticCall call) 
	{

		if(call.exprList!=null)
		{
			for(int i=0 ; i < call.exprList.size() ; i++)
			{
				call.exprList.get(i).accept(this);
			}
			
		}
		
		if(call.className.equals("Library")) // need to save the return value and update the call.resultParam
		{
			
			String mName = call.methodName; 
			
			//The call return a value(NOT void)
			if(mName.equals("readi") || mName.equals("readln") || mName.equals("eof") || mName.equals("stoi") || mName.equals("itos")
			  || mName.equals("stoa") || mName.equals("atos") || mName.equals("random") || mName.equals("time")                     )
			{
				String reg = getNewReg(); //Create a new Reg
				if(call.methodName.equals("readi"))  	addCommand("Library", "__readi()", reg);
				if(call.methodName.equals("readln"))	addCommand("Library", "__readln()", reg);
				if(call.methodName.equals("eof"))		addCommand("Library", "__eof()", reg);
				if(call.methodName.equals("stoi"))		addCommand("Library", "__stoi("+ call.exprList.get(0).resultParam  + ")", reg);
				if(call.methodName.equals("itos"))		addCommand("Library", "__itos(" + call.exprList.get(0).resultParam + ")", reg);
				if(call.methodName.equals("stoa"))		addCommand("Library", "__stoa(" + call.exprList.get(0).resultParam + ")", reg);
				if(call.methodName.equals("atos"))		addCommand("Library", "__atos(" + call.exprList.get(0).resultParam + ")", reg);
				if(call.methodName.equals("random"))	addCommand("Library", "__random(" + call.exprList.get(0).resultParam + ")", reg);
				if(call.methodName.equals("time"))		addCommand("Library", "__time()", reg);
				
				call.resultParam = reg;
			}
			else
			{
				if(call.methodName.equals("println"))	addCommand("Library", "__println(" + call.exprList.get(0).resultParam + ")", "Rdummy");
				if(call.methodName.equals("print"))		addCommand("Library", "__print(" + call.exprList.get(0).resultParam + ")", "Rdummy");
				if(call.methodName.equals("printi"))	addCommand("Library", "__printi(" + call.exprList.get(0).resultParam + ")", "Rdummy");
				if(call.methodName.equals("printb"))	addCommand("Library", "__printb(" + call.exprList.get(0).resultParam + ")", "Rdummy");
				if(call.methodName.equals("exit"))		addCommand("Library", "__exit(" + call.exprList.get(0).resultParam + ")", "Rdummy");
				
				call.resultParam = "Rdummy";
			}
			
			call.resultParamType = Expr.ParamType.Reg;
		}
		else
		{
			String methodName = "_"+ call.className +"_"+ call.methodName;
			String param = "";
			List<Method> staticMethods = tt.ClassStaticMethods(call.className);	
			Method m=null;
			for(Method m1: staticMethods)
			{
				if(m1.name.equals(call.methodName))
				{
					m = m1;
				}
			}
		
			if(call.exprList!=null && m.formals.size() > 0)
			{
				param = "v2"+m.formals.get(0).name+"="+call.exprList.get(0).resultParam;
				for(int i=1; i<m.formals.size();i++)
				{
					param += ", "+"v2"+m.formals.get(i).name+"="+call.exprList.get(i).resultParam;
				}
			}
			
			
			if(call.isStmt)
			{
				addCommand("StaticCall",methodName+"("+ param +")","Rdummy");
			}
			else
			{
				String targetParam = getNewReg();		
				addCommand("StaticCall",methodName+"("+ param +")",targetParam);
				
				call.resultParam = targetParam;
				call.resultParamType = Expr.ParamType.Reg;
			}
			
		}
		
	}


	public void visit(VirtualCall virtualCall) 
	{
		
		if(virtualCall.expr != null)
		{
			virtualCall.expr.accept(this);
		}

		if(virtualCall.expr != null) // check to see if it's reference to null
			addCommand("StaticCall __checkNullRef(a=" + virtualCall.expr.resultParam + "),Rdummy","","");
		
		if(virtualCall.exprList!=null)
		{
			
			for(int i=0 ; i < virtualCall.exprList.size() ; i++)
			{
				virtualCall.exprList.get(i).accept(this);
			}
		}	
		
		String className="";
		if(virtualCall.expr == null)
		{
			className = currentClass;
		}
		else
		{
			className = virtualCall.expr.staticType.className;
		}
		
		
		//find the position of the method.
		int methodPosition=0;
		String methodName = "_"+ className +"_"+ virtualCall.methodName;
		List<String> methods = dispatchTable.get("_DV_"+className);
		for(int i=0; i<methods.size();i++)
		{
			if(methods.get(i).equals(methodName))
			{
				methodPosition = i;
			}		
		}
		
		
		String param = "";
		List<Method> staticMethods = tt.ClassMethods(className);	
		Method m=null;
		for(Method m1: staticMethods)
		{
			if(m1.name.equals(virtualCall.methodName))
			{
				m = m1;
			}
		}
	
		if(virtualCall.exprList!=null && virtualCall.exprList.size() > 0)
		{
			param = "v2"+m.formals.get(0).name+"=" + virtualCall.exprList.get(0).resultParam;
			for(int i=1; i<m.formals.size();i++)
			{
				param += ", "+"v2"+m.formals.get(i).name+"="+virtualCall.exprList.get(i).resultParam;
			}
		}
		
		
		String instanceReg = getNewReg();	
		if(virtualCall.expr == null)
		{
			addCommand("Move","this",instanceReg);
		}
		else
		{
			addCommand("Move",virtualCall.expr.resultParam,instanceReg);
		}
		
		if(virtualCall.isStmt)
		{
			addCommand("VirtualCall",instanceReg+"."+methodPosition+"("+ param +")","Rdummy");
		}
		else
		{
			String targetParam = getNewReg();		
			addCommand("VirtualCall",instanceReg+"."+methodPosition+"("+ param +")",targetParam);
			
			virtualCall.resultParam = targetParam;
			virtualCall.resultParamType = Expr.ParamType.Reg;
		}
		
	}

	@Override
	public void visit(LocationID locationID) 
	{
		if(debug)System.out.println("***LocationID " + locationID.id);
		
		//Operating on a class Field
		if(currentMethod!=null && !currentMethod.isStatic && st.map.get(locationID.id).peek().scopeDepth==1) 
		{//the locationID is located inside a Virtual Method and it is a Field of the class(Not a local variable)
		//if(debug)System.out.println("***LocationID-Virtual " + st.map.get(locationID.id).peek().scopeDepth);

			//Get register which is pointing to the class instance
			String reg_class_instance = getCurrentClassRegister();
			
			//Return pointer the field memory  (For example R2.1) in case we need to set the Field value(Assignment statement)
			String LIRFieldName = reg_class_instance + "." + tt.getFieldIndex(currentClass, locationID.id);
			locationID.resultParamField = LIRFieldName;
			
			//Return a register which contains the Field Value  (For example R1) 
    		//in case we need to run computations(Add) on the Field value
			String newReg_field = getNewReg();
			addCommand("MoveField", LIRFieldName, newReg_field);
			locationID.resultParam = newReg_field;
			locationID.resultParamType = Expr.ParamType.Reg;
		}
		else  
		{   //Operating on a local variable
			locationID.resultParam = getLirSymbolName(locationID.id); //To get variable name(Not register name!)
			locationID.resultParamType = Expr.ParamType.Memory;
		}
	}

	public void visit(LocationExprID locationExprID) 
	{//Currently we are visiting in a Field of an instance/class. For example: "b.b1" and this is the b1 visit
	 // Visit order example: b1.b2 , the first visit will be in "b2" then we will accept it and LocationID will get a visit on "b1"
		if(debug)System.out.println("***LocationExprID " + locationExprID.id);
		String reg_class_instance="";
		String LIRFieldName="";
		locationExprID.expr.accept(this);

		
		addCommand("StaticCall __checkNullRef(a=" + locationExprID.expr.resultParam + "),Rdummy","","");
		
		if(locationExprID.expr.resultParamType == Expr.ParamType.Memory) //Meaning the subExpression is a local variable
		{																 
			//Move memory to register and access the field
			reg_class_instance = getNewReg();
			addCommand("Move", locationExprID.expr.resultParam, reg_class_instance); //Move v2b,R0
		}
		else //Meaning the subExpression is a Field[But was already transfer to a regular register. Example: "R5.5" was transfered "R6"]
		{
			reg_class_instance = locationExprID.expr.resultParam;
		}

		//Build the field name, for example: "R6.1"
		LIRFieldName = reg_class_instance + "." + tt.getFieldIndex(locationExprID.expr.staticType.className, locationExprID.id);
		
		//Set 'resultParamField' to R6.1[Field Pointer] 
		locationExprID.resultParamField = LIRFieldName; //R6.1

		//Resolve R6.1 value into R7 and set R7 in 'resultParam'  [Resolve the Field value] 
		String newReg_field = getNewReg();
		addCommand("MoveField", LIRFieldName, newReg_field);  //MoveField R6.1 , R7
		locationExprID.resultParam = newReg_field;  //R7
		locationExprID.resultParamType = Expr.ParamType.Reg;
	}

	@Override
	public void visit(LocationExprExpr locationIDExprExpr) 
	{
		//if(debug)System.out.println("***locationIDExprExpr ");
		
		locationIDExprExpr.expr1.accept(this); //Left side of the expression. For example, "a"
		locationIDExprExpr.expr2.accept(this); //Right side of the expression. for example the '5' in "a[5]"
		
		String reg_expr1,reg_expr2, reg = getNewReg();

		//In case expr1 is a memory(v2b), move to a register
		if(locationIDExprExpr.expr1.resultParamType == Expr.ParamType.Memory) 
		{
			reg_expr1 = getNewReg();
			addCommand("Move", locationIDExprExpr.expr1.resultParam, reg_expr1); //Move v2b,R0
		}
		else
		{
			reg_expr1 = locationIDExprExpr.expr1.resultParam;
		}

		//In case expr2 is a memory(v2b), move to a register
		if(locationIDExprExpr.expr2.resultParamType == Expr.ParamType.Memory)
		{
			reg_expr2 = getNewReg();
			addCommand("Move", locationIDExprExpr.expr2.resultParam, reg_expr2); //Move v2b,R0
		}
		else
		{
			reg_expr2 = locationIDExprExpr.expr2.resultParam;
		}

		
		addCommand("StaticCall __checkNullRef(a=" + reg_expr1 + "),Rdummy","",""); //Move R2[5],R3
		addCommand("StaticCall __checkArrayAccess(a=" + reg_expr1 + ", i=" + reg_expr2 + "),Rdummy","",""); //Move R2[5],R3
		//Copy element from an array to a register
		//if(reg_expr2==null)
			
		addCommand("MoveArray", reg_expr1 + "[" + reg_expr2 + "]" , reg); //Move R2[5],R3

		locationIDExprExpr.resultParam = reg;
		locationIDExprExpr.resultParamType = Expr.ParamType.Reg;
		locationIDExprExpr.resultParamArray =  reg_expr1 + "[" + reg_expr2 + "]" ;
		
	}

	@Override
	public void visit(ReturnStmt returnStmt) 
	{
		if(currentMethod.type.basicType == BasicType.VOID) //Void method 
		{
			addCommand("Return", "9999", "");
		}
		else //Not a void method 
		{
			returnStmt.expr.accept(this); //Resolve expression type
			addCommand("Return", returnStmt.expr.resultParam, "");
		}
		
		
	}

	@Override
	public void visit(ContinueBreakStmt contunueBreakStmt) 
	{
		if(contunueBreakStmt.action.equals("break")) // break
		{
			addCommand("Jump", "_end" + whileLable.get(whileLable.size()-1), "");
		}
		else // continue
		{
			addCommand("Jump", "_condition" + whileLable.get(whileLable.size()-1), "");
		}
		
	}

	@Override
	public void visit(ScopeStmt scopeStmt) {
		// new scope
		st.pushScope();

		scopeStmt.statements.accept(this);

		// exit scope
		st.popScope();
	}

	@Override
	public void visit(IfStmt ifStmt) 
	{
		if(verbose) addCommand("# IF statement:","","");
		
		ifStmt.expr.accept(this);
		
		if(verbose) addCommand("# Done accepting IF boolean expression","","");
		
		String newLable = getNewLable();
		
		String newReg = getNewReg();
		addCommand("Move", ifStmt.expr.resultParam, newReg);
		addCommand("Compare", "0", newReg);
		addCommand("JumpFalse", "_then" + newLable, "");
		addCommand("Jump", "_else" + newLable, "");
		addCommand("_then" + newLable + ":", "", "");
		ifStmt.stmt1.accept(this);
		
		
		addCommand("Jump", "_end" + newLable, "");
		addCommand("_else" + newLable + ":", "", "");

		if(ifStmt.stmt2!=null) //Else statement Exist
		{
			ifStmt.stmt2.accept(this);
		}
		addCommand("_end" + newLable + ":", "", "");
		
		if(verbose) addCommand("# //IF statement","","");
	}

	@Override
	public void visit(WhileStmt whileStmt) 
	{
		String newLable = getNewLable();
		String newReg = getNewReg();
		
		whileLable.add(newLable);
		
		addCommand("_condition" + newLable + ":", "", "");
		whileStmt.expr.accept(this);
		addCommand("Move", whileStmt.expr.resultParam, newReg);
		addCommand("Jump", "_while" + newLable, "");
		
		
		addCommand("_while" + newLable + ":", "", "");
		addCommand("Compare", "0", newReg);
		addCommand("JumpFalse", "_then" + newLable, "");
		addCommand("Jump", "_end" + newLable, "");
		addCommand("_then" + newLable + ":", "", "");
		whileStmt.stmt.accept(this);
		addCommand("Jump", "_condition" + newLable, "");
		
		addCommand("_end" + newLable + ":", "", "");	
		
		whileLable.remove(whileLable.size()-1);
		
	}

	@Override
	public void visit(DeclarationStmt declarationStmt)
	{
		st.tryAddSymbol(declarationStmt.type, declarationStmt.name, false, declarationStmt.line);
	}

	public void visit(ThisExpr thisExpr) 
	{
		
		thisExpr.resultParam =  getCurrentClassRegister();
		thisExpr.resultParamType = Expr.ParamType.Reg;
		
	}
	
	private String getCurrentClassRegister()
	{
		//Get register which is pointing to the class instance
		String reg_class_instance;
		if(currentClassInstanceReg !="") //We already have a register which is pointing to the class instance
		{
			reg_class_instance = currentClassInstanceReg;
		}
		else
		{//We are creating a register which will point to the class instance
			reg_class_instance = getNewReg();
			currentClassInstanceReg = reg_class_instance;
			addCommand("Move", "this", reg_class_instance);
		}
		
		return reg_class_instance;
	}

	@Override
	public void visit(NewExpr newExpr) 
	{
		if(newExpr.classID == null)
		{
			newExpr.expr.accept(this);
			
			addCommand("StaticCall __checkSize(n=" + newExpr.expr.resultParam + "),Rdummy","","");
			
			String resultReg = getNewReg();

			//Multiply by 4
			String reg_alloc_size = getNewReg();
			addCommand("Move", newExpr.expr.resultParam , reg_alloc_size);
			addCommand("Mul", "4" , reg_alloc_size);

			
			addCommand("Library", "__allocateArray(" + reg_alloc_size + ")", resultReg);
			
			newExpr.resultParam = resultReg;
		}
		else
		{
			String resultReg = getNewReg();
			int allocSize = (tt.FieldsInClass.get(newExpr.classID).size() + 1) * 4;
			
			if(verbose)addCommand("# new " + newExpr.classID +"() :","","");
			addCommand("Library", "__allocateObject(" + allocSize + ")", resultReg);
			addCommand("MoveField", "_DV_" + newExpr.classID, resultReg + ".0");
			
			newExpr.resultParam = resultReg;
		}
		
		newExpr.resultParamType = ParamType.Reg;
		
	}

	@Override
	public void visit(LengthExpr lengthExpr) 
	{
		lengthExpr.expr.accept(this);
		
		String newReg = getNewReg();
		
		
		addCommand("StaticCall __checkNullRef(a=" + lengthExpr.expr.resultParam + "),Rdummy","","");
		addCommand("ArrayLength", lengthExpr.expr.resultParam, newReg);
		
		lengthExpr.resultParam = newReg;
		lengthExpr.resultParamType = Expr.ParamType.Reg;
	}

	@Override
	public void visit(ParenthesesExpr parenthesesExpr) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void visit(NullExpr nullExpr) 
	{
		if(verbose)addCommand("# NULL assignment to R00:", "" , "");
		addCommand("Move", "0" , "R00");
		nullExpr.resultParam = "R00";
		nullExpr.resultParamType = Expr.ParamType.Reg;
	}

	private void addCheckFunctionStringErrors()
	{
		if(verbose) stringLiterals.append("# Error Strings:\n");
		stringLiterals.append("str_err_checkNullRef" + ": \"" + "Runtime Error: Null pointer dereference!" + "\" \n");
		stringLiterals.append("str_err_checkArrayAccess" + ": \"" + "Runtime Error: Array index out of bounds!" + "\" \n");
		stringLiterals.append("str_err_checkSize" + ": \"" + "Runtime Error: Array allocation with non-positive array size!" + "\" \n");
		stringLiterals.append("str_err_checkZero" + ": \"" + "Runtime Error: Division by zero!" + "\" \n");
		stringLiterals.append("\n");
	}
	
	public void addCheckFunctions()
	{
		addCommand("","","");
		if(verbose) addCommand("# Error verification functions:","","");

		addCheckNullRef();
		addCheckArrayAccess();
		addCheckSize();
		addCheckZero();
		
		addCommand("","","");
		addCommand("","","");
	}
	
	public void addCheckNullRef()
	{
		//"checkNullRef"
		
		addCommand("","","");
		addCommand("__checkNullRef:","","");
		addCommand("Move a, R01","","");
		addCommand("Compare 0, R01","","");
		addCommand("JumpTrue _error_checkNullRef","","");
		addCommand("Return 9999","","");
		addCommand("","","");
		
		//err_checkNullRef
		addCommand("","","");
		addCommand("_error_checkNullRef:","","");
		addCommand("Library __println(str_err_checkNullRef), Rdummy","","");
		addCommand("Library __exit(1), Rdummy","","");
		addCommand("Return 9999","","");
	}
	
	public void addCheckArrayAccess()
	{
		//checkArrayAccess

		addCommand("","","");
		addCommand("__checkArrayAccess:","","");
		addCommand("ArrayLength a, R01","","");
		addCommand("Compare i, R01","","");
		addCommand("JumpLE _error_checkArrayAccess","",""); // R01(ArrayLength) <= i(index) 
		
		addCommand("Move i, R01","","");
		addCommand("Compare 0, R01","","");
		addCommand("JumpL _error_checkArrayAccess","","");
		
		addCommand("Return 9999","","");
		
		//err_checkArrayAccess
		addCommand("","","");
		addCommand("_error_checkArrayAccess:","","");
		addCommand("Library __println(str_err_checkArrayAccess), Rdummy","","");
		addCommand("Library __exit(1), Rdummy","","");
		addCommand("Return 9999","","");
	}
	
	public void addCheckSize()
	{
		//checkSize
		addCommand("","","");
		
		addCommand("__checkSize:","","");
		addCommand("Move n, R01","","");
		addCommand("Compare 0, R01","","");		
		addCommand("JumpLE _error_checkSize","",""); //  Size <= 0 
		addCommand("Return 9999","","");
		
		//err_checkSize
		addCommand("","","");
		addCommand("_error_checkSize:","","");
		addCommand("Library __println(str_err_checkSize), Rdummy","","");
		addCommand("Library __exit(1), Rdummy","","");
		addCommand("Return 9999","","");
	}
	
	public void addCheckZero()
	{
		//checkZero
		addCommand("","","");
		
		addCommand("__checkZero:","","");
		addCommand("Move b, R01","","");
		addCommand("Compare 0, R01","","");		
		addCommand("JumpTrue _error_checkZero","",""); //  Size <= 0 
		addCommand("Return 9999","","");
		
		//err_checkZero
		addCommand("","","");
		addCommand("_error_checkZero:","","");
		addCommand("Library __println(str_err_checkZero), Rdummy","","");
		addCommand("Library __exit(1), Rdummy","","");
		addCommand("Return 9999","","");
	}
	
	
}
