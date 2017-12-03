package compiler.tree.stmt;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.Type;

/**
 * An AST node for assignment statements.
 */
public class DeclarationStmt extends Stmt
{
	public final String name;
	public final Type type;
	

	public DeclarationStmt(int line, Type type,String name)
	{
		super(line);
		this.name = name;
		this.type = type;

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

