package compiler.semanticAnalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import compiler.tree.ASTNode;
import compiler.tree.BasicType;
import compiler.tree.ClassDecl;
import compiler.tree.Field;
import compiler.tree.Formal;
import compiler.tree.Method;
import compiler.tree.Program;
import compiler.tree.Type;

public class TypeTable 
{
	boolean debug = true;
	
	private Map<String,List<Method>> MethodsInClass = new HashMap<String,List<Method>>();
	public Map<String,Set<Field>> FieldsInClass = new HashMap<String,Set<Field>>();
	private Map<String,String> SuperClasses = new HashMap<String,String>();
	public Set<String> allClasses = new HashSet<String>();
	
	
	//Matan:
	public HashMap<String,Map<String,Integer>> fieldsInClassPos = new HashMap<String, Map<String,Integer>>();
	//Matan:
	private void create_FieldsInClassPos_map()
	{
		HashMap<String,Integer> classFieldsPos;
		int counter = 1;
		
		for(String className: allClasses) //Iterate through all class names
		{
			classFieldsPos = new HashMap<String, Integer>();
			fieldsInClassPos.put(className, classFieldsPos);
			counter = 1;

			Set<Field> classFieldsSet = ClassFields(className); //Get class fields
			if(classFieldsSet!=null)
			{
				for(Field f: classFieldsSet)
				{
					for(String fName: f.idList)
					{
						//System.out.println("Class " + className + " Field " + fName + " Pos=" + counter);
						classFieldsPos.put(fName, counter);
						counter++;
					}
				}
			}
			
		}
		
		//System.out.println("Testing..." + fieldsInClassPos.get("B").get("c"));
	}
	
	public int getFieldIndex(String className, String fieldName)
	{
		return fieldsInClassPos.get(className).get(fieldName);
	}

	
	
