package compiler.semanticAnalysis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utils.Visitor;
import compiler.tree.*;
import compiler.tree.expr.*;
import compiler.tree.stmt.*;

public class SemanticAnalyser implements Visitor 
{
	public static boolean exitOnSemanticError = true;
	int loop=0;
	boolean debug = false; //For debugging purposes. [Turn OFF/ON]
	String currentClass="";
	Method currentMethod;
	
	SymbolTable st;
	TypeTable tt;
	
	protected final ASTNode root;
	
	public SemanticAnalyser(ASTNode root)
	{
		this.root = root;
		tt= new TypeTable(root);
		st = new SymbolTable();
	}
	
	public void analyse() 
	{
		root.accept(this);
	}
	
	@Override
	public void visit(Expr expr) 
	{
		throw new UnsupportedOperationException("Unexpected visit of Expr abstract class");		
	}

	@Override
	public void visit(VarExpr expr) {
		expr.staticType = new Type(0, BasicType.STRING);
		
	}

	@Override
	public void visit(NumberExpr expr)
	{
		expr.staticType = new Type(0, BasicType.INT);
	}

	@Override
	public void visit(UnaryOpExpr expr) 
	{
		expr.operand.accept(this);
		
		if(expr.op == Operator.MINUS)
		{
			if(expr.operand.staticType.basicType != BasicType.INT)
				PrintError("Unary operator: '" + expr.op + "' accepts only integers" ,expr.line);
			expr.staticType = new Type (0,expr.operand.staticType.basicType);
		}
		else if(expr.op == Operator.LNEG)
		{
			if(expr.operand.staticType.basicType != BasicType.BOOLEAN)
				PrintError("Unary operator: '" + expr.op + "' accepts only booleans" , expr.line );
			expr.staticType = new Type (0,expr.operand.staticType.basicType);
		}
		else
			throw new UnsupportedOperationException("Unrecognised unary operator");		
			
		
	}
	
	@Override
	public void visit(BinaryOpExpr expr)
	{ 
		expr.lhs.accept(this); 
		expr.rhs.accept(this); 

		if(expr.op == Operator.PLUS) 
		{ 
			if(expr.lhs.staticType.basicType == BasicType.INT && expr.rhs.staticType.basicType == BasicType.INT) 
				expr.staticType = new Type(0, BasicType.INT); 
			else if(expr.lhs.staticType.basicType == BasicType.STRING && expr.rhs.staticType.basicType == BasicType.STRING) 
				expr.staticType = new Type(0, BasicType.STRING);
			else PrintError( "Binary operator '" + expr.op + "' accepts only integers and strings (both operands should be of the same type)" ,expr.line);
		} 
		else if(expr.op == Operator.MINUS || expr.op == Operator.DIVIDE|| expr.op == Operator.MULTIPLY || expr.op == Operator.MOD) 
		{ 
			if(expr.lhs.staticType.basicType != BasicType.INT || expr.rhs.staticType.basicType != BasicType.INT)
			{
				PrintError( "Binary operator '" + expr.op + "' accepts only integers" , expr.line);
			}
			expr.staticType = new Type(0, BasicType.INT);
		} 
		else if(expr.op == Operator.GT || expr.op == Operator.GTE || expr.op == Operator.LT || expr.op == Operator.LTE) 
		{ 
			if(expr.lhs.staticType.basicType != BasicType.INT || expr.rhs.staticType.basicType != BasicType.INT)
				PrintError( "'" + expr.op + "' accepts only integers" ,expr.line);
			expr.staticType = new Type(0, BasicType.BOOLEAN);
		} 
		else if(expr.op == Operator.EQUAL || expr.op == Operator.NEQUAL) 
		{ 
			if(!tt.compareTypes(expr.lhs.staticType, expr.rhs.staticType) && !tt.compareTypes(expr.rhs.staticType,expr.lhs.staticType)) 
			{ //Not from the same type and not Subclass of one another(Including null)
				PrintError("Incompatible operand types " + expr.lhs.staticType + " and " + expr.rhs.staticType + "." , expr.line);
			}
			expr.staticType = new Type(0, BasicType.BOOLEAN);
		} 
		else if(expr.op == Operator.LAND || expr.op == Operator.LOR ) // "&&" or "||" 
		{ 
			if(expr.lhs.staticType.basicType!=BasicType.BOOLEAN || expr.rhs.staticType.basicType!=BasicType.BOOLEAN) 
			{ 
				PrintError( "The operator is undefined for the types " + expr.lhs.staticType + " and " + expr.rhs.staticType + "." , expr.line);
			} 
			expr.staticType = new Type(0, BasicType.BOOLEAN);
			} 
		else 
			throw new UnsupportedOperationException("Unrecognized binary operator"); 
	}

