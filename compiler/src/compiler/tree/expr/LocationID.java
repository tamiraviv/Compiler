package compiler.tree.expr;

import compiler.tree.BasicType;
import utils.PropagatingVisitor;
import utils.Visitor;

public class LocationID extends Location {

	public String id;
	
	public LocationID(int line,String id)
	{
		super(line);
		this.id = id;
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
	
	
	public String toString()
	{
		return id;
	}

}
