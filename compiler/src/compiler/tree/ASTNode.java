package compiler.tree;

import utils.PropagatingVisitor;
import utils.Visitor;

/** The base class of all AST nodes in this package.
 */
public abstract class ASTNode {
	
	public int line;
	
	/** Accepts a visitor object as part of the visitor pattern.
	 * @param visitor A visitor.
	 */
	public abstract void accept(Visitor visitor);
	
	/** Accepts a propagating visitor parameterized by two types.
	 * 
	 * @param <DownType> The type of the object holding the context.
	 * @param <UpType> The type of the result object.
	 * @param visitor A propagating visitor.
	 * @param context An object holding context information.
	 * @return The result of visiting this node.
	 */
	public abstract <DownType, UpType> UpType accept(
			PropagatingVisitor<DownType, UpType> visitor, DownType context);
	
	protected ASTNode(int line) {
		this.line = line;
	}
	
	protected ASTNode() {
	}

	public int getLine() {
		return line;
	}

}