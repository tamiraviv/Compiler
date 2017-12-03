package compiler.tree;

public enum BasicType {

	VOID,
	INT,
	BOOLEAN,
	STRING,
	CLASS_ID,
	NULL;
	
	public String toString() {
		switch (this) {
		case VOID: return "void";
		case INT: return "int";
		case BOOLEAN: return "boolean";
		case STRING: return "string";
		case CLASS_ID: return "class";
		case NULL: return "null";
		
		default: throw new RuntimeException("Unexpected value: " + this.name());
		}
	}
}
