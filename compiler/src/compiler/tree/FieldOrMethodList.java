package compiler.tree;

import java.util.List;
import java.util.ArrayList;

import utils.PropagatingVisitor;
import utils.Visitor;

public class FieldOrMethodList extends ASTNode
{
	public List<Method> methodList;
	public List<Field> fieldList;

	public FieldOrMethodList() 
	{
		this.fieldList = new ArrayList<Field>(); 
		this.methodList = new ArrayList<Method>();
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
		return visitor.visit(this, context);
	}
	
}
