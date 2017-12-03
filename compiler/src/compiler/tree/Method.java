package compiler.tree;

import java.util.List;

import utils.PropagatingVisitor;
import utils.Visitor;
import compiler.tree.stmt.StmtList;

/**
 * Abstract base class for method AST nodes.
 * 
 * 
 */
public class Method extends ASTNode {

	public Type type;
	public String name;
	public List<Formal> formals;
	public StmtList statements;
	public boolean isStatic;
	public String className;

	/**
	 * Constructs a new method node. Used by subclasses.
	 * 
	 * @param type
	 *            Data type returned by method.
	 * @param name
	 *            Name of method.
	 * @param formals
	 *            List of method parameters.
	 * @param statements
	 *            List of method's statements.
	 */
	public Method(int line,Type type, String name, List<Formal> formals,StmtList statements) 
	{
		super(line);
		this.type = type;
		this.name = name;
		this.formals = formals;
		this.statements = statements;
	}

	public Type getType() {
		return type;
	}
	
	public void setType(Type t) {
		this.type = t;
	}

	public String getName() {
		return name;
	}


	public StmtList getStatements() {
		return statements;
	}
	
	public boolean getIsStatic() {
		return isStatic;
	}
	
	
	public void setStatic()
	{
		isStatic = true;
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