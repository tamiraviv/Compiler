package compiler.tree.expr;

import utils.PropagatingVisitor;
import utils.Visitor;

public class LocationExprExpr extends Location{
	public Expr expr1;
	public Expr expr2;
	
	public LocationExprExpr(int line, Expr expr1, Expr expr2)
	{
		super(line);
		this.expr1 = expr1;
		this.expr2 = expr2;
	}
	
	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
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
		return null; //visitor.visit(this, context);
	}	

}
