package compiler;

import java.io.*;
import java.util.Arrays;

import compiler.LIRGeneration.LIRGenerator;
import compiler.semanticAnalysis.*;
import compiler.tree.*;
import java_cup.runtime.*;


public class Testing 
{
	static String result_str = "Result";
	static String lir_str = "LirCode";
	static String ic_str = "Test";
	
	public static void saveToFile(String folderPath ,String lirCode, String codePath) throws IOException
	{
		
		int counter = 0;
		PrintWriter pWriter;
		BufferedReader br;
		
		while(new File(folderPath + result_str + counter + ".txt").isFile())
		{
			counter++;
		}
		
		// run the microLIR interpreter on the code file
		Process process = new ProcessBuilder("java","-classpath","\".\\microLIR\\build;.\\microLIR\\java-cup-v11a-runtime.jar\"", "microLIR.Main", ".\\LIRTestResult.lir").start();
		InputStream is = process.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		br = new BufferedReader(isr);
		String line;
		
		
		//Save result:
		
		pWriter = new PrintWriter(folderPath + result_str + counter + ".txt");
		
		while ((line = br.readLine()) != null) 
		{
		  pWriter.println(line);
		} 
		
		pWriter.close();
		br.close();
		
		//Save LIR code:
		pWriter = new PrintWriter(folderPath + lir_str + counter + ".txt");
		pWriter.println(lirCode);
		pWriter.close();
		
		
		//Save Code to a file
		pWriter = new PrintWriter(folderPath + ic_str + counter + ".txt");

		br = new BufferedReader(new FileReader(codePath)); 
		{
		    while ((line = br.readLine()) != null) 
		    {
				pWriter.println(line);
		    }
		}
		
		pWriter.close();
		br.close();
		
		
	}
	
	public static void runSingleTest() throws IOException, Exception
	{
		BufferedReader br;
		String resultPath;
		
    	resultPath = "C:\\Users\\Admin\\workspace_EX4_take2\\Compiler\\Testing\\Result175.txt";
    	
		br = new BufferedReader(new FileReader(resultPath));
		
		//System.out.println("Compare: " + resultPath + " and " + file.getName());
		
		System.out.println("******** Run test on: " + resultPath + " ********");
		
		if(!compareFiles  ( br ,  interpretationBR(  "C:\\Users\\Admin\\workspace_EX4_take2\\Compiler\\Testing\\Test175.txt" )))
		{
			System.out.println("******** Test failed on: " + resultPath + " ********");
			br.close();
			return;
		}
		br.close();

		
		System.out.println("******** All tests were completed successfully! ********");

	}

	public static void runTests(String folderPath) throws Exception
	{
		File[] files = new File(folderPath).listFiles();
		BufferedReader br;
		String resultPath;
		
		for (File file : files) 
		{
	        if (file.isFile() && file.getName().contains(ic_str))//Test file 
	        {
	        	resultPath = result_str + file.getName().substring(ic_str.length());
	        	
	    		br = new BufferedReader(new FileReader(folderPath + resultPath));
	    		
	    		//System.out.println("Compare: " + resultPath + " and " + file.getName());
	    		
	    		System.out.println("******** Run test on: " + file.getName() + " ********");
	    		
	    		if(!compareFiles  ( br ,  interpretationBR(file.getAbsolutePath()) )  )
	    		{
	    			System.out.println("******** Test failed on: " + file.getName() + " ********");
	    			br.close();
	    			return;
	    		}
	    		br.close();

	        }
	    }
		
		System.out.println("******** All tests were completed successfully! ********");
	}

	public static boolean compareFiles(BufferedReader br1,BufferedReader br2) throws IOException
	{
		String line_br1 = null ,line_br2 = null;
		
		while ((line_br1 = br1.readLine()) != null   &&  (line_br2 = br2.readLine()) != null) 
		{
//			System.out.println(line_br1);
//			System.out.println(line_br2);

			if(!line_br1.equals(line_br2))
			{
				System.out.println("The following lines are not equal:");
				System.out.println("Result file:  " + line_br1);
				System.out.println("Interpertaion:" + line_br2);
				br2.close();
				return false;
			}
		}
		
		line_br2 = br2.readLine();
		
		//System.out.println("Done " + (line_br1 != null)  +  " " + (line_br2 != null) );
		if(line_br1 != null || line_br2 != null)
		{
			br2.close();
			return false;
		}
		br2.close();
		return true;
	}
	
	public static BufferedReader interpretationBR(String fileName) throws Exception
	{
		// Parse the input files
		FileReader txtFile = new FileReader(fileName);
		Lexer scanner = new Lexer(txtFile); 
		ICCup parser = new ICCup(scanner); 
		parser.printTokens = false;
		Symbol parseSymbol = parser.parse(); 
		Program root = (Program) parseSymbol.value;
		//	System.out.println("\n\n========== Start Semantic Checks ==========\n\n");
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
		return br;
	}
	
	
}
