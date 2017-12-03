package compiler.tree;

import utils.PropagatingVisitor;
import utils.Visitor;


public class Type extends ASTNode
{

	public BasicType basicType;
	public String className;
	
	public int numOfArrays = 0;

	/**
	 * Constructs a new field node.
	 * 
	 * @param type
	 *            Data type of field.
	 * @param name
	 *            Name of field.
	 */
	public Type(int line, BasicType basicType) {
		super(line);
		this.basicType = basicType;
	}
	
	public Type(int line, BasicType basicType, String className) {
		super(line);
		this.className = className;
		this.basicType = basicType;
	}

	public Type(int line, int numOfArrays, BasicType basicType, String className) {
		super(line);
		this.numOfArrays = numOfArrays;
		this.className = className;
		this.basicType = basicType;
	}

	
	public BasicType getBasicType() {
		return basicType;
	}

	public String toString()
	{
		String t = "";
		
		if(basicType == BasicType.CLASS_ID)
			t += className;
		else
			t = basicType.toString();
		
		for(int i = 0; i < this.numOfArrays; i++)
			t += "[]";
		
		return t;
	}

	public int getNumOfArrays() {
		return numOfArrays;
	}
	
	public void setNumOfArrays()
	{
		numOfArrays++;
	}
	
	public void setBasicType(BasicType t)
	{
		this.basicType = t;
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
