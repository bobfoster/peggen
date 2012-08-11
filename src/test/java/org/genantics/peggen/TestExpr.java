/*******************************************************************************
 * Copyright (C) 2012 Bob Foster. All rights reserved.
 * 
 * This software is provided under the terms of the Apache License, Version 2.0
 * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
 * 
 * Contributors:
 * 
 *    Bob Foster, initial API and implementation.
 *******************************************************************************/
 
package org.genantics.peggen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import junit.framework.TestCase;

/**
 *
 * @author Bob Foster
 */
public class TestExpr extends TestCase {
  
  private void generate(String pegName, String className) {
    PegGen gen = new PegGen();
    File file = new File("./target/test/java/org/genantics/generated/"+className+".java");
    File inFile = new File("./src/test/resources/org/genantics/peggen/"+pegName+".peg");
    try {
      File parent = file.getParentFile();
      if (!parent.exists())
        assertTrue(parent.mkdirs());
      PrintWriter writer = new PrintWriter(new FileWriter(file));
      try {
        gen.generate(inFile, writer, "org.genantics.generated", className, "Grammar");
      } catch (Exception e) {
        e.printStackTrace();
        assertTrue(false);
      } finally {
        writer.close();
      }
      assertTrue(file.exists());
      assertTrue(file.length() > 0);
    } catch (IOException e) {
      e.printStackTrace();
      assertTrue(false);
    }
  }
  
  /**
   * Test that PegGen generate method with a simple grammar.
   * 
   * Not a functional test in the sense that the generated parser is tested,
   * which would require compiling the generated file,
   * just a test that something gets generated.
   */
  public void testSimpleExpr() {
    generate("expr-simple", "ParseSimpleExpr");
  }
  
  /**
   * Test the same grammar written with "classic" (Bryan Ford) syntax.
   */
  public void testClassicExpr() {
    generate("expr-classic", "ParseClassicExpr");
  }
  
  /**
   * Test the same grammar written with BNF syntax.
   */
  public void testBNFExpr() {
    generate("expr-bnf", "ParseBNFExpr");
  }

}
