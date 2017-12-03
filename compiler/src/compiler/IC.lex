/***************************/
/* FILE NAME: LEX_FILE.lex */
/***************************/

/***************************/
/* AUTHOR: OREN ISH SHALOM */
/***************************/

/*************/
/* USER CODE */
/*************/
   
package compiler;
import java_cup.runtime.*;

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/
      
%%
   
/************************************/
/* OPTIONS AND DECLARATIONS SECTION */
/************************************/
   
/*****************************************************/ 
/* Lexer is the name of the class JFlex will create. */
/* The code will be written to the file Lexer.java.  */
/*****************************************************/ 
%class Lexer

%eofval{
  return new java_cup.runtime.Symbol(ICSym.EOF);
%eofval}

/********************************************************************/
/* The current line number can be accessed with the variable yyline */
/* and the current column number with the variable yycolumn.        */
/********************************************************************/
%line
%column
    
/******************************************************************/
/* CUP compatibility mode interfaces with a CUP generated parser. */
/******************************************************************/
%cup
   
/****************/
/* DECLARATIONS */
/****************/
/*****************************************************************************/   
/* Code between %{ and %}, both of which must be at the beginning of a line, */
/* will be copied letter to letter into the Lexer class code.                */
/* Here you declare member variables and functions that are used inside the  */
/* scanner actions.                                                          */  
/*****************************************************************************/   
%{   
    /*********************************************************************************/
    /* Create a new java_cup.runtime.Symbol with information about the current token */
    /*********************************************************************************/
    StringBuffer string = new StringBuffer();
    
    private Symbol symbol(int type)               {return new Symbol(type, yyline, yycolumn);}
    private Symbol symbol(int type, Object value) {return new Symbol(type, yyline, yycolumn, value);}
    
    private boolean printTokens = false;
    
    // returns current line number (first line has an index 1) 
    public int currentLine()
    {
        return yyline + 1;
    } 

	// routine for lexical errors
	private void onError(String err, String text){
		System.out.print(currentLine() + ": " + "Lexical error: " + err + " '");
		System.out.print(text);
		System.out.print("'");
		System.exit(0);
	}
	
	// prints current line number and token name
	private void printToken(String token){
		if(printTokens)
			System.out.println(currentLine() + ": " + token);
	}
	
	// prints current line number, token name, and token text
	private void printTokenWithText(String token, String text){
		if(printTokens){
			System.out.print(currentLine() + ": " + token + "(");
			System.out.print(text);
			System.out.println(")");
		}
	}
%}

/***********************/
/* MACRO DECALARATIONS */
/***********************/
LINE_TEMINATOR			= \r|\n|\r\n
WHITE_SPACE				= {LINE_TEMINATOR} | [ \t\f]
INTEGER					= 0 | [1-9][0-9]*
ID						= [a-z][A-Za-z_0-9]*
CLASS_ID				= [A-Z][A-Za-z_0-9]*

