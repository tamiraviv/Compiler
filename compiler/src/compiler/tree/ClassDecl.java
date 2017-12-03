package compiler.tree;

import java.util.List;

import utils.PropagatingVisitor;
import utils.Visitor;

/**
 * Class declaration AST node.
 * 
 */

public class ClassDecl extends ASTNode 
{
	public String name;
	public String superClassName = null;
	public List<Field> fields;
	public List<Method> methods;

	/**
	 * Constructs a new class node.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public ClassDecl(int line, String name) 
	{
		super(line);
		this.name = name;
	}

	public ClassDecl(int line, String name, List<Field> fields,List<Method> methods) 
	{
		super(line);
		this.name = name;
		this.fields = fields;
		this.methods = methods;
	}

	/**
	 * Constructs a new class node, with a superclass.
	 * 
	 * @param line
	 *            Line number of class declaration.
	 * @param name
	 *            Class identifier name.
	 * @param superClassName
	 *            Superclass identifier name.
	 * @param fields
	 *            List of all fields in the class.
	 * @param methods
	 *            List of all methods in the class.
	 */
	public ClassDecl(int line, String name, String superClassName,List<Field> fields, List<Method> methods) 
	{
		this(line, name, fields, methods);
		this.superClassName = superClassName;
		
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
