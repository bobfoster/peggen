/*******************************************************************************
 * Copyright (C) 2003-2012 Bob Foster. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster, initial API and implementation.
 *******************************************************************************/
 
package org.genantics.peggen;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class for generating parsers.
 * 
 * @author Bob Foster
 */
public class PegGen {
	
	public static void main(String[] args)
	{
		if (args.length < 3 || args.length > 5)
		{
			System.out.println("Usage: java -jar peggen-1.0.jar input output startRule [packageName [className]]");
			System.exit(1);
		}
		File inputFile = new File(args[0]);
		if (!inputFile.exists())
		{
			System.out.println("Input file "+args[0]+" not found");
			System.exit(1);
		}
		File outputFile = new File(args[1]);
		String startRule = args[2];
		try {
			PrintWriter writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(outputFile)));
			String packageName = "";
			if (args.length >= 4)
				packageName = args[3];
			String className = "GeneratedParser";
			if (args.length == 5)
				className = args[4];
			PegGen pegGen = new PegGen();
			pegGen.generate(inputFile, writer, packageName, className, startRule);
			writer.close();
		} catch (IOException e) {
			System.out.println("Input/output exception");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void generate(PrintWriter writer, String packageName, String className) throws IOException {
		generate("com/objfac/peg/parser/peg.peg", writer, packageName, className, "Grammar");
	}

	public void generate(String resourceName, PrintWriter writer, String packageName, String className, String startRule) throws IOException {
		char[] input = readPegGrammar(resourceName);
		generate(input, writer, packageName, className, startRule);
	}
	
	public void generate(File inputFile, PrintWriter writer, String packageName, String className, String startRule) throws IOException {
		InputStream stream = new BufferedInputStream(new FileInputStream(inputFile));
		char[] input = readGrammar(stream);
		generate(input, writer, packageName, className, startRule);
	}
	
	public void generate(char[] input, PrintWriter writer, String packageName, String className, String startRule) throws IOException {
		Parser parser = new Parser();
		Node[] tree = parser.parseGrammar(input, 0, input.length);
		List list = parser.getErrors();
		if (list != null) {
			Iterator it = list.iterator();
			while (it.hasNext())
				System.out.println(it.next());
		}
		if (tree == null) return;
		SimplePegGenerator gen = new SimplePegGenerator();
		gen.generate(tree[0], input, writer, packageName, className, startRule, "\t");
	}

	
	public static char[] readGrammar(InputStream stream) throws IOException {
		Reader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuffer buf = new StringBuffer();
		int c;
		while ((c = reader.read()) >= 0)
			buf.append((char)c);
		return buf.toString().toCharArray();
	}
	
	public static char[] readPegGrammar(String resourceName) throws IOException {
		InputStream stream = PegGen.class.getClassLoader().getResourceAsStream(resourceName);
		return readGrammar(stream);
	}

}

