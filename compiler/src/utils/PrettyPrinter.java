package utils;

import compiler.tree.ASTNode;
import compiler.tree.BasicType;
import compiler.tree.ClassDecl;
import compiler.tree.Field;
import compiler.tree.FieldOrMethodList;
import compiler.tree.Formal;
import compiler.tree.Method;
import compiler.tree.Program;
import compiler.tree.Type;
import compiler.tree.expr.BinaryOpExpr;
import compiler.tree.expr.BooleanExpr;
import compiler.tree.expr.Expr;
import compiler.tree.expr.LengthExpr;
import compiler.tree.expr.LocationExprExpr;
import compiler.tree.expr.LocationExprID;
import compiler.tree.expr.LocationID;
import compiler.tree.expr.NewExpr;
import compiler.tree.expr.NullExpr;
import compiler.tree.expr.NumberExpr;
import compiler.tree.expr.ParenthesesExpr;
import compiler.tree.expr.StaticCall;
import compiler.tree.expr.ThisExpr;
import compiler.tree.expr.UnaryOpExpr;
import compiler.tree.expr.VarExpr;
import compiler.tree.expr.VirtualCall;
import compiler.tree.stmt.AssignStmt;
import compiler.tree.stmt.CallStmt;
import compiler.tree.stmt.ContinueBreakStmt;
import compiler.tree.stmt.DeclarationStmt;
import compiler.tree.stmt.IfStmt;
import compiler.tree.stmt.ReturnStmt;
import compiler.tree.stmt.ScopeStmt;
import compiler.tree.stmt.Stmt;
import compiler.tree.stmt.StmtList;
import compiler.tree.stmt.WhileStmt;

/** Pretty-prints an SLP AST.
 */
public class PrettyPrinter implements Visitor 
{
	protected final ASTNode root;
	private int depth = 0;
	
	/** Constructs a printin visitor from an AST.
	 * 
	 * @param root The root of the AST.
	 */
	public PrettyPrinter(ASTNode root) {
		this.root = root;
	}

	/** Prints the AST with the given root.
	 */
	public void print() {
		root.accept(this);
	}
	
	//Done
	@Override
	public void visit(Program program) 
	{
		System.out.println("Abstract Syntax Tree: \n");
		
		depth = 0;
		
		for (ClassDecl cd : program.classDecl_l)
		{
			cd.accept(this);
		}
	}

	//Done
	@Override
	public void visit(ClassDecl classDecl) 
	{
		depth += 2 ;
		
		System.out.print(classDecl.line + ": Declaration of class: " + classDecl.name);
		
		if(classDecl.superClassName != null) 
		{System.out.print(" Extends " + classDecl.superClassName );}
		
		System.out.println();
		for (Field field: classDecl.fields)
		{
			depth += 2 ;
			field.accept(this);
			depth -= 2 ;
		}
		
		for (Method method: classDecl.methods)
		{
			depth += 2 ;
			method.accept(this);
			depth -= 2 ;
		}

		depth -= 2 ;
	}

	//Done
	public void visit(Field field) 
	{
		indent();
		System.out.print(field.line + ": Declaration of fields from type: ");


		field.type.accept(this);

		depth++;
		for(int i=  field.idList.size()-1 ; i >= 0  ; i--)
		{
			indent();
			System.out.println(field.line + ": Field Id: " + field.idList.get(i));
		}
		depth--;

	}

	//Done
	@Override
	public void visit(Type type) 
	{
		if(type.numOfArrays > 0)
		{
			System.out.print(type.numOfArrays + "-dimensional array of ");
		}

		if(type.getBasicType() == BasicType.CLASS_ID)
			System.out.println(type.className);
		else
			System.out.println(type.getBasicType());
	}

	//Done
	@Override
	public void visit(Method method) 
	{
		indent();
		
		System.out.print(method.line + ": Declaration of ");
				
		if(method.isStatic)
		{
			System.out.print("static ");
		}
		
		System.out.println("method: " + method.name);
		
		depth+=2;
		
		indent();
		System.out.print(method.line + ": ");
		
		System.out.print("Method return value: ");
		method.type.accept(this);
		
		
		if(method.formals!=null)
		{
			for(int i=0 ; i < method.formals.size() ; i++)
			{
				method.formals.get(i).accept(this);
			}
		}
	

		method.statements.accept(this);
	
		depth-=2;
		
	}