// Quotes
 LEGAL_QUOTE_CONTENT    = ([\x20-\x21\x23-\x5B\x5D-\x7E]|\\\"|\\\\|\\t|\\n)

// Comments
LINE_COMMENT			= "//" [^\r\n]* {LINE_TEMINATOR}?
UNCLOSED_BLOCK_COMMENT 	= "/*" ([^*] | \*+ [^/] )* 
BLOCK_COMMENT			= {UNCLOSED_BLOCK_COMMENT} "*/"
COMMENT					= {LINE_COMMENT} | {BLOCK_COMMENT}
   
   
%state QUOTE

/******************************/
/* DOLAR DOLAR - DON'T TOUCH! */
/******************************/

%%
 
/************************************************************/
/* LEXER matches regular expressions to actions (Java code) */
/************************************************************/
   
/**************************************************************/
/* YYINITIAL is the state at which the lexer begins scanning. */
/* So these regular expressions will only be matched if the   */
/* scanner is in the start state YYINITIAL.                   */
/**************************************************************/
   
<YYINITIAL> {

	"+"					{ printToken("PLUS"); 		return symbol(ICSym.PLUS);}
	"-"					{ printToken("MINUS");      return symbol(ICSym.MINUS);}
	"*"					{ printToken("MULTIPLY");   return symbol(ICSym.MULTIPLY);}
	"/"					{ printToken("DIVIDE");     return symbol(ICSym.DIVIDE);}
	"%"					{ printToken("MOD");    	return symbol(ICSym.MOD);}
	"("					{ printToken("LP");     	return symbol(ICSym.LP);}
	")"					{ printToken("RP");   		return symbol(ICSym.RP);}
	"{"					{ printToken("LCBR");  	    return symbol(ICSym.LCBR);}
	"}"					{ printToken("RCBR");  	 	return symbol(ICSym.RCBR);}
	"["					{    		return symbol(ICSym.LB);} //printToken("LB");
	"]"					{  	return symbol(ICSym.RB);} //printToken("RB");    
	"="					{ printToken("ASSIGN");     return symbol(ICSym.ASSIGN);}
	";"					{ printToken("SEMI");    	return symbol(ICSym.SEMI);}
	","			 	    { printToken("COMMA");      return symbol(ICSym.COMMA);}
	"<"					{ printToken("LT");    		return symbol(ICSym.LT);}
	">"			 	    { printToken("GT");         return symbol(ICSym.GT);}
	"."			 	    { printToken("DOT");        return symbol(ICSym.DOT);}
	"!"			 	    { printToken("LNEG");       return symbol(ICSym.LNEG);}
	"=="			 	{ printToken("EQUAL");      return symbol(ICSym.EQUAL);}
	"!="			 	{ printToken("NEQUAL");     return symbol(ICSym.NEQUAL);}
	">="			 	{ printToken("GTE");       	return symbol(ICSym.GTE);}
	"<="			 	{ printToken("LTE");        return symbol(ICSym.LTE);}
	"||"			 	{ printToken("LOR");       	return symbol(ICSym.LOR);}
	"&&"			 	{ printToken("LAND");       return symbol(ICSym.LAND);}
	"length"			{ printToken("LENGTH");     return symbol(ICSym.LENGTH);}
	"continue"			{ printToken("CONTINUE");   return symbol(ICSym.CONTINUE);}
	"boolean"			{ printToken("BOOLEAN");    return symbol(ICSym.BOOLEAN);}
	"break"				{ printToken("BREAK");     	return symbol(ICSym.BREAK);}
	"int"			    { printToken("INT");      	return symbol(ICSym.INT);}
	"string"		    { printToken("STRING");     return symbol(ICSym.STRING);}
	"class"			    { printToken("CLASS");      return symbol(ICSym.CLASS);}
	"while"			    { printToken("WHILE");      return symbol(ICSym.WHILE);}
	"static"		    { printToken("STATIC");     return symbol(ICSym.STATIC);}
	"void"	 		    { printToken("VOID");       return symbol(ICSym.VOID);}
	"return"		    { printToken("RETURN");     return symbol(ICSym.RETURN);}
	"if"			    { printToken("IF");       	return symbol(ICSym.IF);}
	"else"			    { printToken("ELSE");       return symbol(ICSym.ELSE);}
	"this"			    { printToken("THIS");       return symbol(ICSym.THIS);}
	"new" 			    { printToken("NEW");        return symbol(ICSym.NEW);}
	"true" 			    { printToken("TRUE");       return symbol(ICSym.TRUE);}
	"false"			    { printToken("FALSE");      return symbol(ICSym.FALSE);}
	"extends"		    { printToken("EXTENDS");      return symbol(ICSym.EXTENDS);}
	"null"	  		    { printToken("NULL");       return symbol(ICSym.NULL);}
	
	{INTEGER}					{
									try{
									
										Integer.valueOf(yytext());
									}
									catch(NumberFormatException e){
										onError("illegal value of integer number", yytext());
									}
			
									printTokenWithText("INTEGER", yytext());
									return symbol(ICSym.INTEGER, new Integer(yytext()));
								}   
								
	"\""                        { string.setLength(0); yybegin(QUOTE);}
	
	{ID}						{ printTokenWithText("ID", yytext());					return symbol(ICSym.ID, new String(yytext())); }
	{CLASS_ID}					{ printTokenWithText("CLASS_ID", yytext());				return symbol(ICSym.CLASS_ID, new String(yytext()));}

   
	{WHITE_SPACE}				{ /* skip white spaces */ } 
	{COMMENT}					{ /* skip comments */ }
	{UNCLOSED_BLOCK_COMMENT}	{ onError("Unclosed Block Comment", yytext()); }
	
	<<EOF>>	  		    		{ /*System.out.print((currentLine()+1) + ": " + "EOF"); */  return symbol(ICSym.EOF);}
		
	// illegal character detection (no regular expression applies)
	
	.							{ onError("illegal character", yytext()); }

}

<QUOTE> {
	\"                           { yybegin(YYINITIAL); 
		  							   printTokenWithText("QUOTE", "\"" + string.toString() + "\"");
		                               return symbol(ICSym.QUOTE, 
		                               string.toString()); }
	{LEGAL_QUOTE_CONTENT}+ 		 { string.append( yytext() ); }
	{LINE_TEMINATOR}			 { onError("Unclosed quote", string.toString());}
	<<EOF>>  	 				 { onError("Unclosed quote", string.toString());}
	
	.							 { onError("Illegal Quote Content", yytext());}
 }