	@Override
	public void visit(Formal field) 
	{
		throw new UnsupportedOperationException("Unexpected visit of 'visit(Formal field)'");
		
	}

	@Override
	public void visit(Method method) 
	{
		// new scope
		st.pushScope();
		
		currentMethod = method;
		//currentMethodType = method.isStatic ? MethodType.STATIC : MethodType.VIRTUAL;

		if(method.formals != null)
		{
			for(Formal formal :  method.formals) //Add all the method parameters to the current scope 
			{
				st.tryAddSymbol(formal.type ,formal.name,true,formal.line);
			}
		}
		method.statements.accept(this);

		if(method.type.basicType != BasicType.VOID  &&  !alwaysReturnValue(new ScopeStmt(0, method.statements)))
		{
			PrintError("Not all path necessarily return a value." , method.line);
		}
		
		// exit scope
		currentMethod = null;
		st.popScope();
	}
	
	@Override
	public void visit(StmtList stmts) 
	{
		if(stmts.statements!=null)
		{
			for (Stmt stmt : stmts.statements) 
			{
				stmt.accept(this);
			}
		}
		
	}

	@Override
	public void visit(Stmt stmt) 
	{
		throw new UnsupportedOperationException("Unexpected visit of stmt abstract class ");
	}
	
	@Override
	public void visit(CallStmt callStmt) 
	{
		callStmt.call.accept(this);
	}

	@Override
	public void visit(ClassDecl classDecl) 
	{
		
		st.pushScope();
		
		currentClass = classDecl.name;
		
		//Run verifications on Static methods Before Class Fields were added to the Scope.
		if(classDecl.methods!=null)
			for (Method method: classDecl.methods)
			{
				if(method.isStatic)
					method.accept(this);
			}
		
		//st.pushScope();
		
		//Add Class Fields to the class Scope
		if(tt.FieldsInClass.containsKey(currentClass))
			for (Field field: tt.FieldsInClass.get(currentClass))
			{
				field.accept(this);
			}

		//Run verifications on Non-Static methods after Class Fields were added to  the scope
		if(classDecl.methods!=null)
			for (Method method: classDecl.methods)
			{
				if(!method.isStatic)
					method.accept(this);
			}
		
		currentClass = "";
		
		//st.popScope();
		st.popScope();
	}

	@Override
	public void visit(Program program) 
	{
		loop = 0;
		if(program.classDecl_l!=null)
		for (ClassDecl cd : program.classDecl_l)
		{
			cd.accept(this);
		}
		
	}
	
	@Override
	public void visit(AssignStmt stmt) 
	{
		Type targetType;
		//Accept the expression on the right side of the assignment first 
		stmt.expr.accept(this);
		
		
		//Declaration with Assignment;		[// stmt ::= type ID ASSIGN expr SEMI]  For example:  "int a = b;"
		if(stmt.location == null)	
		{//Add Symbol to Scope
			//Try to Add variable ID to symbol table in the current scope + Initialize the variable because it was assigned
			st.tryAddSymbol(stmt.type, stmt.name, true, stmt.line);
			
			targetType = stmt.type;

		}
		//Only Assignment(Without Declaration);		[// stmt ::= location ASSIGN expr SEMI] For example:  "a = b;"
		else
		{
			//We only initialize the Symbol
			if(stmt.location.getClass() == LocationID.class)  //For example: a = b;
			{
				st.tryInitSymbol(((LocationID)stmt.location).id);
			}

			stmt.location.accept(this);
			
			targetType = stmt.location.staticType;
		}
		
		//Check assignment types.  [check whether the assignment is up-casting]
		if(!tt.compareTypes(targetType, stmt.expr.staticType))
		{
			
			PrintError("Cannot convert from type " + stmt.expr.staticType + " to " + targetType, stmt.line);
		}	
	}

