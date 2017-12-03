package utils;

import compiler.ICSym;

public class SymbolTranslate {
	
	public static String translate(int num)
	{
		switch(num){
		  case ICSym.DIVIDE:
			  return "/";
		  case ICSym.LCBR:
			  return "{";
		  case ICSym.LTE:
			  return "<=";
		  case ICSym.INTEGER:
			  return "Number";
		  case ICSym.SEMI:
			  return ";";
		  case ICSym.CONTINUE:
			  return "continue";
		  case ICSym.INT:
			  return "int";
		  case ICSym.MINUS:
			  return "-";
		  case ICSym.STATIC:
			  return "static";
		  case ICSym.LT:
			  return "<";
		  case ICSym.LP:
			  return "(";
		  case ICSym.COMMA:
			  return ",";
		  case ICSym.CLASS:
			  return "class";
		  case ICSym.RP:
			  return ")";
		  case ICSym.PLUS:
			  return "+";
		  case ICSym.MULTIPLY:
			  return "*";
		  case ICSym.ASSIGN:
			  return "=";
		  case ICSym.QUOTE:
			  return "Quote";
		  case ICSym.IF:
			  return "if";
		  case ICSym.THIS:
			  return "this";
		  case ICSym.ID:
			  return "Identifier";
		  case ICSym.DOT:
			  return ".";
		  case ICSym.BOOLEAN:
			  return "boolean";
		  case ICSym.EOF:
			  return "End of file";
		  case ICSym.RETURN:
			  return "return";
		  case ICSym.RCBR:
			  return "}";
		  case ICSym.LB:
			  return "[";
		  case ICSym.LAND:
			  return "&&";
		  case ICSym.EQUAL:
			  return "==";
		  case ICSym.TRUE:
			  return "true";
		  case ICSym.NEW:
			  return "new";
		  case ICSym.RB:
			  return "]";
		  case ICSym.LOR:
			  return "||";
		  case ICSym.NULL:
			  return "null";
		  case ICSym.MOD:
			  return "%";
		  case ICSym.BREAK:
			  return "break";
		  case ICSym.VOID:
			  return "void";
		  case ICSym.GTE:
			  return ">=";
		  case ICSym.ELSE:
			  return "else";
		  case ICSym.WHILE:
			  return "while";
		  case ICSym.NEQUAL:
			  return "!=";
		  case ICSym.CLASS_ID:
			  return "Class Name";
		  case ICSym.EXTENDS:
			  return "extends";
		  case ICSym.STRING:
			  return "String";
		  case ICSym.LNEG:
			  return "!";
		  case ICSym.FALSE:
			  return "false";
		  case ICSym.GT:
			  return ">";
		  case ICSym.LENGTH:
			  return "length";
		  default:
			  return "Illigal token";
		}
	}
}