	//Done
	@Override
	public void visit(DeclarationStmt declarationStmt)
	{
		indent(declarationStmt.line);
		System.out.print("Declaration of variable: " + declarationStmt.name + " from type: ");
		declarationStmt.type.accept(this);
	}

	//Done
	public void visit(StmtList stmts) 
	{
		for (Stmt stmt : stmts.statements) 
		{
			stmt.accept(this);
		}
	}

	//Done(Abstract)
	public void visit(Stmt stmt) 
	{
		throw new UnsupportedOperationException("Unexpected visit of stmt abstract class ");
	}
	

	//Done
	public void visit(AssignStmt stmt) 
	{
		
        //new variable : new AssignStmt(null,(Type)t,id,e); :}
		
		if(stmt.type != null) // Assignment + Variable deceleration . For Example "int a = b+c;"
		{
			indent(stmt.line);
			System.out.print("Declaration of variable(with initial value): " + stmt.name + " from type: ");
			stmt.type.accept(this);
			depth++;
			stmt.expr.accept(this);
	
			depth--;
		}
		else if(stmt.location!=null) // Only assignment to an existing variable!! for example "a[1] = b+c;"
		{
			indent(stmt.line);
			System.out.println("Assignment statement");
			depth++;
			stmt.location.accept(this);
			stmt.expr.accept(this);
			depth--;
		}
		
	}
	
	//Done(abstract class)
	public void visit(Expr expr) 
	{		
		throw new UnsupportedOperationException("Unexpected visit of Expr abstract class");		
	}	
	
	//Done
	//String expression
	public void visit(VarExpr expr) 
	{
		indent(expr.line);
		System.out.println("String literal: " + "\""+expr.name+"\"");
	}
	
	//Done
	public void visit(NumberExpr expr) 
	{
		indent(expr.line);
		System.out.println("Integer literal: " + expr.value);
	}

	//Done
	public void visit(UnaryOpExpr expr)
	{
		indent(expr.line);
		System.out.println("Unary operator: " + expr.op.toString());
		depth++;
		expr.operand.accept(this);
		depth--;
	}
	
	//Done
	public void visit(BinaryOpExpr expr) 
	{
		indent(expr.line);
		System.out.println("Binary operation: " + expr.op.toString());
		depth+=2;
		expr.lhs.accept(this);
		expr.rhs.accept(this);
		depth-=2;
	}

	//done
	public void visit(BooleanExpr booleanExpr) 
	{
		//Boolean literal: 
		indent(booleanExpr.line);
		System.out.println("Boolean literal: " + booleanExpr.value);	
	}

	//Done
	@Override
	public void visit(Formal field)
	{
		indent(field.line);
		System.out.print("Parameter: " + field.name + " from type: ");
		field.type.accept(this);
	}

	@Override
	public void visit(FieldOrMethodList fieldOrMethodList) 
	{
		// TODO Auto-generated method stub
		
	}

	//Done
	@Override
	public void visit(StaticCall call) 
	{
		indent(call.line);
		System.out.println("Call to static method: " + call.methodName + ", in class " + call.className);
		//System.out.println("size = " + call.exprList.size());

		depth++;
		for(int i=0 ; i < call.exprList.size() ; i++)
		{
			call.exprList.get(call.exprList.size() - i - 1).accept(this);
		}
		depth--;
	}

	@Override
	public void visit(VirtualCall virtualCall) 
	{
		indent(virtualCall.line);
		System.out.print("Call to virtual method: " + virtualCall.methodName);
	
		//The parameters of the call
		depth++;
		
		if(virtualCall.expr != null)
		{
			System.out.println(", in external scope");
			virtualCall.expr.accept(this);
		}
		else
		{
			System.out.println();
		}
		
		
		if(virtualCall.exprList!=null)
		{
			
			for(int i=0 ; i < virtualCall.exprList.size() ; i++)
			{
				virtualCall.exprList.get(virtualCall.exprList.size() - i - 1).accept(this);
			}
		}
		depth--;
	}

