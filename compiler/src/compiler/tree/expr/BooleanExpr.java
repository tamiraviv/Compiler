package compiler.tree.expr;

import utils.PropagatingVisitor;
import utils.Visitor;

public class BooleanExpr extends Expr 
{

	public final boolean value;
	
	public BooleanExpr(int line,String s)
	{
		super(line);
		Boolean b = new Boolean(s);
		this.value = b.booleanValue();
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

