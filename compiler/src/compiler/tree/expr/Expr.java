package compiler.tree.expr;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.ASTNode;
import compiler.tree.Type;


/** A base class for AST nodes for expressions.
 */
public class Expr extends ASTNode 
{
	public enum ParamType{
		Immediate,
		Reg,
		Memory
	}
	
	public Type staticType;
	public String resultParam;
	public String resultParamField = "";
	public String resultParamArray = "";
	public ParamType resultParamType;
	
	public Expr(int line) 
	{
		super(line);
	}

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