	//Done
	@Override
	public void visit(LocationExprID locationID) 
	{
		indent(locationID.line);
		System.out.print("Reference to variable: " + locationID.id);
		
		if(locationID.expr != null)
		{
			System.out.println(", in external scope");
			depth++;
			locationID.expr.accept(this);
			depth--;
		}
		else
		{
			System.out.println();
		}
	}


	//Done
	public void visit(LocationExprExpr locationIDExprExpr)
	{
		indent(locationIDExprExpr.line);
		System.out.println("Reference to array");
		depth++;
		locationIDExprExpr.expr1.accept(this);
		locationIDExprExpr.expr2.accept(this);
		depth--;
	}



	//Done
	public void visit(ReturnStmt returnStmt) 
	{
		indent(returnStmt.line);
		System.out.print("Return statement");
		
		if(returnStmt.expr == null)
		{
			System.out.println();
		}
		else
		{
			System.out.println(", with return value");
			depth++;
			returnStmt.expr.accept(this);
			depth--;
		}
	}


	//Done
	public void visit(ContinueBreakStmt contunueBreakStmt) 
	{
		indent(contunueBreakStmt.line);
		if(contunueBreakStmt.action == "break")
		{
			System.out.print("Break ");
		}
		else
		{
			System.out.print("Continue ");
		}
		
		System.out.println("statement");

		
	}


	//Done
	public void visit(ScopeStmt scopeStmt) 
	{
		indent(scopeStmt.line);
		
		System.out.println("Block of statements");
		depth += 2;
		scopeStmt.statements.accept(this);
		depth -= 2;
	}

	//Done
	public void visit(IfStmt ifStmt)
	{
		indent(ifStmt.line);
		System.out.println("If statement");
		depth+=2;
			ifStmt.expr.accept(this);
			ifStmt.stmt1.accept(this);
			
			if(ifStmt.stmt2!=null)
			{
				indent(ifStmt.line);
				System.out.println("else statement");
				depth+=1;
				ifStmt.stmt2.accept(this);
				depth-=1;
			}
		
		depth-=2;
		
	}
	
	public void visit(WhileStmt whileStmt) 
	{
		indent(whileStmt.line);
		System.out.println("While statement");
		depth+=2;
			whileStmt.expr.accept(this);
			whileStmt.stmt.accept(this);
		depth-=2;

	}

	//Done
	public void visit(ThisExpr thisExpr)
	{
		indent(thisExpr.line);
		System.out.println("This statement");
	}

	//Done
	public void visit(NewExpr newExpr) 
	{
		indent(newExpr.line);
		if(newExpr.classID != null)
		{
			System.out.println("Instantiation of class: " + newExpr.classID);
		}
		else
		{
			System.out.print("Array allocation from type: ");
			newExpr.type.accept(this);
			depth++;
			newExpr.expr.accept(this);
			depth--;
		}
	}

	//Done
	public void visit(LengthExpr lengthExpr)
	{
		indent(lengthExpr.line);
		System.out.println("Reference to array length");
		depth++;
		lengthExpr.expr.accept(this);
		depth--;
	}


	//Done
	public void visit(ParenthesesExpr parenthesesExpr) 
	{
		parenthesesExpr.expr.accept(this);
	}

	
	private void indent() 
	{
		for (int i = 0; i < depth; ++i)
			System.out.print(" ");
	}
	
	private void indent(int line) 
	{
		for (int i = 0; i < depth; ++i)
			System.out.print(" ");
		
		System.out.print(line + ": ");
	}

	//Done
	public void visit(CallStmt callStmt)
	{
		indent(callStmt.line);
		System.out.println("Method call statement");
		depth++;
		//Method call statement
		callStmt.call.accept(this);
		depth--;
	}

	@Override
	public void visit(NullExpr nullExpr) 
	{
		indent(nullExpr.line);
		System.out.println("Null statement");
	}

	@Override
	public void visit(LocationID locationID) {
		// TODO Auto-generated method stub
		
	}

}