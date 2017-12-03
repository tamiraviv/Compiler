package compiler.tree.stmt;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.ASTNode;


/** The super class of all AST node for program statements.
 */
public abstract class Stmt extends ASTNode
{
	
	
	public Stmt(int line) 
	{
		super(line);
	}



	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
	 */
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}



	@Override
	public <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context) {
		return visitor.visit(this, context);
	}
}