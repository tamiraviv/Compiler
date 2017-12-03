package compiler.tree;

import java.util.List;

import utils.PropagatingVisitor;
import utils.Visitor;

/**
 * Class field AST node.
 * 
 * 
 */
public class Field extends ASTNode {

	public Type type;
	public List<String> idList;

	/**
	 * Constructs a new field node.
	 * 
	 * @param type
	 *            Data type of field.
	 * @param name
	 *            Name of field.
	 */
	
	public Field(int line, Type type, List<String> idList) 
	{
		super(line);
		this.type = type;
		this.idList = idList;
	}

	public Type getType() {
		return type;
	}

	public List<String> getIDList() {
		return idList;
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