	public TypeTable(ASTNode root) 
	{
		Program program = (Program)root;
		int mainCount=0;
		
		List<Method> childMethods;
		Set<Field> childFields;
		Set<String> checkDuplication;
		Set<String> checkDuplicationForFields;
		String classMain = "";
		for (ClassDecl cd : program.classDecl_l)
		{
			allClasses.add(cd.name);
			childMethods = new ArrayList<>();
			childFields = new HashSet<>();
			
			// add to every method the of the class in which it was declared
			for(Method m: cd.methods)
			{
				m.className = cd.name;
			}
			
			if(cd.superClassName!=null) // the class extends from other class
			{
				if(cd.superClassName.equals("Library"))
				{
					SemanticAnalyser.PrintError("Can't extends from class: Library" , cd.line);
				}
				if(SuperClasses.put(cd.name,cd.superClassName)!=null)
				{
					SemanticAnalyser.PrintError("This Class is already exist: " + cd.name , cd.line);
				}
				
				List<Method> tempChildMethods = MethodsInClass.get(cd.superClassName);
				Set<Field> tempChildFields = FieldsInClass.get(cd.superClassName);				
				if(tempChildMethods == null || tempChildFields==null)
				{
					SemanticAnalyser.PrintError("There is no such super Class: " + cd.superClassName , cd.line);
				}
				
				childMethods.addAll(tempChildMethods);
				childFields.addAll(tempChildFields);
				
				checkDuplication =  new HashSet<>();
				
				for(Method m: cd.methods)
				{
					if(!checkDuplication.add(m.name))
					{
						SemanticAnalyser.PrintError("There are two Method: " + m.name +" in the class: " + cd.name + "  with the same name" , m.line);
					}
				}
				
				checkDuplication =  new HashSet<>();
				
				for(Method m: childMethods)
				{
					if(!checkDuplication.add(m.name))
					{
						SemanticAnalyser.PrintError("There are two Method: " + m.name +" in the class: " + cd.superClassName + "  with the same name" , m.line);
					}
				}
				
				int index=0;
				for(int i=0; i< cd.methods.size(); i++)
				{
					if(!checkDuplication.add(cd.methods.get(i).name)) // overridden/redefine
					{
						if(cd.methods.get(i).name.equals("main"))
						{
							SemanticAnalyser.PrintError("There Should be only 1 main method in the Program",cd.methods.get(i).line);
						}
						
						index = removeMethod(cd.methods.get(i).name,childMethods);
						childMethods.add(index,cd.methods.get(i));				
					}
					else
					{
						childMethods.add(cd.methods.get(i));
					}
					
	
				}
	
				
				for(Field f: cd.fields)
				{
					if(!childFields.add(f))
					{
						SemanticAnalyser.PrintError("The Field: " + f.idList.get(0) +" is already exist" , f.line);
					}
				}
				
				if(MethodsInClass.put(cd.name,childMethods)!=null)
				{
					SemanticAnalyser.PrintError("The class: " + cd.name +" is already exist" , cd.line);
				}
				
				if(FieldsInClass.put(cd.name,childFields)!=null)
				{
					SemanticAnalyser.PrintError("The class: " + cd.name +" is already exist" , cd.line);
				}
			}
			else // regular class
			{
				checkDuplication =  new HashSet<>();
				for(Method m: cd.methods)
				{
					if(!checkDuplication.add(m.name))
					{
						SemanticAnalyser.PrintError("There are two Method: " + m.name +" in the class: " + cd.name + "  with the same name" , m.line);
					}
				}
				
				for(int i=0; i<cd.methods.size();i++)
				{
					childMethods.add(i,cd.methods.get(i));
				}
				
				for(Field f: cd.fields)
				{
					if(!childFields.add(f))
					{
						SemanticAnalyser.PrintError("The Field: " + f.idList.get(0) +" is already exist" , f.line);
					}
				}
				
				if(MethodsInClass.put(cd.name,childMethods)!=null)
				{
					SemanticAnalyser.PrintError("The class: " + cd.name +" is already exist" , cd.line);
				}
				
				if(FieldsInClass.put(cd.name,childFields)!=null)
				{
					SemanticAnalyser.PrintError("The class: " + cd.name +" is already exist" , cd.line);
				}
				
			}
			
		}
		
		
		for (ClassDecl cd : program.classDecl_l)
		{
			checkDuplicationForFields =  new HashSet<>();
			checkDuplication =  new HashSet<>();
			if(FieldsInClass.get(cd.name)!=null)
			{		
				for(Field d: FieldsInClass.get(cd.name))
				{
					if(d.type.basicType == BasicType.CLASS_ID )
					{
						if(!allClasses.contains(d.type.className))
						{
							SemanticAnalyser.PrintError("There is no such Class Type for this Field: " + d.idList.get(0) , d.line);
						}
					}
				
					for(String name: d.idList)
					{
						if(!checkDuplicationForFields.add(name))
						{
							SemanticAnalyser.PrintError("The Field: " + name +" is already exist" , d.line);
						}
					}
				
				}
			}
			
			if(MethodsInClass.get(cd.name) != null)
			{
				List<Method> lm= MethodsInClass.get(cd.name);
				for(Method m: lm)
				{
				
					if(m.type.basicType == BasicType.CLASS_ID )
					{
						if( !FieldsInClass.containsKey(m.type.className) && !MethodsInClass.containsKey(m.type.className))
						{
							SemanticAnalyser.PrintError("The class: " + m.type.className +" is not defined" , m.line);
						}
					}
				
								
					if(m.name.equals("main")) // checking the Main function
					{
						if(mainCount == 0)
						{
							classMain=cd.name;
							mainCount++;
						}
						else
						{
							if(!isSuperClassOf(cd.name,classMain))
							{
								SemanticAnalyser.PrintError("There Should be only 1 main method in the Program",m.line);
							}
						}
						
						if(!m.isStatic)
						{
							SemanticAnalyser.PrintError("Main should be a static method" , m.line);
						}
						if(m.type.basicType != BasicType.VOID)
						{
							SemanticAnalyser.PrintError("Main should be void" , m.line);
						}
					
						if(m.formals == null || m.formals.size() != 1)
						{
							SemanticAnalyser.PrintError("Main should have only one formal" , m.line);
						}
					
						for(Formal f: m.formals)
						{
							if(f.type.basicType!= BasicType.STRING || f.type.numOfArrays != 1 || !f.name.equals("args"))
							{
								SemanticAnalyser.PrintError("Argument of Main should be: (string[] args)" , f.line);
							}
						}			
					}
				
					if(!checkDuplication.add(m.name))
					{
						SemanticAnalyser.PrintError("The Method: " + m.name +" is already exist" , m.line);
					}
					
					if(!checkDuplicationForFields.add(m.name))
					{
						SemanticAnalyser.PrintError(" A field with the name: " + m.name + " is already exist." , m.line);
					}
				
					for(Formal f: m.formals)
					{
						if(f.type.basicType == BasicType.CLASS_ID )
						{
							if( !FieldsInClass.containsKey(f.type.className) && !MethodsInClass.containsKey(f.type.className))
							{
								SemanticAnalyser.PrintError("The class: " + f.type.className +" is not defined" , f.line);
							}
						}
					}	
				}
			}
			
		}		
		if(mainCount == 0)
		{
			SemanticAnalyser.PrintError("There is no main method in the Program",0);
		}
		/*if(mainCount > 1)
		{
			SemanticAnalyser.PrintError("There Should be only 1 main method in the Program",0);
		}*/	
		
		/*
		 * so far so good, now we add the Library class to the TypeTable:
		 */
		
		List<Method> libraryMethods = new ArrayList<>();
		Method m;
		allClasses.add("Library");
		// static void println(string s);
		List<Formal> f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.STRING),"s"));
		m = new Method(0,new Type(0,BasicType.VOID),"println",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static void print(string s);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.STRING),"s"));
		m = new Method(0,new Type(0,BasicType.VOID),"print",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static void printi(int i);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.INT),"i"));
		m = new Method(0,new Type(0,BasicType.VOID),"printi",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static void printb(boolean b);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.BOOLEAN),"b"));
		m = new Method(0,new Type(0,BasicType.VOID),"printb",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		
		// static int readi();
		f = new ArrayList<>();
		m = new Method(0,new Type(0,BasicType.INT),"readi",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static string readln();
		f = new ArrayList<>();
		m = new Method(0,new Type(0,BasicType.STRING),"readln",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static boolean eof();
		f = new ArrayList<>();
		m = new Method(0,new Type(0,BasicType.BOOLEAN),"eof",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static int stoi(string s, int n);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.STRING),"s"));
		m = new Method(0,new Type(0,BasicType.INT),"stoi",f,null);
		m.setStatic();
		libraryMethods.add(m);		
		
		// static string itos(int i)
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.INT),"i"));
		m = new Method(0,new Type(0,BasicType.STRING),"itos",f,null);
		m.setStatic();
		libraryMethods.add(m);	
		
		// static int[] stoa(string s);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.STRING),"s"));
		m = new Method(0,new Type(0,1,BasicType.INT,null),"stoa",f,null);
		m.setStatic();
		libraryMethods.add(m);	
		
		// static string atos(int[] a);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,1,BasicType.INT,null),"a"));
		m = new Method(0,new Type(0,BasicType.STRING),"atos",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		// static int random(int i);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.INT),"i"));
		m = new Method(0,new Type(0,BasicType.INT),"random",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		//static int time();
		f = new ArrayList<>();
		m = new Method(0,new Type(0,BasicType.INT),"time",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		//static void exit(int i);
		f = new ArrayList<>();
		f.add(new Formal(0,new Type(0,BasicType.INT),"i"));
		m = new Method(0,new Type(0,BasicType.VOID),"exit",f,null);
		m.setStatic();
		libraryMethods.add(m);
		
		MethodsInClass.put("Library",libraryMethods);
		
		create_FieldsInClassPos_map();
	}
	
	private int removeMethod(String name,List<Method> childMethods)
	{
		int i;
		for(i=0; i<childMethods.size();i++)
		{
			if(name.equals(childMethods.get(i).name))
			{
				childMethods.remove(i);
				break;
			}
		}
		
		return i;
		
	}
	
	public Set<Field> ClassFields(String className)
	{
		return FieldsInClass.get(className);
	}
	
	public List<Method> ClassMethods(String className)
	{
		return MethodsInClass.get(className);
	}
	
	public List<Method> ClassStaticMethods(String className)
	{
		List<Method> staticMethods = new ArrayList<>();
		List<Method> allMethods = MethodsInClass.get(className);

		if(allMethods!=null)
		{
			for(int i=0; i<allMethods.size();i++)
			{
				if(allMethods.get(i).isStatic)
				{
					staticMethods.add(allMethods.get(i));
				}
			}
		}
		
		return staticMethods;
	}
	
	public List<Method> ClassNonStaticMethods(String className)
	{
		List<Method> nonStaticMethods = new ArrayList<>();
		List<Method> allMethods = MethodsInClass.get(className);

		if(allMethods!=null)
		{
			for(int i=0; i<allMethods.size();i++)
			{
				if(!allMethods.get(i).isStatic)
				{
					nonStaticMethods.add(allMethods.get(i));
				}
			}
		}
		
		return nonStaticMethods;
	}
	
	
	public boolean isSuperClassOf(String subClass,String superClass)
	{
		if(subClass.equals(superClass))
		{
			return true;
		}
		String tempSuperClass;
		tempSuperClass = SuperClasses.get(subClass);
		if(tempSuperClass==null)
		{
			//if(debug) System.out.println("isSuperClassOf: " + "no SuperClass!");
			return false;
		}
		if(tempSuperClass.equals(superClass))
		{
			return true;
		}
		else
		{
			return isSuperClassOf(tempSuperClass,superClass);
		}
		
		
	}
	

	public Type getFieldType(String className,String fieldName)
	{
		if(!FieldsInClass.containsKey(className)) //className doesn't exist
		{
			return null;
		}
		
		for(Field fld : FieldsInClass.get(className))
		{
			for(String id : fld.idList)
			{
				if(id.equals(fieldName))
				{
					return fld.type;
				}
			}

		}
		
		return null;
	}
	
	public Type getMethodType(String className,String methodName)
	{
		if(MethodsInClass.containsKey(className) && MethodsInClass.get(className)!=null) //className exist
		{
			for(Method meth : MethodsInClass.get(className)) //Find method in class
			{
				if(methodName.equals(meth.name))
				{
					return meth.type; //Return the Type
				}
			}
		}
		
		return null;
	}

	public Type isLegalMethod(String curClass,boolean isStaticCall,boolean isInstance,boolean isCurMethodStatic,Method m)
	{
		List<Method> sm;
		Method method = null;
		
		
		if(!allClasses.contains(curClass))
		{
			SemanticAnalyser.PrintError("There is no such class: " + curClass , m.line);
			return null;
		}
		
		if(isStaticCall)
		{
			sm = ClassStaticMethods(curClass);
		}
		else // virtual call
		{
			if(isInstance) // virtual call from instance can call only for Non-static Methods
			{
				sm = ClassNonStaticMethods(curClass);
			}
			else //  direct calls ( without the instance), 
			{
				if(isCurMethodStatic) // static Methods can direct virtual call only to static Methods
				{
					sm = ClassStaticMethods(curClass);
				}
				else // // Non-static Methods can direct virtual call to everyMethod
				{
					sm = ClassMethods(curClass);
				}
			}
		}
				
		
		for(Method m1: sm)
		{
				if(m1.name.equals(m.name))
				{
					method = m1;
					break;
				}
		}
		
		if(method == null)
		{
			
			if(isStaticCall) // static calls can call only for static Methods
			{
				SemanticAnalyser.PrintError("There is no such static method: " + m.name + " in the class: " + curClass , m.line);
				return null;
			}
			else // virtual call
			{
				if(isInstance) // virtual call from instance can call only for Non-static Methods
				{
					SemanticAnalyser.PrintError("There is no such non-static method: " + m.name + " in the class: " + curClass , m.line);
					return null;
				}
				else //  direct calls ( without the instance), 
				{
					if(isCurMethodStatic) // static Methods can direct virtual call only to static Methods
					{
						SemanticAnalyser.PrintError("There is no such static method: " + m.name + " in the class: " + curClass , m.line);
						return null;
					}
					else // // Non-static Methods can direct virtual call to everyMethod
					{
						SemanticAnalyser.PrintError("There is no such method: " + m.name + " in the class: " + curClass , m.line);
						return null;
					}
				}
			}
						
		}
		
		
		if(method.formals.size() != m.formals.size())
		{
			SemanticAnalyser.PrintError("Number of arguments isn't the same" , m.line);
			return null;
		}
		
		Formal f1,f2;
		for(int i = 0; i< method.formals.size() ; i++)
		{
			f1 = method.formals.get(i);
			f2 = m.formals.get(i);
			
			if(!compareTypes(f1.type,f2.type))
			{
				SemanticAnalyser.PrintError("Incompatible method arguments, for argument number: "+(i+1) , m.line);
				return null;
			}
		}
		
		return method.type;
	}
	
	//t1 should be the superClass and t2 should be the subClass
	public boolean compareTypes(Type t1,Type t2)
	{
		
		if(t1 == null || t2 == null)
			return false;
		
		if((t1.basicType != BasicType.INT || t1.numOfArrays > 0) && t2.basicType ==BasicType.NULL)
			return true;
		
		if(t1.basicType != t2.basicType)
			return false;
		
		if(t1.numOfArrays != t2.numOfArrays)
			return false;
		
		if(t1.className == null && t2.className!= null)
			return false;

		if(t1.className != null && t2.className==null)
			return false;
		
		if(t1.className != null && t2.className!=null)
		{
			if(t1.numOfArrays > 0 && t2.numOfArrays > 0 && !t1.className.equals(t2.className))
			{
				return false; //Arrays must be exactly from the same Type
			}
			
			return isSuperClassOf(t2.className, t1.className);
		}
		
		return true;
		
	}

	public Method getMethod(String className,String methodName)
	{
		if(MethodsInClass.containsKey(className) && MethodsInClass.get(className)!=null) //className exist
		{
			for(Method meth : MethodsInClass.get(className))
			{
				if(methodName.equals(meth.name))
				{
					return meth;
				}
			}
		}
		return null;
		
		
	}
}