	@Override
	public void visit(Type type) 
	{
		throw new UnsupportedOperationException("Unexpected visit of 'visit(Type type)'");
		
	}

	@Override
	public void visit(FieldOrMethodList fieldOrMethodList) 
	{
		throw new UnsupportedOperationException("Unexpected visit of 'visit(FieldOrMethodList fieldOrMethodList)'");
		
	}

	@Override
	public void visit(Field field) 
	{ 
		for(String id: field.idList) //Add all Class fields to current Scope 
		{ 
			st.tryAddSymbol(field.type ,id,false,field.line); 
			st.tryInitSymbol(id);		
		}
	}

	@Override
	public void visit(StaticCall call) 
	{
		//System.out.println("StaticCall, ClassName = " + call.className + " , " + call.methodName);
		List<Formal> f = new ArrayList<>();
		Type t;
		if(call.exprList!=null)
		{
			for(int i=0 ; i < call.exprList.size() ; i++)
			{
				call.exprList.get(i).accept(this);
			}
			
		}
		
		for(int i=0 ; i < call.exprList.size() ; i++)
		{
			t = call.exprList.get(i).staticType;
			f.add(new Formal(0,t,null));
		}
		
		
		Method m = new Method(call.line,null,call.methodName,f,null);
		t = tt.isLegalMethod(call.className,true,false,false,m);
			
		if(t==null)
		{
			PrintError("Call is not legal" , call.line);
		}
		
		call.staticType = tt.getMethodType(call.className, call.methodName);
		//call.staticType = t;
				
		if(call.staticType==null)
		{
			PrintError("Call '" + call.methodName + "' Doesn't exist in Class '" + call.className + "'" , call.line);
		}
		//
		
	}
	
	@Override
	public void visit(VirtualCall virtualCall) 
	{
		if(virtualCall.expr == null) //Direct call.  [For example: "fun1();" in opposed  to "a.fun1();" or"A.fun1();"]
		{ 
			Method method = tt.getMethod(currentClass, virtualCall.methodName); //Get Method object from the current class
			if(method != null)  //Method was found in current class
			{
				if(method.isStatic) //Call for a Static Method
				{
					PrintError("Static method must be referenced through its class name.",virtualCall.line);
				} 
				else //Call for a Virtual method
				{
					if(currentMethod.isStatic) //From a static Method
					{
						PrintError("Calling a Virtual method from a Static method is IIlegal.",virtualCall.line);
					}
				}
			}
		}
		
		
		if(virtualCall.expr != null)
		{
			//System.out.println(", in external scope");
			virtualCall.expr.accept(this);
			if(virtualCall.expr.staticType.basicType != BasicType.CLASS_ID)
			{
				PrintError("The type of this call should be Class" , virtualCall.line);
			}
		}
		
		
		if(virtualCall.exprList!=null)
		{
			
			for(int i=0 ; i < virtualCall.exprList.size() ; i++)
			{
				virtualCall.exprList.get(i).accept(this);
			}
		}
		
		List<Formal> f = new ArrayList<>();
		Type t;
		
		
		for(int i=0 ; i < virtualCall.exprList.size() ; i++)
		{
			t = virtualCall.exprList.get(i).staticType;
			f.add(new Formal(0,t,null));
		}
		
		

		Method m = new Method(virtualCall.line,null,virtualCall.methodName,f,null);
		
		if(virtualCall.expr==null)
		{		
			if(currentMethod.isStatic)
			{
				t = tt.isLegalMethod(currentClass,false,false,true,m);
			}
			else
			{
				t = tt.isLegalMethod(currentClass,false,false,false,m);
			}
		}
		else
		{
			t = tt.isLegalMethod(virtualCall.expr.staticType.className,false,true,false,m);
		}
			
		if(t==null)
		{
			PrintError("Call is not legal" , virtualCall.line);
		}
		
		virtualCall.staticType = t;
		
		
	}
	
