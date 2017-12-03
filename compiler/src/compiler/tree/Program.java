package compiler.tree;

import java.util.List;

import utils.PropagatingVisitor;
import utils.Visitor;

/** An AST node for a list of statements.
 */
public class Program extends ASTNode 
{
	public List<ClassDecl> classDecl_l;
	
	public Program(List<ClassDecl> cd_l) 
	{
		super(0);
		this.classDecl_l = cd_l;
	}
	
	public List<ClassDecl> getClasses() 
	{
		return classDecl_l;
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