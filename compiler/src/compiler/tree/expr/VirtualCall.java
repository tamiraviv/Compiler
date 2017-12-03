package compiler.tree.expr;

import java.util.List;

import utils.PropagatingVisitor;
import utils.Visitor;

public class VirtualCall extends Call
{

	public Expr expr = null;
	public String methodName;
	public List<Expr> exprList;
	
	public VirtualCall(int line, String methodName, List<Expr> exprList)
	{
		super(line);
		this.methodName = methodName;
		this.exprList = exprList;
	}
	
	public VirtualCall(int line, Expr expr, String methodName, List<Expr> exprList)
	{
		this(line,methodName, exprList);
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
