package utils;

import compiler.tree.*;
import compiler.tree.expr.*;
import compiler.tree.stmt.*;


/** An interface for AST visitors.
 */
public interface Visitor {
	public void visit(CallStmt callStmt);
	public void visit(ClassDecl classDecl);
	public void visit(Program program);
	public void visit(StmtList stmts);
	public void visit(Stmt stmt);
	public void visit(AssignStmt stmt);
	public void visit(Expr expr);
	public void visit(VarExpr expr);
	public void visit(NumberExpr expr);
	public void visit(UnaryOpExpr expr);
	public void visit(BinaryOpExpr expr);
	public void visit(Formal field);
	public void visit(Method method);
	public void visit(Type type);
	public void visit(FieldOrMethodList fieldOrMethodList);
	public void visit(Field field);
	public void visit(StaticCall call);
	public void visit(VirtualCall virtualCall);
	public void visit(LocationID locationID);
	public void visit(LocationExprID locationExprID);
	public void visit(LocationExprExpr locationIDExprExpr);
	public void visit(BooleanExpr booleanExpr);
	public void visit(ReturnStmt returnStmt);
	public void visit(ContinueBreakStmt contunueBreakStmt);
	public void visit(ScopeStmt scopeStmt);
	public void visit(IfStmt ifStmt);
	public void visit(WhileStmt whileStmt);
	public void visit(DeclarationStmt declarationStmt);
	public void visit(ThisExpr thisExpr);
	public void visit(NewExpr newExpr);
	public void visit(LengthExpr lengthExpr);
	public void visit(ParenthesesExpr parenthesesExpr);
	public void visit(NullExpr nullExpr);

	
	
}