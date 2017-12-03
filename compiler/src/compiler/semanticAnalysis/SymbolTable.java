package compiler.semanticAnalysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import compiler.tree.Type;

public class SymbolTable 
{
	boolean debug = false; //For debugging purposes. [Turn OFF/ON]
	
	public Map<String, Stack<InScopeId>> map;
	int scopeDepth;
	
	public SymbolTable()
	{
		map = new HashMap<>();
		scopeDepth = 0;
	}
	
	boolean addSymbol(Type type, String id)
	{
		
		
		InScopeId inSId = new InScopeId(type, this.scopeDepth); //Create variable details of the current scope [With current depth]
		
		if(!map.containsKey(id))  //The ID wasn't found - meaning wasn't declared ever
		{
			map.put(id, new Stack<InScopeId>()); //Add new ID to the SymbolTable with a new STACK data-structure 
		}
		else
		{//The ID was found
			if(!map.get(id).isEmpty() && map.get(id).peek().scopeDepth == scopeDepth) //Variable was already declared in the current Scope!
			{
				if(debug)System.out.println("SymbolTable->addSymbol: Scope " + this.scopeDepth + ": '" + id + "' was already " );
				return false; //Variable details weren't added to the current scope!
			}
		}
		
		if(debug)System.out.println("SymbolTable->addSymbol: Pushed '" + id + "' to scope: " + this.scopeDepth);
		
		map.get(id).push(inSId); //Add to the STACK the variable the details of the current scope
		
		return true; //Variable details were added to the current scope successfully
	}
	
	void symbolInit(String id)
	{
		map.get(id).peek().isInitialized = true;
	}
	
	public Type getSymbolType(String id)
	{
		if(!map.containsKey(id))
		{
			return null; //Variable was never declared
		}
				
		return map.get(id).peek().type;
	}
	
	boolean isDeclared(String id)
	{
		if(map.containsKey(id) && !map.get(id).isEmpty()) //The ID appear in the Symbol table and its STACK is not-EMPTY
		{
			return true;
		}
			
		return false;
	}
	
	boolean isInitialised(String id)
	{
		return map.get(id).peek().isInitialized;
	}
	
	
	public void pushScope()
	{
		scopeDepth++;
	}
	
	//When exiting from a scope
	public void popScope()
	{
		for(String id : map.keySet())
		{
			if(!map.get(id).isEmpty() && map.get(id).peek().scopeDepth == this.scopeDepth)
			{
				map.get(id).pop();
			}
		}
		
		scopeDepth--;
	}
	
	public class InScopeId
	{
		Type type;
		public int scopeDepth;
		boolean isInitialized;
		
		public InScopeId(Type type, int scopeDepth)
		{
			this.type = type;
			this.scopeDepth = scopeDepth;
			this.isInitialized = false;
		}
	}

	
	public void tryAddSymbol(Type type, String id,boolean init , int line)
	{
		if(!addSymbol(type, id)) //Variable was already declared in the current scope! ERROR
		{
			SemanticAnalyser.PrintError("Variable '" + id + "' was already declared" , line);
		}
		else  //Variable was added to the current scope
		{
			if(init) //"Initialize" variable in case init==true
			{
				symbolInit(id);	//Init the variable because it was assigned
			}
		}
	}
	
	public boolean tryInitSymbol(String id)
	{
		if(!isDeclared(id)) 
		{
			return false;  // Variable was never declared
		}
		
		symbolInit(id); //Init Symbol
		
		return true;  //Variable was initilized
	}
	
}