	@Override
	public void visit(LocationExprID locationExprID) 
	{
		locationExprID.expr.accept(this);
		
		//Verification that a Field exist in the subExpression, for example: "a.f;" verify that "f" exist in "a"
		if((locationExprID.expr.staticType !=null && locationExprID.expr.staticType.basicType == BasicType.CLASS_ID) || locationExprID.expr.getClass()==ThisExpr.class)
		{//SubExpression is "this" or From Class Type
			String className;

			if(locationExprID.expr.getClass()==ThisExpr.class) 
			{// The SubExprssion is "This". For example: "This.blabla"
				className = currentClass;
			}
			else
			{//The subExpression is a class
				className = locationExprID.expr.staticType.className;
			}
			
			locationExprID.staticType = tt.getFieldType(className, locationExprID.id); //Resolve the type of the current ID

			if(locationExprID.staticType == null) //Class doesn't contain this Field
			{
				PrintError( "Type '" + className + "' Doesn't contain Field: '" + locationExprID.id + "'." , locationExprID.line);
			}
		}
		else
		{//The subExpression Doesn't have any fields at all!  (Could be array or a Field which doesn't exist)
			PrintError("'" + locationExprID.id	+ "' Field doesn't exist" , locationExprID.line);
		}
		
	}
	
	@Override
	public void visit(LocationID locationID) 
	{//Only the most left variable visit here. for example: a.b.c = 5;   only "a" expression will get here!
		if(debug) System.out.println("locationID : " + locationID.id);
		
		if(!st.isDeclared(locationID.id)) // Check if the variable was declared
		{
			PrintError("Variable '" + locationID.id + "' is not declared" , locationID.line);
		}
		else
		{
			locationID.staticType = st.getSymbolType(locationID.id); //Resolve the Type of the id from the SymbolTable
			
			if(!st.isInitialised(locationID.id))
				PrintError( "Variable '" + locationID.id + "' may not have been initialized" , locationID.line);
		}
		
		if(debug) System.out.println("locationID : " + locationID.id + " ClassName = " + locationID.staticType.className);
	}

	@Override
	public void visit(LocationExprExpr locationExprExpr) 
	{
		Type newType,beforeType;
		
		//if(debug)System.out.println("**Visit LocationExprExpr");
		locationExprExpr.expr1.accept(this);
		beforeType = locationExprExpr.expr1.staticType;
		
		newType = new Type(beforeType.line,beforeType.numOfArrays - 1, beforeType.basicType, beforeType.className);
		locationExprExpr.staticType = newType;
		
		if(newType.numOfArrays < 0)
		{
			PrintError("Out of array dimention bounds!" , locationExprExpr.line);

		}

		locationExprExpr.expr2.accept(this);
		if(locationExprExpr.expr2.staticType.basicType != BasicType.INT)
		{
			PrintError( "Array index can only be integer!" , locationExprExpr.line);
		}
	}
	
	@Override
	public void visit(BooleanExpr booleanExpr) 
	{
		booleanExpr.staticType = new Type(0, BasicType.BOOLEAN);
	}
	
	@Override 
	public void visit(ReturnStmt returnStmt)
	{ 
		if(currentMethod.type.basicType == BasicType.VOID) //Void method 
		{
			if(returnStmt.expr != null) //Method is void however return value exist 
			{
			 	PrintError( "Void methods cannot return a value" , returnStmt.line); 
			}
		 }
		 else //Not a void method 
		{
			 if(returnStmt.expr == null) // "return;" 
			{
				 PrintError("This method must return a result of type " + currentMethod.type , returnStmt.line); 
			} 
			else // "retrun 5;" --> compare return type 
			{
				returnStmt.expr.accept(this); //Resolve expression type
				if(!tt.compareTypes(currentMethod.type, returnStmt.expr.staticType)) 
				{ //Resolve the Type String 
					String returnType = returnStmt.expr.staticType.toString() ;
					String methodType = currentMethod.type.toString() ;
					PrintError("Type mismatch: cannot convert from " + returnType + " to " + methodType , returnStmt.line);
				}
			}
		}
	}

