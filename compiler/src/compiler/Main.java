package compiler;

import java.io.*;
import java.util.Arrays;

import compiler.LIRGeneration.LIRGenerator;
import compiler.semanticAnalysis.*;
import compiler.tree.*;
import java_cup.runtime.*;

/** The entry point of the SLP (Straight Line Program) application.
 *
 */
public class Main 
{
	static boolean saveTest = false;
	static boolean runTest = false;
	
	private static boolean printtokens = false;
	
	/** Reads an SLP and pretty-prints it.
	 * 
	 * @param args Should be the name of the file containing an SLP.
	 * @throws Exception 
	 */
	
	public static void main(String[] args) throws Exception 
	{
//		saveTest = true;
//		runTest = true;
//		Testing.runSingleTest();
//		Testing.runSingleTest();
//		System.exit(0);
		if(runTest) {Testing.runTests("Testing\\");  System.exit(0);}
	
		
		
			if (args.length == 0) 
			{
				System.out.println("Error: Missing input file argument!");
				printUsage();
				System.exit(-1) ; 
			}
			if (args.length == 2) 
			{
				if (args[1].equals("-printtokens")) {
					printtokens =  true;
				}
				else {
					printUsage();
					System.exit(-1);
				} 
			}  
			     
			// Parse the input file
			FileReader txtFile = new FileReader(args[0]);
			Lexer scanner = new Lexer(txtFile); 
			ICCup parser = new ICCup(scanner); 
			parser.printTokens = printtokens;
			
			
 			Symbol parseSymbol = parser.parse(); 
			
			
			//System.out.println("\nParsed " + args[0] + " successfully!\n");
			Program root = (Program) parseSymbol.value;

			
			// 	Pretty-print the program to System.out
			//	PrettyPrinter printer = new PrettyPrinter(root);
			//	printer.print();
			
			//	System.out.println("\n\n========== Start Semantic Checks ==========\n\n");
			
			//	System.out.println("\nPrettyPrinter completed successfully!\n");
			SemanticAnalyser analyser = new SemanticAnalyser(root);
			analyser.analyse();
			
			//	System.out.println("\n\n========== Semantic Checks - DONE ==========\n\n");
			

			// Generate LIR code
			LIRGenerator LIRG = new LIRGenerator(root);
			String lirCode = LIRG.generate();
			
			// Write the newly created code to file
			PrintWriter out = new PrintWriter("LIRTestResult.lir");
			out.println(lirCode);
			out.close();
			// run the microLIR interpreter on the code file
			Process process = new ProcessBuilder(
			"java","-classpath","\".\\microLIR\\build;.\\microLIR\\java-cup-v11a-runtime.jar\"", "microLIR.Main", ".\\LIRTestResult.lir").start();
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;
			
			System.out.println("\n========= Interpreting the Code ==========\n");
			
			
		
			while ((line = br.readLine()) != null) 
			{
			  System.out.println(line);
			} 
			
			if(saveTest) Testing.saveToFile("Testing\\",lirCode,args[0]);
			

			
			

	}
	
	/** Prints usage information about this application to System.out
	 */
	public static void printUsage() {
		System.out.println("Usage: slp file [-printtokens]"); 
	}
}