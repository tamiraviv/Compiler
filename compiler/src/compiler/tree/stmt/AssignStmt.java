package compiler.tree.stmt;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.*;
import compiler.tree.expr.*;

/**
 * An AST node for assignment statements.
 */
public class AssignStmt extends Stmt
{
	public final Location location;
	public final String name;
	public final Type type;
	public final Expr expr;
	
	public AssignStmt(int line, Location location,Type type,String name, Expr expr) 
	{
		super(line);
		this.location = location;
		this.type = type;
		this.expr = expr;
		this.name = name;
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