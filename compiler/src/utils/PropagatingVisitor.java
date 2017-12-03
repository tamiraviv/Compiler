package utils;

import compiler.tree.*;
import compiler.tree.expr.*;
import compiler.tree.stmt.*;


/** An interface for a propagating AST visitor.
 * The visitor passes down objects of type <code>DownType</code>
 * and propagates up objects of type <code>UpType</code>.
 */
public interface PropagatingVisitor<DownType,UpType> {
	public UpType visit(ClassDecl classDecl, DownType d);
	public UpType visit(Program program, DownType d);
	public UpType visit(StmtList stmts, DownType d);
	public UpType visit(Stmt stmt, DownType d);
	public UpType visit(PrintStmt stmt, DownType d);
	public UpType visit(AssignStmt stmt, DownType d);
	public UpType visit(Expr expr, DownType d);
	public UpType visit(VarExpr expr, DownType d);
	public UpType visit(NumberExpr expr, DownType d);
	public UpType visit(UnaryOpExpr expr, DownType d);
	public UpType visit(BinaryOpExpr expr, DownType d);
	public UpType visit(Formal field, DownType d);
	public UpType visit(Method method, DownType d);
	public UpType visit(Type type, DownType d);
	public UpType visit(FieldOrMethodList type, DownType d);
	public UpType visit(Field field, DownType d);
	public UpType visit(BooleanExpr booleanExpr, DownType d);
	public UpType visit(ReturnStmt returnStmt, DownType d);
	public UpType visit(ContinueBreakStmt contunueBreakStmt, DownType d);
	public UpType visit(ScopeStmt scopeStmt, DownType d);
	public UpType visit(IfStmt ifStmt, DownType d);
	public UpType visit(WhileStmt whileStmt, DownType d);
	public UpType visit(DeclarationStmt declarationStmt, DownType d);
	public UpType visit(ThisExpr thisExpr, DownType d);
	public UpType visit(NewExpr newExpr, DownType d);
	public UpType visit(LengthExpr lengthExpr, DownType d);
	public UpType visit(ParenthesesExpr parenthesesExpr, DownType d);


}