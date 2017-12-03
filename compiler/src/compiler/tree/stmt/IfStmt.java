package compiler.tree.stmt;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.expr.Expr;

/**
 * An AST node for assignment statements.
 */
public class IfStmt extends Stmt
{
	public final Expr expr;
	public final Stmt stmt1;
	public final Stmt stmt2;
	

	public IfStmt(int line, Expr expr,Stmt stmt1,Stmt stmt2)
	{
		super(line);
		this.expr = expr;
		this.stmt1 = stmt1;
		this.stmt2 = stmt2;
	}

	/**
	 * Accepts a visitor object as part of the visitor pattern.
	 * 
	 * @param visitor
	 *            A visitor.
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	/** Accepts a propagating visitor parameterized by two types.
	 * 
	 * @param <DownType> The type of the object holding the context.
	 * @param <UpType> The type of the result object.
	 * @param visitor A propagating visitor.
	 * @param context An object holding context information.
	 * @return The result of visiting this node.
	 */
	@Override
	public <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) {
		return visitor.visit(this, context);
	}
}