	@Override
	public void visit(ContinueBreakStmt continueBreakStmt)
	{
		if(loop<1)
		{
			PrintError( continueBreakStmt.action +" must be inside a loop" , continueBreakStmt.line);
		}
		
	}

	@Override
	public void visit(ScopeStmt scopeStmt) 
	{
		// new scope
		st.pushScope();

		scopeStmt.statements.accept(this);

		// exit scope
		st.popScope();
	}

	@Override
	public void visit(IfStmt ifStmt) 
	{
		ifStmt.expr.accept(this);
		
		if(ifStmt.expr.staticType.basicType != BasicType.BOOLEAN)
		{
			PrintError( "IF expression must be a boolean type" , ifStmt.line);
		}
		//Get UnInitialized variables
		Set<String> unInitilizedVarsBefore_if = getUnInitializedVars();
		
		ifStmt.stmt1.accept(this);

		//Get Changed variables
		Set<String> initilizedVarsAfter_if = getInitializedVars(unInitilizedVarsBefore_if);

		// UnInitialize the variables back
		unInitializeVars(initilizedVarsAfter_if); 
		
		if(ifStmt.stmt2!=null) //Else statement Exist
		{
			//Get UnInitialized variables
			Set<String> unInitilizedVarsBefore_else = getUnInitializedVars();
			
			ifStmt.stmt2.accept(this);
			//Get Changed variables
			Set<String> initilizedVarsAfter_else = getInitializedVars(unInitilizedVarsBefore_else);
			// UnInitialize the variables back
			unInitializeVars(initilizedVarsAfter_else); // UnInitialize the variables back..
			
			//Initialize variables which were initialized for sure
			initializeIntersectionVars(initilizedVarsAfter_if,initilizedVarsAfter_else);
		}
		
	}
	
	private void initializeIntersectionVars(Set<String> varsSet_1,Set<String> varsSet_2)
	{
		for(String id : varsSet_1) // Initialize the variables 
		{
			if(varsSet_2.contains(id))
			{
				st.map.get(id).peek().isInitialized = true;
			}
		}

		for(String id : varsSet_2) // Initialize the variables 
		{
			if(varsSet_1.contains(id))
			{
				st.map.get(id).peek().isInitialized = true;
			}
		}
	}
	
	private void unInitializeVars(Set<String> varsSet)
	{
		for(String id : varsSet) // UnInitialize the variables back.. 
		{
			st.map.get(id).peek().isInitialized = false;
		}
	}

	private Set<String> getInitializedVars(Set<String> getUnInitilizedVars)
	{
		Set<String> initilizedVars = new HashSet<String>();
		
		for(String id : getUnInitilizedVars)
		{
			if(st.isInitialised(id) )
			{
				initilizedVars.add(id);
			}
		}
		
		return initilizedVars;
	}
	
	private Set<String> getUnInitializedVars()
	{
		Set<String> unInitilizedVars = new HashSet<String>();
		
		for(String id : st.map.keySet())
		{
			if(!st.map.get(id).isEmpty() && !st.map.get(id).peek().isInitialized)
			{
				unInitilizedVars.add(id);
			}
		}
		return unInitilizedVars;
	}

	@Override
	public void visit(WhileStmt whileStmt) 
	{
		loop++;
		
		whileStmt.expr.accept(this);
		
		if(whileStmt.expr.staticType.basicType != BasicType.BOOLEAN)
		{
			PrintError( "WHILE expression must be a boolean type" , whileStmt.line);
		}

		//Get UnInitialized variables
		Set<String> unInitilizedVarsBefore_while = getUnInitializedVars();

		whileStmt.stmt.accept(this);
		
		//Get Changed variables
		Set<String> initilizedVarsAfter_while = getInitializedVars(unInitilizedVarsBefore_while);
		// UnInitialize the variables back
		unInitializeVars(initilizedVarsAfter_while); 
		
		loop--;
	}

