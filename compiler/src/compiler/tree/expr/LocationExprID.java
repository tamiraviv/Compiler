package compiler.tree.expr;

import utils.PropagatingVisitor;
import utils.Visitor;

public class LocationExprID extends Location{
	
	public String id;
	public Expr expr;
	
	public LocationExprID(int line,String id)
	{
		super(line);
		this.id = id;
	}

	public LocationExprID(int line, String id , Expr expr)
	{
		this(line ,id);
		this.expr = expr;
		
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