	@Override
	public void visit(DeclarationStmt declarationStmt) 
	{
		if(declarationStmt.type.className != null && declarationStmt.type.className.equals("Library"))
		{
			PrintError("Can't be and instance of Library" , declarationStmt.line);
		}
		st.tryAddSymbol(declarationStmt.type, declarationStmt.name, false, declarationStmt.line);
	}

	@Override
	public void visit(ThisExpr thisExpr)
	{
		if(currentMethod.isStatic)
			PrintError("Instanse fields and methods can not be called by a static function" , thisExpr.line);
		
		thisExpr.staticType = new Type(thisExpr.line, BasicType.CLASS_ID,currentClass);		
	}

	@Override
	public void visit(NewExpr newExpr) 
	{
		if(newExpr.classID == null)
		{
			//newExpr.type.accept(this);
			newExpr.expr.accept(this);
			
			if(newExpr.expr.staticType.basicType != BasicType.INT)
			{
				PrintError( "Array size can only be integer!" , newExpr.line);
			}
		}
		
		if(newExpr.type != null)
		{
			newExpr.staticType = new Type(0, newExpr.type.basicType);
			newExpr.staticType.numOfArrays = newExpr.type.numOfArrays;
			if(newExpr.type.className!=null)
			{
				newExpr.staticType.className = newExpr.type.className;
			}
			newExpr.staticType.setNumOfArrays(); 
		}
		else
		{
			if(newExpr.classID != null && newExpr.classID.equals("Library"))
			{
				PrintError("Can't create instance of Library" , newExpr.line);
			}		
			newExpr.staticType = new Type(0, BasicType.CLASS_ID, newExpr.classID);
		}
		
		
	}	
	
	public void visit(LengthExpr lengthExpr)
	{ 
		lengthExpr.expr.accept(this);
		if(lengthExpr.expr.staticType.numOfArrays == 0)
			PrintError( ".length is an \"array only\" field" , lengthExpr.line);
		else 
			lengthExpr.staticType = new Type(lengthExpr.line, BasicType.INT);
	}

	@Override
	public void visit(ParenthesesExpr parenthesesExpr) 
	{
		throw new UnsupportedOperationException("Unexpected visit of 'visit(ParenthesesExpr parenthesesExpr)'");
		
	}

	@Override
	public void visit(NullExpr nullExpr) 
	{
		nullExpr.staticType = new Type(0, BasicType.NULL);
	}

	public boolean alwaysReturnValue(Stmt stmt)
	{
		if(stmt == null) //For example: stmt2 of IfStmt was not defined
		{
			return false; 
		}
		else if(stmt.getClass()==ReturnStmt.class) //Return statement
		{
			return true; //Found return statement
		}
		else if(stmt.getClass() == ScopeStmt.class) //List of statements
		{
			//In case one statement in the list does always return a value then return true
			if(((ScopeStmt)stmt).statements!=null && ((ScopeStmt)stmt).statements.statements!=null )
			{
				for(Stmt stmtInList : ((ScopeStmt)stmt).statements.statements)
				{
					if(alwaysReturnValue(stmtInList))
						return true; //Found a statement that always return a value
				}
			}
			return false; //Statement list doesn't exist
		}
		else if(stmt.getClass() == IfStmt.class) //IfStmt : Check his two Stmts
		{
			if(alwaysReturnValue(((IfStmt)stmt).stmt1) && alwaysReturnValue(((IfStmt)stmt).stmt2))
				return true; //Both IF and Else, always return a value
		}
		
		return false; //For all other Stmts - Return false
	}

	public static void PrintError(String error, int line)
	{
		if(line > 0)
		{
			System.out.println("Line " + line + ": Semantic error: " + error);
		}
		else
		{
			System.out.println("Semantic error: " + error);
		}
		
		
		if(exitOnSemanticError)
		{
			System.exit(-1);
		}
	}

}
